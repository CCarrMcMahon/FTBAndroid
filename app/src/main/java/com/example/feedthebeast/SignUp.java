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
                email = email.trim();

                if (!Common.checkEmail(getApplicationContext(), email)) {
                    return;
                }

                String username = tiet_SignUp_Username.getText().toString();
                username = username.trim();

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
                // Creating array for parameters
                String[] field = new String[3];
                field[0] = "email";
                field[1] = "username";
                field[2] = "password";

                // Creating array for data
                String[] data = new String[3];
                data[0] = email;
                data[1] = username;
                data[2] = password;

                PhpHandler phpHandler = new PhpHandler(Common.BASE_URL + "SignUp.php", "POST", field, data);
                phpHandler.sendRequest();

                if (phpHandler.resultReady()) {
                    pb_SignUp_ProgressBar.setVisibility(View.INVISIBLE);
                    String result = phpHandler.getResult();
                    Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);

                    if (result.equals("Successfully signed up.")) {
                        // Creating array for parameters
                        String[] field2 = {field[1], field[2]};

                        // Creating array for data
                        String[] data2 = {data[1], data[2]};

                        phpHandler = new PhpHandler(Common.BASE_URL + "LogIn.php", "POST", field2, data2);
                        phpHandler.sendRequest();

                        if (phpHandler.resultReady()) {
                            result = phpHandler.getResult();
                            Common.showMessage(getApplicationContext(), result, Toast.LENGTH_SHORT);
                            Intent intent = null;

                            if (result.equals("Successfully logged in.")) {
                                Common.username = username;
                                intent = new Intent(getApplicationContext(), FeederList.class);
                            } else {
                                intent = new Intent(getApplicationContext(), LogIn.class);
                            }

                            Log.println(Log.VERBOSE, "PutData", result);

                            startActivity(intent);
                            finish();
                        }
                    }

                    Log.println(Log.VERBOSE, "PutData", result);
                }
            }

        });
    }
}