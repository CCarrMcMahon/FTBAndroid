package com.example.feedthebeast;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final Binder binder = new LocalBinder();
    private byte[] valueToWrite;

    public final static String ACTION_GATT_CONNECTED = "com.example.feedthebeast.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.feedthebeast.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.feedthebeast.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_DATA_AVAILABLE = "com.example.feedthebeast.ACTION_GATT_DATA_AVAILABLE";
    public static final String ACTION_GATT_WRITE_SUCCESSFUL = "com.example.feedthebeast.ACTION_GATT_WRITE_SUCCESSFUL";
    public static final String EXTRA_DATA = "com.example.feedthebeast.EXTRA_DATA";

    public BluetoothLeService() {

    }

    // Class to access object as remote service
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    // Return object on service bind
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Method to broadcast actions to a receiver
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // Method to broadcast characteristics to a receiver
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);

            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }

            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }

        sendBroadcast(intent);
    }

    // Callback used to determine what actions should be broadcast
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        // Runs when the connection to the GATT server changes
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Successfully connected to the GATT Server
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Connected to the GATT server.");

                // Attempt to discover services after successful connection
                boolean isDiscoveringServices = bluetoothGatt.discoverServices();
                Log.i(TAG, String.format("Attempting to discover services: %b", isDiscoveringServices));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the GATT Server
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                Log.i(TAG, "Disconnected from the GATT server.");
            }
        }

        // Runs after a GATT service scan is requested
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(TAG, "Successfully discovered services.");
            } else {
                Log.w(TAG, String.format("Failed to discover services: %d", status));
            }
        }

        // Runs when a characteristic is read
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_DATA_AVAILABLE, characteristic);
                Log.i(TAG, "Successfully read characteristic.");
            } else {
                Log.w(TAG, String.format("Failed to reach characteristic: %d", status));
            }
        }

        // Runs what a characteristic is written to
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.getValue() == valueToWrite) {
                broadcastUpdate(ACTION_GATT_WRITE_SUCCESSFUL);
                Log.i(TAG, "Successfully wrote to characteristic.");
            } else {
                Log.w(TAG, String.format("Failed to write to characteristic: %d", status));
            }
        }

        // Runs when a characteristic changes
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_GATT_DATA_AVAILABLE, characteristic);
        }
    };

    // Checks to see if a bluetooth adapter or gatt connection is created
    public boolean isCreated() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "Either the Bluetooth Adapter object or the Gatt Connection hasn't been created.");
            return false;
        }

        return true;
    }

    public List<BluetoothGattService> getSupportedGattService() {
        if (!isCreated()) {
            return null;
        }

        return bluetoothGatt.getServices();
    }

    // Reads a characteristic
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!isCreated()) {
            return;
        }

        bluetoothGatt.readCharacteristic(characteristic);
    }

    // Writes a characteristic
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (!isCreated()) {
            return;
        }

        valueToWrite = value;

        characteristic.setValue(valueToWrite);
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (!isCreated()) {
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Failed to created Bluetooth Adapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            return false;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Unable to connect to the Bluetooth device as the address provided is invalid.");
            return false;
        }
    }

    public void disconnect() {
        if (!isCreated()) {
            return;
        }

        bluetoothGatt.disconnect();
        Log.i(TAG, "GATT server disconnected.");
    }

    private void close() {
        if (!isCreated()) {
            return;
        }

        bluetoothGatt.close();
        bluetoothGatt = null;
        Log.i(TAG, "GATT server closed.");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }
}