package com.example.feedthebeast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class FeederHome extends AppCompatActivity {
    private final static String TAG = WiFiDetails.class.getSimpleName();
    private final Context context = this;

    private String mac;
    private String name;

    private Toolbar tb;

    private EditText et_Name;
    private Button btn_SaveName;

    private FeederHomeRVA feederHomeRVA;
    private RecyclerView rv_FeederHome;
    private EditText et_StartTime;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private float cups;
    private EditText et_EndTime;
    private EditText et_Cups;
    private Button btn_AddFeedingTime;

    private Button btn_RemoveFeeder;

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

        et_Name = findViewById(R.id.et_FeederName);
        et_Name.setText(name);

        btn_SaveName = findViewById(R.id.btn_FeederHome_Save);
        btn_SaveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });

        feederHomeRVA = new FeederHomeRVA(context);

        rv_FeederHome = findViewById(R.id.rv_FeederHome);
        rv_FeederHome.setAdapter(feederHomeRVA);
        rv_FeederHome.setLayoutManager(new LinearLayoutManager(context));

        et_StartTime = findViewById(R.id.et_FeederHome_StartTime);
        et_StartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeClicked();
            }
        });

        et_EndTime = findViewById(R.id.et_FeederHome_EndTime);
        et_EndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimeClicked();
            }
        });

        et_Cups = findViewById(R.id.et_FeederHome_Cups);
        et_Cups.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String cupsString = et_Cups.getText().toString();

                    if (cupsString.equals("null")) {
                        return;
                    }

                    if (cupsString.isEmpty()) {
                        return;
                    }

                    float newCups = 0.001f;

                    try {
                        newCups = Float.parseFloat(cupsString);

                        if (newCups <= 0.0f) {
                            newCups = 0.001f;
                        } else if (newCups > 8.0f) {
                            newCups = 8.0f;
                        }

                        et_Cups.setText(String.valueOf(newCups));
                        cups = newCups;
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "afterTextChanged: Cups value was invalid.\n", e);
                    }
                }
            }
        });

        btn_AddFeedingTime = findViewById(R.id.btn_FeederHome_Add);
        btn_AddFeedingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFeedingTime();
            }
        });

        btn_RemoveFeeder = findViewById(R.id.btn_FeederHome_Remove);
        btn_RemoveFeeder.setOnClickListener(new View.OnClickListener() {
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
        data[2] = et_Name.getText().toString();

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

    private void startTimeClicked() {
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.clear();

        if (startCalendar != null) {
            int hourOfDay = startCalendar.get(Calendar.HOUR_OF_DAY);
            tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);

            int minute = startCalendar.get(Calendar.MINUTE);
            tempCalendar.set(Calendar.MINUTE, minute);
        }

        int originalHourOfDay = tempCalendar.get(Calendar.HOUR_OF_DAY);
        int originalMinute = tempCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int newHourOfDay, int newMinute) {
                if (startCalendar == null) {
                    startCalendar = Calendar.getInstance();
                    startCalendar.clear();
                }

                startCalendar.set(Calendar.HOUR_OF_DAY, newHourOfDay);
                startCalendar.set(Calendar.MINUTE, newMinute);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                et_StartTime.setText(simpleDateFormat.format(startCalendar.getTime()));
            }
        }, originalHourOfDay, originalMinute, false);

        timePickerDialog.show();
    }

    private void endTimeClicked() {
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.clear();

        if (endCalendar != null) {
            int hourOfDay = endCalendar.get(Calendar.HOUR_OF_DAY);
            tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);

            int minute = endCalendar.get(Calendar.MINUTE);
            tempCalendar.set(Calendar.MINUTE, minute);
        }

        int originalHourOfDay = tempCalendar.get(Calendar.HOUR_OF_DAY);
        int originalMinute = tempCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int newHourOfDay, int newMinute) {
                if (endCalendar == null) {
                    endCalendar = Calendar.getInstance();
                    endCalendar.clear();
                }

                endCalendar.set(Calendar.HOUR_OF_DAY, newHourOfDay);
                endCalendar.set(Calendar.MINUTE, newMinute);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                et_EndTime.setText(simpleDateFormat.format(endCalendar.getTime()));
            }
        }, originalHourOfDay, originalMinute, false);

        timePickerDialog.show();
    }

    private void addFeedingTime() {
        if (startCalendar == null) {
            Common.showMessage(context, "You need to set a start time.", Toast.LENGTH_SHORT);
            return;
        }

        if (endCalendar == null) {
            Common.showMessage(context, "You need to set an end time.", Toast.LENGTH_SHORT);
            return;
        }

        if (et_Cups.getText().toString().isEmpty()) {
            Common.showMessage(context, "You need to set an amount of food to give.", Toast.LENGTH_SHORT);
            return;
        }

        et_Cups.clearFocus();

        // If the end time has already been set, make sure the start time starts before it
        if (startCalendar.compareTo(endCalendar) >= 0) {
            Common.showMessage(context, "Your start time must begin before your end time.", Toast.LENGTH_SHORT);
            return;
        }

        // If the start time has already been set, make sure the end time starts after it
        if (endCalendar.compareTo(startCalendar) <= 0) {
            Common.showMessage(context, "Your end time must begin after your start time.", Toast.LENGTH_SHORT);
            return;
        }

        Calendar newStartCalendar = (Calendar) startCalendar.clone();
        Calendar newEndCalendar = (Calendar) endCalendar.clone();

        feederHomeRVA.addToFeedingTimes(newStartCalendar, newEndCalendar, cups);
    }

    public void removeFeeder() {
        // Creating array for parameters
        String[] field = new String[2];
        field[0] = "mac";
        field[1] = "owner";

        // Creating array for data
        String[] data = new String[2];
        data[0] = mac;
        data[1] = Common.username;

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