package bluejay.sdk;

import android.app.Service;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.IBinder;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AdvertisingService extends Service implements Scanner.Callback {
    public static final String ACTION_START = "bluejay.start";
    public static final String ACTION_STOP = "bluejay.stop";

    public static final long BEACON_EXPIRE_INTERVAL_S = 2L;
    public static final long COLLECTION_INTERVAL = 2000L;

    Cache<String, ScanResult> beacons;
    Scanner scanner;
    Timer collector;
    Client client;

    @Override
    public void onCreate() {
        beacons = CacheBuilder.newBuilder()
                .expireAfterWrite(BEACON_EXPIRE_INTERVAL_S, TimeUnit.SECONDS)
                .build();
        scanner = new Scanner(this, this);
        collector = new Timer();
        client = new Client(this);
    }

    @Override
    public void onDestroy() {
        stop();
    }

    void start() {
        scanner.start();
        collector.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Set<String> beaconIds = ImmutableSet.copyOf(beacons.asMap().keySet());

                for (final String beaconId : beaconIds) {
                    Utils.withAdvertisingId(AdvertisingService.this, new Utils.Callback() {
                        @Override
                        public void call(String advertisingId) {
                            client.collectBeacon(beaconId, advertisingId);
                        }
                    });
                }

                beacons.invalidateAll();
                beacons.cleanUp();
            }
        }, 0, COLLECTION_INTERVAL);
    }

    void stop() {
        scanner.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bluejay.log("onStartCommand: %s", intent);
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    start();
                    break;
                case ACTION_STOP:
                    stop();
                    break;
                default:
                    Bluejay.oops("Unknown action: %s", intent.getAction());
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onResult(ScanResult result) {
        String beaconId = result.getDevice().getAddress();
        beacons.put(beaconId, result);
    }
}
