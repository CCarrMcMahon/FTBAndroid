package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import java.util.Random;

// Note: Possibly add RecyclerView so user can select what Wi-Fi SSID they want to connect to
// Also adjust RecyclerView so arguments can be passed in that allow a single use adapter
// This could be done by passing in what view to get and what row to use
public class WiFiDetails extends AppCompatActivity {
    private final static String TAG = WiFiDetails.class.getSimpleName();
    private final Context context = this;

    private BluetoothDevice bluetoothDevice;
    private BluetoothLeService bluetoothLeService;
    private String deviceAddress;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic gattCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private int characteristicProperties;

    private Toolbar tb;
    private TextView tv_Title;
    private TextInputEditText tiet_SSID;
    private TextInputEditText tiet_Password;
    private ProgressBar pb;
    private Button btn_WriteData;
    private Button btn_ReadData;
    private TextView tv_Data;
    private TextView tv_WiFiInfo;

    // region Service Handling
    // Callback used to get the status of the BluetoothLeService
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            serviceConnected(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceDisconnected();
        }
    };

    public void serviceConnected(IBinder service) {
        bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

        // Automatically connects to the device upon successful start-up initialization.
        if (!bluetoothLeService.connect(deviceAddress)) {
            finish();
        }
    }

    public void serviceDisconnected() {
        bluetoothLeService = null;
    }
    // endregion

    // region Gatt Updates
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_READ_SUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_WRITE_SUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_CHANGED);
        return intentFilter;
    }

    // Callback used to receive broadcasts from the GATT server
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    gattConnected();
                    break;

                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    gattDisconnected();
                    break;

                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    gattServicesDiscovered();
                    break;

                case BluetoothLeService.ACTION_GATT_READ_SUCCESSFUL:
                    gattReadSuccessful(intent);
                    break;

                case BluetoothLeService.ACTION_GATT_WRITE_SUCCESSFUL:
                    gattWriteSuccessful(intent);
                    break;

                case BluetoothLeService.ACTION_GATT_CHARACTERISTIC_CHANGED:
                    gattCharacteristicChanged(intent);
                    break;

                default:
                    break;
            }
        }
    };

    public void gattConnected() {

    }

    public void gattDisconnected() {

    }

    public void gattServicesDiscovered() {
        gattService = bluetoothLeService.getGattService();

        if (gattService == null) {
            Log.e(TAG, "gattServicesDiscovered: Unable to obtain GattService.");
            return;
        }

        gattCharacteristic = gattService.getCharacteristic(Common.CHARACTERISTIC_UUID);

        if (gattCharacteristic == null) {
            Log.e(TAG, "gattServicesDiscovered: Unable to obtain GattCharacteristic.");
            return;
        }

        characteristicProperties = gattCharacteristic.getProperties();
        bluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
    }

    public void gattReadSuccessful(Intent intent) {
        tv_Data.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
    }

    public void gattWriteSuccessful(Intent intent) {
        tv_Data.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
    }

    public void gattCharacteristicChanged(Intent intent) {
        tv_Data.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
    }
    // endregion

    // region Data Transfer
    private void readData() {
        if (gattService == null) {
            Log.e(TAG, "readData: Unable to obtain GattService.");
            return;
        }

        if (gattCharacteristic == null) {
            Log.e(TAG, "readData: Unable to obtain GattCharacteristic.");
            return;
        }

        if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
            Log.e(TAG, "readData: Characteristic is not readable.");
            return;
        }

        bluetoothLeService.readCharacteristic(gattCharacteristic);
    }

    private void writeData(byte[] data) {
        if (gattService == null) {
            Log.e(TAG, "writeData: Unable to obtain GattService.");
            return;
        }

        if (gattCharacteristic == null) {
            Log.e(TAG, "writeData: Unable to obtain GattCharacteristic.");
            return;
        }

        if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
            if ((characteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
                Log.e(TAG, "writeData: Characteristic is not writeable.");
                return;
            }
        }

        if (gattCharacteristic.setValue(data)) {
            Log.i(TAG, "writeData: Successfully set local characteristic value.");
        } else {
            Log.e(TAG, "writeData: Failed to set local characteristic value.");
            return;
        }

        bluetoothLeService.writeCharacteristic(gattCharacteristic, data);
    }
    // endregion

    // region Process Handling
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_details);

        // Create the toolbar and add a back button
        tb = findViewById(R.id.tb_WiFiDetails);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tv_Title = findViewById(R.id.tv_WiFiDetails_Title);

        tiet_SSID = findViewById(R.id.tiet_WiFiDetails_SSID);
        tiet_Password = findViewById(R.id.tiet_WiFiDetails_Password);
        pb = findViewById(R.id.pb_WiFiDetails);
        btn_WriteData = findViewById(R.id.btn_WriteData);
        btn_ReadData = findViewById(R.id.btn_ReadData);
        tv_Data = findViewById(R.id.tv_Data);
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

        Random random = new Random();
        byte[] bytes = new byte[10];

        btn_WriteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pb.setVisibility(View.VISIBLE);
                random.nextBytes(bytes);
                writeData(bytes);
            }
        });

        btn_ReadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pb.setVisibility(View.VISIBLE);
                readData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        if (bluetoothLeService != null) {
            boolean result = bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, String.format("Connection to BluetoothLeService Reestablished: %b", result));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothLeService.disconnect();
        unbindService(serviceConnection);
        bluetoothLeService = null;
    }
    // endregion
}