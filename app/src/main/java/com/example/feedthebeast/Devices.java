package com.example.feedthebeast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Devices extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    FloatingActionButton fab;

    Button btnStartScanning = null;
    String[] device_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        toolbar = findViewById(R.id.toolbar_Devices);
        recyclerView = findViewById(R.id.recyclerView_Devices);


        setSupportActionBar(toolbar);

        getDevices(Common.username);

        MyAdapter myAdapter = new MyAdapter(device_list);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab = findViewById(R.id.fab_Devices_Add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(getApplicationContext(), Bluetooth.class);
                 startActivity(intent);
            }
        });

        btnStartScanning = findViewById(R.id.btn_Devices_Scan);
        btnStartScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDevices(Common.username);
                myAdapter.names = device_list;
                myAdapter.notifyItemRangeChanged(0, device_list.length);
            }
        });
    }

    public void getDevices(String username) {
        // Creating array for parameters
        String[] field = new String[1];
        field[0] = "username";

        // Creating array for data
        String[] data = new String[1];
        data[0] = username;

        PhpHandler phpHandler = new PhpHandler(Common.DEVICES_URL, "POST", field, data);

        phpHandler.sendRequest();

        if (phpHandler.resultReady()) {
            String result = phpHandler.getResult();
            device_list = result.split("\n");
        }
    }
}
