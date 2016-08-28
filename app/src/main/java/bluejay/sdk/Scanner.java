package bluejay.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class Scanner {

    static final int MIN_RSSI = -69;

    interface Callback {
        void onResult(ScanResult result);
    }

    final Context ctx;
    final BluetoothManager btManager;
    final BluetoothAdapter btAdapter;
    final BluetoothLeScanner bleScanner;
    final AtomicBoolean isScanning;
    final List<ScanFilter> scanFilters;
    final ScanSettings scanSettings;
    final ScanCallback scanCallback;
    final Callback callback;

    public Scanner(Context activity, Callback cbk) {
        ctx = activity;
        btManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        bleScanner = btAdapter.getBluetoothLeScanner();
        isScanning = new AtomicBoolean(false);
        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setDeviceName("estimote").build());
        scanSettings = new ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Scanner.this.onScanResult(callbackType, result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Scanner.this.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Bluejay.oops("BLE scan failed: %d", errorCode);
            }
        };
        callback = cbk;
    }

    public void start() {
        if (isScanning.compareAndSet(false, true)) {
            Bluejay.log("Starting scan...");
            bleScanner.startScan(scanFilters, scanSettings, scanCallback);
        } else {
            Bluejay.oops("Can't start scan, already scanning!");
        }
    }

    public void stop() {
        if (isScanning.compareAndSet(true, false)) {
            Bluejay.log("Stopping scan...");
            bleScanner.stopScan(scanCallback);
        }
    }

    public void onScanResult(int callbackType, ScanResult result) {
        // TODO: Remove callbackType if it's not useful
        final String address = result.getDevice().getAddress();
        if (result.getRssi() < MIN_RSSI) {
            Bluejay.log("Beacon [%s] is out of range: %d", address, result.getRssi());
        } else {
            Bluejay.log("Found beacon [%s]", address);
            callback.onResult(result);
        }
    }

    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            onScanResult(-1, result);
        }
    }


}
