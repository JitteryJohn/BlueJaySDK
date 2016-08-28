package bluejay.sdk;

import android.content.Context;

import org.json.JSONArray;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class Client {

    interface AdsCallback {
        void call(String adUrl);
    }

    final Context ctx;
    final OkHttpClient client;
    final String baseUrl = "http://52.53.183.113:5678";

    public Client(Context context) {
        ctx = context;
        client = new OkHttpClient();
    }

    Call api(String method, String endpoint) {
        Bluejay.log("%s %s", method, endpoint);
        RequestBody reqBody = "GET".equals(method) ? null : RequestBody.create(MediaType.parse("application/json"), "");
        return client.newCall(new Request.Builder()
                .method(method, reqBody)
                .url(baseUrl + endpoint)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build());
    }

    JSONArray asJSON(Response response) {
        try (final ResponseBody body = response.body()) {
            return new JSONArray(body.string());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void collectBeacon(String beaconId, final String deviceId) {
        beaconId = beaconId.replaceAll(":", "").toLowerCase();
        final String endpoint = String.format("/collect/%s/%s", beaconId, deviceId);
        api("POST", endpoint).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Bluejay.oops("%s: %s", endpoint, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() >= 300) {
                    Bluejay.oops("%s: %d", endpoint, response.code());
                }
            }
        });
    }

    // TODO: Not Objects!
    public void getAds(final String deviceId, final AdsCallback callback) {
        final String endpoint = String.format("/suggest/%s", deviceId);
        api("GET", endpoint).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Bluejay.oops("%s: %s", endpoint, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    Bluejay.oops("%s: %d", endpoint, response.code());
                    callback.call("wtf");
                } else {
                    JSONArray ads = asJSON(response);
                    if (ads.length() == 0) {
                        callback.call("wtf");
                    } else {
                        JSONArray keywords =
                                ads.optJSONObject(0).optJSONObject("beacon").optJSONArray("keywords");
                        String path = keywords.optString(0) + "_" + keywords.optString(1);
                        callback.call(path);
                    }
                }
            }
        });
    }

    public void trackClick(String beaconId, final String deviceId, final String adUnitId) {
        beaconId = beaconId.replaceAll(":", "").toLowerCase();
        Bluejay.log("trackClick(%s, %s, %s)", beaconId, deviceId, adUnitId);
    }
}
