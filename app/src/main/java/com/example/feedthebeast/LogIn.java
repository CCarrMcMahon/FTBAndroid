package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class LogIn extends AppCompatActivity {
    private TextInputEditText tiet_LogIn_Username, tiet_LogIn_Password;
    private ProgressBar pb_LogIn_ProgressBar;
    private Button btn_LogIn_LogIn;
    private TextView tv_LogIn_NewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        tiet_LogIn_Username = findViewById(R.id.tiet_LogIn_Username);
        tiet_LogIn_Password = findViewById(R.id.tiet_LogIn_Password);

        pb_LogIn_ProgressBar = findViewById(R.id.pb_LogIn_ProgressBar);
        btn_LogIn_LogIn = findViewById(R.id.btn_LogIn_LogIn);
        tv_LogIn_NewUser = findViewById(R.id.tv_LogIn_NewUser);

        btn_LogIn_LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = tiet_LogIn_Username.getText().toString();
                username = username.trim();

                if (!Common.checkUsername(getApplicationContext(), username)) {
                    return;
                }

                String password = tiet_LogIn_Password.getText().toString();

                if (!Common.checkPassword(getApplicationContext(), password)) {
                    return;
                }

                logIn(username, password);
            }
        });

        tv_LogIn_NewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void logIn(String username, String password) {
        pb_LogIn_ProgressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                // Creating array for parameters
                String[] field = new String[2];
                field[0] = "username";
                field[1] = "password";

                // Creating array for data
                String[] data = new String[2];
                data[0] = username;
                data[1] = password;

                PhpHandler phpHandler = new PhpHandler(Common.BASE_URL + "LogIn.php", "POST", field, data);
                phpHandler.sendRequest();

                if (phpHandler.resultReady()) {
                    pb_LogIn_ProgressBar.setVisibility(View.INVISIBLE);
                    String result = phpHandler.getResult();
                    Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);

                    if (result.equals("Successfully logged in.")) {
                        Common.username = username;
                        Intent intent = new Intent(getApplicationContext(), FeederList.class);
                        startActivity(intent);
                        finish();
                    }

                    Log.println(Log.VERBOSE, "PutData", result);
                }
            }
        });
    }
}