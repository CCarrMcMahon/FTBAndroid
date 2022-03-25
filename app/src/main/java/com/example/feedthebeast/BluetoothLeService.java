package com.example.feedthebeast;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private final Binder binder = new LocalBinder();
    private byte[] valueToWrite;

    public static final String ACTION_GATT_CONNECTED = "com.example.feedthebeast.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.example.feedthebeast.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.example.feedthebeast.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_GATT_READ_SUCCESSFUL = "com.example.feedthebeast.ACTION_GATT_READ_SUCCESSFUL";
    public static final String ACTION_GATT_WRITE_SUCCESSFUL = "com.example.feedthebeast.ACTION_GATT_WRITE_SUCCESSFUL";
    public static final String ACTION_GATT_CHARACTERISTIC_CHANGED = "com.example.feedthebeast.ACTION_GATT_CHARACTERISTIC_CHANGED";
    public static final String EXTRA_DATA = "com.example.feedthebeast.EXTRA_DATA";

    // region Creation
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
    // endregion

    // region Broadcasting
    // Method to broadcast actions to a receiver
    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // Method to broadcast characteristics to a receiver
    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        byte[] data = characteristic.getValue();

        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
        }

        sendBroadcast(intent);
    }

    private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        // The connection is killed by the system, no need to gently disconnect
                        bluetoothGatt.disconnect();
                    }
                    // Calling close() will prevent the STATE_OFF event from being logged (this receiver will be unregistered). But it doesn't matter.
                    close();
                    break;
            }
        }
    };

    // Callback used to determine what actions should be broadcast
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        // Runs when the connection to the GATT server changes
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Successfully connected to the GATT Server
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Connected to the GATT server.");

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BluetoothAdapter.EXTRA_STATE);
                intentFilter.addAction(BluetoothAdapter.EXTRA_PREVIOUS_STATE);

                registerReceiver(bluetoothStateBroadcastReceiver, intentFilter);

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
                broadcastUpdate(ACTION_GATT_READ_SUCCESSFUL, characteristic);
                Log.i(TAG, "onCharacteristicRead: Successfully read characteristic.");
            } else {
                Log.e(TAG, String.format("onCharacteristicRead: Failed to read characteristic: %d", status));
            }
        }

        // Runs what a characteristic is written to
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.getValue() == valueToWrite) {
                broadcastUpdate(ACTION_GATT_WRITE_SUCCESSFUL, characteristic);
                Log.i(TAG, "onCharacteristicWrite: Successfully wrote to the characteristic.");
            } else {
                Log.e(TAG, String.format("onCharacteristicWrite: Failed to write to the characteristic: %d", status));
            }
        }

        // Runs when a characteristic changes
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_GATT_CHARACTERISTIC_CHANGED, characteristic);
            Log.i(TAG, "onCharacteristicChanged: Characteristic changed.");
        }
    };
    // endregion

    // region Functionality
    // Checks to see if the bluetooth adapter or gatt connection has not been created yet
    public boolean notCreated() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "notCreated: The bluetoothAdapter object is null.");
            return true;
        }

        if (bluetoothGatt == null) {
            Log.e(TAG, "notCreated: The bluetoothGatt object is null.");
            return true;
        }

        return false;
    }

    public BluetoothGattService getGattService() {
        if (notCreated()) {
            return null;
        }

        return bluetoothGatt.getService(Common.SERVICE_UUID);
    }

    // Reads a characteristic
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (notCreated()) {
            return;
        }

        if (bluetoothGatt.readCharacteristic(characteristic)) {
            Log.i(TAG, "readCharacteristic: Successfully started read operation.");
        } else {
            Log.e(TAG, "readCharacteristic: Failed to start read operation.");
        }
    }

    // Writes a characteristic
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (notCreated()) {
            return;
        }

        valueToWrite = value;

        if (bluetoothGatt.writeCharacteristic(characteristic)) {
            Log.i(TAG, "writeCharacteristic: Successfully started write operation.");
        } else {
            Log.e(TAG, "writeCharacteristic: Failed to start write operation.");
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (notCreated()) {
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }
    // endregion

    // region Connections
    public boolean connect(Context context, String address) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "connect: The BluetoothAdapter object is null.");
            return false;
        }

        if (address == null) {
            Log.e(TAG, "connect: The address provided is null.");
            return false;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
            Log.i(TAG, "connect: Connected to BluetoothLeService.");
            return true;
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "connect: Unable to connect to the Bluetooth device as the address provided is invalid.");
            return false;
        }
    }

    public void disconnect() {
        if (notCreated()) {
            return;
        }

        bluetoothGatt.disconnect();
        Log.i(TAG, "disconnect: GATT server disconnected.");
    }

    private void close() {
        if (notCreated()) {
            return;
        }

        disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
        Log.i(TAG, "close: GATT server closed.");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        unregisterReceiver(bluetoothStateBroadcastReceiver);
        return super.onUnbind(intent);
    }
    // endregion
}