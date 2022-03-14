package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

// Note: Possibly add RecyclerView so user can select what Wi-Fi SSID they want to connect to
// Also adjust RecyclerView so arguments can be passed in that allow a single use adapter
// This could be done by passing in what view to get and what row to use
public class WiFiDetails extends AppCompatActivity {
    private final static String TAG = WiFiDetails.class.getSimpleName();

    private BluetoothDevice bluetoothDevice;
    private Toolbar tb;
    private TextInputEditText tiet_SSID;
    private TextInputEditText tiet_Password;
    private ProgressBar pb;
    private Button btn_connect;
    private TextView tv_WiFiInfo;

    private final Context context = this;

    private BluetoothLeService bluetoothLeService;
    private String deviceAddress;
    private boolean connected = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
            Log.d(TAG, "Service Disconnected");
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                tv_WiFiInfo.append("\nGATT Connected\n");
                Log.d(TAG, "GATT Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                tv_WiFiInfo.append("\nGATT Disconnected\n");
                Log.d(TAG, "GATT Disconnected");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_details);

        // Create the toolbar and add a back button
        tb = findViewById(R.id.tb_WiFiDetails);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tiet_SSID = findViewById(R.id.tiet_WiFiDetails_SSID);
        tiet_Password = findViewById(R.id.tiet_WiFiDetails_Password);
        pb = findViewById(R.id.pb_WiFiDetails);
        btn_connect = findViewById(R.id.btn_WiFiDetails);
        tv_WiFiInfo = findViewById(R.id.tv_WiFiInfo);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String ip = String.valueOf(wifiInfo.getIpAddress());
        String mac = wifiInfo.getMacAddress();
        String ssid = wifiInfo.getSSID();
        String bssid = wifiInfo.getBSSID();
        String rssi = String.valueOf(wifiInfo.getRssi());

        if (getIntent().hasExtra("device")) {
            bluetoothDevice = getIntent().getParcelableExtra("device");
        }

        String btName = bluetoothDevice.getName();
        deviceAddress = bluetoothDevice.getAddress();

        tv_WiFiInfo.setText(String.format("Bluetooth Device:\nName: %s\nMAC: %s\n\n" +
                "Wi-Fi:\nSSID: %s\nIP: %s\nMAC: %s\nBSSID: %s\nRSSI: %s", btName, deviceAddress,
                ssid, ip, mac, bssid, rssi));

        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        if (bluetoothLeService != null) {
            final boolean result = bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }
}