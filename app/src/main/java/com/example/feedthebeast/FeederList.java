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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeederList extends AppCompatActivity {
    FeederListRVA feederListRVA;

    Toolbar toolbar;
    RecyclerView recyclerView;

    FloatingActionButton fab;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeder_list);

        feederListRVA = new FeederListRVA();

        toolbar = findViewById(R.id.tb_FeederList);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_FeederList);
        recyclerView.setAdapter(feederListRVA);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getFeeders(Common.username);

        fab = findViewById(R.id.fab_FeederList_Add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(getApplicationContext(), BluetoothList.class);
                 startActivity(intent);
            }
        });

        btn = findViewById(R.id.btn_FeederList_Scan);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFeeders(Common.username);
            }
        });
    }

    public void getFeeders(String username) {
        // Creating array for parameters
        String[] field = new String[1];
        field[0] = "username";

        // Creating array for data
        String[] data = new String[1];
        data[0] = username;

        PhpHandler phpHandler = new PhpHandler(Common.FEEDERS_URL, "POST", field, data);

        phpHandler.sendRequest();

        if (phpHandler.resultReady()) {
            String result = phpHandler.getResult();

            String[] result_array = result.split("\n");

            if (result_array[0].equals("No feeders found.")) {
                feederListRVA.names.clear();
            } else {
                feederListRVA.names = Arrays.asList(result_array);
            }

            feederListRVA.notifyDataSetChanged();
        }
    }
}
