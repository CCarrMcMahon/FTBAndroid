package com.example.feedthebeast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.companion.DeviceFilter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bluetooth extends AppCompatActivity {
    private static final String UNSUPPORTED_BLUETOOTH = "This device does not support bluetooth and is required.";
    private static final String DENIED_BLUETOOTH = "Please enable bluetooth to use this app.";
    private static final String DENIED_LOCATION = "Please enable location to use this app.";

    private static final int REQUEST_ENABLE_LOCATION = 0;

    public static final Pattern VALID_DEVICE_REGEX = Pattern.compile("(DSD) .+");

    BluetoothAdapter bluetoothAdapter = null;
    Context context = this;

    StringBuilder info = new StringBuilder();
    TextView tv_Bluetooth_Devices;

    // Activity request to handle enabling bluetooth
    ActivityResultLauncher<Intent> requestBluetoothEnable = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    Common.showMessage(context, DENIED_BLUETOOTH, Toast.LENGTH_SHORT);
                    Intent intent = new Intent(getApplicationContext(), Devices.class);
                    startActivity(intent);
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Select the Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Check if the device has Bluetooth and notify the user if they don't have it.
        // This should not occur since Bluetooth is required in the Manifest file.
        if (bluetoothAdapter == null) {
            Common.showMessage(context, UNSUPPORTED_BLUETOOTH, Toast.LENGTH_SHORT);
            finishAndRemoveTask();
            System.exit(0);
        }

        // Make sure Bluetooth is enabled and ask the user to enable it if not
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestBluetoothEnable.launch(enableBluetoothIntent);
        }

        // Users need to allow access to location for bluetooth
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(discovery_receiver, filter);

//        // Get a list of paired devices
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//
//                info.append(deviceName).append(" | ").append(deviceHardwareAddress).append("\n");
//            }
//
//            viewInfo.setText(info);
//        }

        tv_Bluetooth_Devices = findViewById(R.id.tv_Bluetooth_Devices);

        if (!bluetoothAdapter.startDiscovery()) {
            info.append("Failed to start scanning\n");
            tv_Bluetooth_Devices.setText(info);
        }

//        if (bluetoothAdapter.isDiscovering()) {
//            bluetoothAdapter.cancelDiscovery();
//            info.append("Was Scanning\n");
//            tv_Bluetooth_Devices.setText(info);
//        }
//
//        if (!bluetoothAdapter.startDiscovery()) {
//            info.append("Failed to start scanning\n");
//            tv_Bluetooth_Devices.setText(info);
//        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] { permission }, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch (requestCode) {
                // Location request
                case REQUEST_ENABLE_LOCATION:
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }

                    Toast.makeText(context, DENIED_LOCATION, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Devices.class);
                    startActivity(intent);
                    finish();
                    return;

                default:
                    return;
            }
        } catch (Exception e) {
            Log.e("Request to enable permission threw exception...\n", e.toString());
            e.printStackTrace();
            finish();
        }
    }

    // Create a BroadcastReceiver for discovery mode
    private final BroadcastReceiver discovery_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    info.append("Started Discovery\n");
                    tv_Bluetooth_Devices.setText(info);
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress(); // MAC address

                    if (deviceName == null) {
                        deviceName = "NULL";
                    }

                    if (deviceAddress == null) {
                        deviceAddress = "NULL";
                    }

                    Matcher matcher = VALID_DEVICE_REGEX.matcher(deviceName);

                    if (matcher.matches()) {
                        info.append(deviceName).append(" | ").append(deviceAddress).append("\n");
                        tv_Bluetooth_Devices.setText(info);
                    }

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    info.append("Finished Discovery\n");
                    tv_Bluetooth_Devices.setText(info);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the discovery_receiver.
        unregisterReceiver(discovery_receiver);

        bluetoothAdapter.cancelDiscovery();
    }
}