package bluejay.sdk;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

public abstract class Utils {

    interface Callback {
        void call(String advertisingId);
    }

    static String getAdvertisingId(Context ctx) {
        try {
            final String advertisingId = AdvertisingIdClient.getAdvertisingIdInfo(ctx).getId();
            Bluejay.log("Got advertising ID: %s", advertisingId);
            return advertisingId;
        } catch (Throwable t) {
            Bluejay.oops("Failed to retrieve advertising info: %s", t);
            // TODO: Is this safe?
            return null;
        }
    }

    static void withAdvertisingId(final Context ctx, final Callback callback) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return getAdvertisingId(ctx);
            }

            @Override
            protected void onPostExecute(String advertisingId) {
                callback.call(advertisingId);
            }
        };
        task.execute();
    }
}
