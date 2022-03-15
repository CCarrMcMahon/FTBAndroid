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
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private TextView tv_WiFiDetails_Title;

    private final Context context = this;

    private BluetoothLeService bluetoothLeService;
    private String deviceAddress;
    private List<BluetoothGattService> gattServices;
    private List<List<BluetoothGattCharacteristic>> gattCharacteristics;
    private BluetoothGattCharacteristic notifyCharacteristic;

    private ExpandableListView gattServicesList;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_WRITE_SUCCESSFUL);
        return intentFilter;
    }

    // Callback used to get the status of the BluetoothLeService
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!bluetoothLeService.initialize()) {
                finish();
            }

            // Automatically connects to the device upon successful start-up initialization.
            if (bluetoothLeService.connect(deviceAddress)) {
                Log.i(TAG, "Connected to BluetoothLeService.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
            Log.i(TAG, "Disconnected from BluetoothLeService.");
        }
    };

    // Callback used to receive broadcasts from the GATT server
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED:

                    break;

                case BluetoothLeService.ACTION_GATT_DISCONNECTED:

                    break;

                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    gattServices = bluetoothLeService.getSupportedGattService();
                    break;

                case BluetoothLeService.ACTION_GATT_DATA_AVAILABLE:
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    break;

                case BluetoothLeService.ACTION_GATT_WRITE_SUCCESSFUL:
                    break;

                default:
                    break;
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            tv_WiFiDetails_Title.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices() {
        if (gattServices == null) {
            return;
        }

        String uuid = null;

        String unknownServiceString = "Unknown Service";
        String unknownCharaString = "Unknown Characteristic";

        List<HashMap<String, String>> gattServiceData = new ArrayList<>();
        List<List<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();

        gattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();

            uuid = gattService.getUuid().toString();

            currentServiceData.put("NAME", unknownServiceString);
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);

            List<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattServiceCharacteristics = gattService.getCharacteristics();
            List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattServiceCharacteristic : gattServiceCharacteristics) {
                characteristics.add(gattServiceCharacteristic);

                HashMap<String, String> currentCharacteristicData = new HashMap<String, String>();

                uuid = gattServiceCharacteristic.getUuid().toString();

                currentCharacteristicData.put("NAME", unknownCharaString);
                currentCharacteristicData.put("UUID", uuid);
                gattCharacteristicGroupData.add(currentCharacteristicData);
            }

            gattCharacteristics.add(characteristics);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {"NAME", "UUID"},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {"NAME", "UUID"},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );

        gattServicesList.setAdapter(gattServiceAdapter);
    }

    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    if (gattCharacteristics != null) {
                        BluetoothGattCharacteristic characteristic = gattCharacteristics.get(groupPosition).get(childPosition);
                        int charaProp = characteristic.getProperties();

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (notifyCharacteristic != null) {
                                bluetoothLeService.setCharacteristicNotification(notifyCharacteristic, false);
                                notifyCharacteristic = null;
                            }

                            bluetoothLeService.readCharacteristic(characteristic);
                        }

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            notifyCharacteristic = characteristic;
                            bluetoothLeService.setCharacteristicNotification(characteristic, true);
                        }

                        return true;
                    }

                    return false;
                }
            };

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

        gattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        gattServicesList.setOnChildClickListener(servicesListClickListener);

        tv_WiFiDetails_Title = findViewById(R.id.tv_WiFiDetails_Title);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pb.setVisibility(View.VISIBLE);
                displayGattServices();
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
        unbindService(serviceConnection);
        bluetoothLeService = null;
    }
}