package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class FeederHome extends AppCompatActivity {
    private final static String TAG = WiFiDetails.class.getSimpleName();
    private final Context context = this;

    private String mac;
    private String name;

    private Toolbar tb;
    private EditText et_name;
    private Button btn_save;
    private Button btn_remove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeder_home);

        mac = this.getIntent().getStringExtra("mac");
        name = this.getIntent().getStringExtra("name");

        tb = findViewById(R.id.tb_FeederHome);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        et_name = findViewById(R.id.et_FeederName);
        et_name.setText(name);

        btn_save = findViewById(R.id.btn_FeederHome_Save);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });

        btn_remove = findViewById(R.id.btn_FeederHome_Remove);

        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFeeder();
            }
        });
    }

    private void saveName() {
        // Creating array for parameters
        String[] field = new String[3];
        field[0] = "mac";
        field[1] = "owner";
        field[2] = "name";

        // Creating array for data
        String[] data = new String[3];
        data[0] = mac;
        data[1] = Common.username;
        data[2] = et_name.getText().toString();

        if (data[2] == null || data[2].isEmpty()) {
            Common.showMessage(context, "Please enter a name.", Toast.LENGTH_SHORT);
            return;
        }

        PhpHandler phpHandler = new PhpHandler(Common.EDIT_NAME_URL, "POST", field, data);
        phpHandler.sendRequest();

        if (phpHandler.resultReady()) {
            String result = phpHandler.getResult();
            Common.showMessage(context, result, Toast.LENGTH_SHORT);
            name = data[2];
        }
    }

    public void removeFeeder() {
        // Creating array for parameters
        String[] field = new String[3];
        field[0] = "mac";
        field[1] = "owner";
        field[2] = "name";

        // Creating array for data
        String[] data = new String[3];
        data[0] = mac;
        data[1] = Common.username;
        data[2] = name;

        PhpHandler phpHandler = new PhpHandler(Common.REMOVE_FEEDER_URL, "POST", field, data);
        phpHandler.sendRequest();

        if (phpHandler.resultReady()) {
            String result = phpHandler.getResult();
            Common.showMessage(context, result, Toast.LENGTH_SHORT);

            if (result.equals("Successfully removed the feeder.")) {
                Intent intent = new Intent(context, FeederList.class);
                startActivity(intent);
                finish();
            }
        }
    }
}