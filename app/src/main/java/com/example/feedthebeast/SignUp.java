package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class SignUp extends AppCompatActivity {
    private TextInputEditText tiet_SignUp_Email, tiet_SignUp_Username, tiet_SignUp_Password;
    private ProgressBar pb_SignUp_ProgressBar;
    private Button btn_SignUp_SignUp;
    private TextView tv_SignUp_ExistingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        tiet_SignUp_Email = findViewById(R.id.tiet_SignUp_Email);
        tiet_SignUp_Username = findViewById(R.id.tiet_SignUp_Username);
        tiet_SignUp_Password = findViewById(R.id.tiet_SignUp_Password);

        pb_SignUp_ProgressBar = findViewById(R.id.pb_SignUp_ProgressBar);
        btn_SignUp_SignUp = findViewById(R.id.btn_SignUp_SignUp);
        tv_SignUp_ExistingUser = findViewById(R.id.tv_SignUp_ExistingUser);

        btn_SignUp_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = tiet_SignUp_Email.getText().toString();

                if (!Common.checkEmail(getApplicationContext(), email)) {
                    return;
                }

                String username = tiet_SignUp_Username.getText().toString();

                if (!Common.checkUsername(getApplicationContext(), username)) {
                    return;
                }

                String password = tiet_SignUp_Password.getText().toString();

                if (!Common.checkPassword(getApplicationContext(), password)) {
                    return;
                }

                signUp(email, username, password);
            }
        });

        tv_SignUp_ExistingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void signUp(String email, String username, String password) {
        pb_SignUp_ProgressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                //Starting Write and Read data with URL
                //Creating array for parameters
                String[] field = new String[3];
                field[0] = "email";
                field[1] = "username";
                field[2] = "password";

                //Creating array for data
                String[] data = new String[3];
                data[0] = email;
                data[1] = username;
                data[2] = password;

                PutData putData = new PutData(Common.BASE_URL + "SignUp.php", "POST", field, data);

                if (putData.startPut()) {
                    if (putData.onComplete()) {
                        pb_SignUp_ProgressBar.setVisibility(View.INVISIBLE);
                        String result = putData.getResult();

                        if (result.equals("Successfully signed up.")) {
                            Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);

                            //Starting Write and Read data with URL
                            //Creating array for parameters
                            String[] field2 = {field[1], field[2]};

                            //Creating array for data
                            String[] data2 = {data[1], data[2]};

                            putData = new PutData(Common.BASE_URL + "LogIn.php", "POST", field2, data2);

                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    result = putData.getResult();

                                    if (result.equals("Successfully logged in.")) {
                                        Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);
                                        Intent intent = new Intent(getApplicationContext(), LogIn.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    Log.i("PutData", result);
                                }
                            }
                        } else {
                            Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);
                        }

                        Log.i("PutData", result);
                    }
                }
            }
        });
    }
}