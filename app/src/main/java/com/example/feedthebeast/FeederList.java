package com.example.feedthebeast;

import android.content.Context;
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
    private final static String TAG = FeederList.class.getSimpleName();
    private final Context context = this;

    FeederListRVA feederListRVA;

    Toolbar toolbar;
    RecyclerView recyclerView;

    FloatingActionButton fab;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeder_list);

        feederListRVA = new FeederListRVA(context);

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
        feederListRVA.clearFeeders();

        // Creating array for parameters
        String[] field = new String[1];
        field[0] = "owner";

        // Creating array for data
        String[] data = new String[1];
        data[0] = username;

        PhpHandler phpHandler = new PhpHandler(Common.FEEDERS_URL, "POST", field, data);

        phpHandler.sendRequest();

        if (phpHandler.resultReady()) {
            String result = phpHandler.getResult();

            // Catches if web server is not running
            if (result.isEmpty()) {
                return;
            }

            if (result.contains("Error: ")) {
                return;
            }

            if (result.equals("No feeders found.")) {
                return;
            }

            String[] result_array = result.split("\n");

            for (int i = 0; i < result_array.length; i += 2) {
                feederListRVA.addToFeeders(result_array[i], result_array[i + 1]);
            }
        }
    }
}
