package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
    private BluetoothDevice bluetoothDevice;
    private Toolbar tb;
    private TextInputEditText tiet_SSID;
    private TextInputEditText tiet_Password;
    private ProgressBar pb;
    private Button btn_connect;
    private TextView tv_WiFiInfo;


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
        String btMAC = bluetoothDevice.getAddress();

        tv_WiFiInfo.setText(String.format("Bluetooth Device:\nName: %s\nMAC: %s\n\n" +
                "Wi-Fi:\nSSID: %s\nIP: %s\nMAC: %s\nBSSID: %s\nRSSI: %s", btName, btMAC,
                ssid, ip, mac, bssid, rssi));

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);



            }
        });





    }
}