package com.example.feedthebeast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothList extends AppCompatActivity {
    private static final String UNSUPPORTED_BLUETOOTH = "This device does not support bluetooth and is required.";
    private static final String DENIED_BLUETOOTH = "Please enable bluetooth to use this app.";
    private static final String DENIED_LOCATION = "Please enable location to use this app.";
    private static final String FAILED_SCAN = "Failed to start scanning.";
    private static final String DISCOVERY_START = "Started Discovery";
    private static final String DISCOVERY_END = "Finished Discovery";

    private static final int REQUEST_ENABLE_LOCATION = 0;

    public static final Pattern VALID_DEVICE_REGEX = Pattern.compile("(DSD) .+");
    // public static final Pattern VALID_DEVICE_REGEX = Pattern.compile(".*");

    BluetoothAdapter bluetoothAdapter = null;
    Context context = this;

    BluetoothListRVA bluetoothListRVA;

    Toolbar toolbar;
    RecyclerView recyclerView;

    // Activity request to handle enabling Bluetooth
    ActivityResultLauncher<Intent> requestBluetoothEnable = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    Common.showMessage(context, DENIED_BLUETOOTH, Toast.LENGTH_SHORT);
                    Intent intent = new Intent(getApplicationContext(), FeederList.class);
                    startActivity(intent);
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

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

        // Users need to allow access to location for Bluetooth
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(discovery_receiver, filter);

        bluetoothListRVA = new BluetoothListRVA();

        toolbar = findViewById(R.id.tb_BluetoothList);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_BluetoothList);
        recyclerView.setAdapter(bluetoothListRVA);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (!bluetoothAdapter.startDiscovery()) {
            Common.showMessage(context, FAILED_SCAN, Toast.LENGTH_SHORT);
        }
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

                    Common.showMessage(context, DENIED_LOCATION, Toast.LENGTH_SHORT);
                    Intent intent = new Intent(getApplicationContext(), FeederList.class);
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
                    Common.showMessage(context, DISCOVERY_START, Toast.LENGTH_SHORT);
                    bluetoothListRVA.names.clear();

//                    // Get a list of paired devices
//                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//                    if (pairedDevices.size() > 0) {
//                        // There are paired devices. Get the name and address of each paired device.
//                        for (BluetoothDevice device : pairedDevices) {
//                            String deviceName = device.getName();
//                            String deviceAddress = device.getAddress(); // MAC address
//
//                            bluetoothListRVA.names.add(deviceName + "\n" + deviceAddress);
//                            bluetoothListRVA.notifyDataSetChanged();
//                        }
//                    }

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
                        bluetoothListRVA.names.add(deviceName + "\n" + deviceAddress);
                        bluetoothListRVA.notifyDataSetChanged();
                    }

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Common.showMessage(context, DISCOVERY_END, Toast.LENGTH_SHORT);
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