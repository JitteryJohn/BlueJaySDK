package bluejay.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.gms.ads.AdView;

public class Bluejay {
    public static final String TAG = Bluejay.class.getName();
    public static final int REQUEST_PERMISSIONS_CODE = 12345678;
    public static final String[] PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    static void log(String message, Object...args) {
        Log.i(TAG, String.format(message, args));
    }

    static void oops(String message, Object...args) {
        Log.e(TAG, String.format(message, args));
    }

    static boolean hasPermissions(Context ctx) {
        for (String permission : PERMISSIONS) {
            if (ctx.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    static void constructAd(final View oldAd, final String adUrl) {
        oldAd.post(new Runnable() {
            @Override
            public void run() {
                // Get layout information from the existing ads view
                final int width = oldAd.getWidth();
                final int height = oldAd.getHeight();
                final float x = oldAd.getX();
                final float y = oldAd.getY();
                final ViewGroup parent = (ViewGroup) oldAd.getParent();
                final int childIndex = parent.indexOfChild(oldAd);

                // Remove the old view
                parent.removeViewAt(childIndex);

                // Inject the new ads view
                final WebView webView = constructWebView(oldAd.getContext(), adUrl, x, y, width, height);
                parent.addView(webView, childIndex);
            }
        });
    }

    static WebView constructWebView(Context ctx, String adUrl, float x, float y, int width, int height) {
        final WebView webView = new WebView(ctx);
        webView.setX(x);
        webView.setY(y);
        webView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(adUrl);

        webView.setBackgroundColor(Color.TRANSPARENT);

        return webView;
    }

    private final Activity ctx;
    private final Client client;

    public Bluejay(Activity activity) {
        ctx = activity;
        client = new Client(ctx);
    }

    public void initialize() {
        if (!hasPermissions(ctx)) {
            ctx.requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        } else {
            log("Permissions all there! Starting service!");
            final Intent startScanning = new Intent()
                    .setAction(AdvertisingService.ACTION_START)
                    .setClass(ctx, AdvertisingService.class);
            ctx.startService(startScanning);
        }
    }

    public void loadAd(final AdView into) {
        // TODO: Incorporate API call and service interaction
        Utils.withAdvertisingId(ctx, new Utils.Callback() {
            @Override
            public void call(String advertisingId) {
                client.getAds(advertisingId, new Client.AdsCallback() {
                    @Override
                    public void call(String adUrl) {
                        constructAd(into, "http://52.53.183.113:8080/" + adUrl + ".html");
                    }
                });
            }
        });
    }
}
