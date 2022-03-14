package com.example.feedthebeast;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BTConnectThread extends Thread {
    private static final UUID MY_UUID = UUID.randomUUID();
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public BTConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("BTConnectThread", "Socket's create() method failed", e);
        }

        mmSocket = tmp;
        Log.d("BTConnectThread", "Socket created");
    }

    public void run() {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("BTConnectThread", "Could not close the client socket", closeException);
            }
            Log.d("BTConnectThread", "Couldn't connect to socket", connectException);

            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        BTConnectedThread btConnectedThread = new BTConnectedThread(mmSocket);
        btConnectedThread.start();
        Log.d("BTConnectThread", "ConnectedThread created");

        while (btConnectedThread.isAlive()) {

        }

        Log.d("BTConnectThread", "ConnectedThread closed");

        cancel();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("BTConnectThread", "Could not close the client socket", e);
        }
    }
}