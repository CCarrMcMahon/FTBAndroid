package com.example.feedthebeast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class FeederHomeRVA extends RecyclerView.Adapter<FeederHomeRVA.ViewHolder> {
    private final static String TAG = FeederHomeRVA.class.getSimpleName();

    private class FeedingTime {
        public Calendar startCalendar;
        public Calendar endCalendar;
        public float cups;

        public FeedingTime(Calendar startCalendar, Calendar endCalendar, float cups) {
            this.startCalendar = startCalendar;
            this.endCalendar = endCalendar;
            this.cups = cups;
        }
    }

    private Context parentContext;
    private String mac;
    private List<FeedingTime> feedingTimes = new ArrayList<>();
    Type feedingTimesType = new TypeToken<ArrayList<FeedingTime>>(){}.getType();

    // The adapter creates ViewHolder objects as needed
    public FeederHomeRVA(Context context, String mac) {
        parentContext = context;
        this.mac = mac;
        getFeedingTimes();
    }

    public void addToFeedingTimes(Calendar startCalendar, Calendar endCalendar, float cups) {
        if (feedingTimes.size() >= 10) {
            Common.showMessage(parentContext, "You may only have 10 time slots, please remove some to add more.", Toast.LENGTH_SHORT);
            return;
        }

        for (FeedingTime feedingTime: feedingTimes) {
            // Don't let the new time slot overlap with another time slot
            if (startCalendar.before(feedingTime.endCalendar) && feedingTime.startCalendar.before(endCalendar)) {
                Common.showMessage(parentContext, "The new time slot can't intersect previous time slots.", Toast.LENGTH_SHORT);
                return;
            }
        }

        FeedingTime feedingTime = new FeedingTime(startCalendar, endCalendar, cups);

        feedingTimes.add(feedingTime);
        feedingTimes.sort((feedingTime1, feedingTime2) -> feedingTime1.startCalendar.compareTo(feedingTime2.startCalendar));

        setFeedingTimes();
        notifyDataSetChanged();
    }

    public void clearFeedingTimes() {
        feedingTimes.clear();
        notifyDataSetChanged();
    }

    private void getFeedingTimes() {
        // Creating array for parameters
        String[] field = new String[1];
        field[0] = "mac";

        // Creating array for data
        String[] data = new String[1];
        data[0] = mac;

        PhpHandler phpHandler = new PhpHandler(Common.GET_FEEDING_TIMES_URL, "POST", field, data);

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

            feedingTimes = new Gson().fromJson(result, feedingTimesType);
        }
    }

    private void setFeedingTimes() {
        // Creating array for parameters
        String[] field = new String[2];
        field[0] = "mac";
        field[1] = "feeding_times";

        // Creating array for data
        String[] data = new String[2];
        data[0] = mac;
        data[1] = new Gson().toJson(feedingTimes);

        PhpHandler phpHandler = new PhpHandler(Common.SET_FEEDING_TIMES_URL, "POST", field, data);

        phpHandler.sendRequest();

        if (phpHandler.resultReady()) {
            String result = phpHandler.getResult();

            // Catches if web server is not running
            if (result.isEmpty()) {
                return;
            }

            Common.showMessage(parentContext, result, Toast.LENGTH_SHORT);
        }
    }

    // The ViewHolder is a wrapper around a view that contains the layout for each individual item
    // in the list.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView startTime;
        private final TextView endTime;
        private final TextView cups;
        private final FloatingActionButton fab;

        public ViewHolder(@NonNull View view) {
            super(view);

            this.startTime = view.findViewById(R.id.tv_FeederHomeRow_StartTimeValue);
            this.endTime = view.findViewById(R.id.tv_FeederHomeRow_EndTimeValue);
            this.cups = view.findViewById(R.id.tv_FeederHomeRow_CupsValue);
            this.fab = view.findViewById(R.id.fab_FeederHomeRow_Delete);
        }
    }

    // This method is called when the RecyclerView needs to create a new ViewHolder.
    // Each element in the recycler list is defined by a ViewHolder object.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parentContext);
        View view = layoutInflater.inflate(R.layout.feeder_home_row, parent, false);

        return new ViewHolder(view);
    }

    // RecyclerView calls this method to associate a ViewHolder with its data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedingTime feedingTime = feedingTimes.get(position);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        holder.startTime.setText(simpleDateFormat.format(feedingTime.startCalendar.getTime()));
        holder.endTime.setText(simpleDateFormat.format(feedingTime.endCalendar.getTime()));
        holder.cups.setText(String.valueOf(feedingTime.cups));

        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedingTimes.remove(position);
                setFeedingTimes();
                notifyDataSetChanged();
            }
        });
    }

    // Returns the amount of items in the recycler list.
    @Override
    public int getItemCount() {
        return feedingTimes.size();
    }
}
