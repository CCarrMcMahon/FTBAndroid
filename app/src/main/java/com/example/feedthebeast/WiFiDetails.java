package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
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
    private String deviceName;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic gattCharacteristic;
    private int characteristicProperties;

    private Toolbar tb;
    private TextView tv_Title;
    private TextInputEditText tiet_SSID;
    private TextInputEditText tiet_Password;
    private ProgressBar pb;
    private Button btn_Connect;
    private TextView tv_WiFiInfo;

    private Queue<Byte> dataQueue = new LinkedList<>();
    private List<Byte> dataList = new ArrayList<>();
    private boolean characteristicChanged = false;
    private String characteristicString = "";
    private boolean writingData = false;
    private boolean connecting = false;

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
        if (!bluetoothLeService.connect(context, deviceAddress)) {
            finish();
        }
    }

    public void serviceDisconnected() {
        Common.showMessage(context, "Service disconnected.", Toast.LENGTH_SHORT);
        bluetoothLeService = null;
        finish();
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
        Common.showMessage(context, "Gatt disconnected.", Toast.LENGTH_SHORT);
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (bluetoothLeService != null) {
            bluetoothLeService.connect(context, deviceAddress);
        }
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
        // tv_Data.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
    }

    public void gattWriteSuccessful(Intent intent) {
        sendData();
    }

    public void gattCharacteristicChanged(Intent intent) {
        characteristicString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        characteristicChanged = true;

        if (characteristicString.equals("SUCCESS")) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // Creating array for parameters
                    String[] field = new String[3];
                    field[0] = "mac";
                    field[1] = "owner";
                    field[2] = "name";

                    // Creating array for data
                    String[] data = new String[3];
                    data[0] = deviceAddress;
                    data[1] = Common.username;
                    data[2] = deviceName;

                    PhpHandler phpHandler = new PhpHandler(Common.ADD_FEEDER_URL, "POST", field, data);
                    phpHandler.sendRequest();

                    if (phpHandler.resultReady()) {
                        String result = phpHandler.getResult();

                        switch (result) {
                            case "Failed to get your feeders.":
                            case "You already own this feeder.":
                            case "Successfully added the feeder.":
                            case "Failed to add the feeder.":
                                Common.showMessage(context, result, Toast.LENGTH_SHORT);
                                Intent intent = new Intent(context, FeederList.class);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                Common.showMessage(context, "Unable to connect to the website, try again.", Toast.LENGTH_SHORT);
                                break;
                        }
                    }
                }
            });
        } else {
            Common.showMessage(context, "Unable to connect to the internet, try again.", Toast.LENGTH_SHORT);
        }

        pb.setVisibility(View.INVISIBLE);
        connecting = false;
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

        for (byte value : data) {
            dataQueue.add(value);
        }

        writingData = true;

        sendData();
    }

    private void sendData() {
        dataList.clear();

        for (int i = 0; i < 20; i++) {
            if (dataQueue.peek() == null) {
                if (i == 0) {
                    writingData = false;
                    return;
                }

                break;
            }

            dataList.add(dataQueue.remove());
        }

        byte[] bytes = new byte[dataList.size()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = dataList.get(i);
        }
        
        if (gattCharacteristic.setValue(bytes)) {
            Log.i(TAG, "writeData: Successfully set local characteristic value.");
        } else {
            Log.e(TAG, "writeData: Failed to set local characteristic value.");
            return;
        }

        bluetoothLeService.writeCharacteristic(gattCharacteristic, bytes);
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
        btn_Connect = findViewById(R.id.btn_WiFiDetails);
        tv_WiFiInfo = findViewById(R.id.tv_WiFiInfo);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String ssid = wifiInfo.getSSID();

        if (getIntent().hasExtra("device")) {
            bluetoothDevice = getIntent().getParcelableExtra("device");
        }

        deviceName = bluetoothDevice.getName();
        deviceAddress = bluetoothDevice.getAddress();

        tv_WiFiInfo.setText(String.format("Bluetooth Device:\nName: %s\nMAC: %s\n\nWi-Fi:\nSSID: %s", deviceName, deviceAddress, ssid));

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connecting) {
                    Common.showMessage(context, "Connection in progress, please wait.", Toast.LENGTH_SHORT);
                    return;
                }

                String packetID = "connect";

                String ssidString = tiet_SSID.getText().toString();
                String passwordString = tiet_Password.getText().toString();
                String macString = deviceAddress;

                if (ssidString.equals("null") || ssidString.equals("")) {
                    Common.showMessage(context, "Please enter an SSID.", Toast.LENGTH_SHORT);
                    return;
                }

                if (passwordString.equals("null") || passwordString.equals("")) {
                    Common.showMessage(context, "Please enter a Password.", Toast.LENGTH_SHORT);
                    return;
                }

                if (deviceAddress.equals("null") || deviceAddress.equals("")) {
                    Common.showMessage(context, "Error: MAC address not detected.", Toast.LENGTH_SHORT);
                    return;
                }

                connecting = true;
                pb.setVisibility(View.VISIBLE);

                String dataString = "id:" + packetID + "&ssid:" + ssidString + "&password:" + passwordString + "&mac:" + macString;
                writeData(dataString.getBytes());

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!characteristicChanged) {
                            Common.showMessage(context, "No response from the feeder.", Toast.LENGTH_SHORT);
                        }

                        characteristicChanged = false;
                        writingData = false;
                        connecting = false;
                        pb.setVisibility(View.INVISIBLE);
                    }
                }, 20000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        if (bluetoothLeService != null) {
            bluetoothLeService.connect(context, deviceAddress);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bluetoothLeService = null;
    }
    // endregion
}