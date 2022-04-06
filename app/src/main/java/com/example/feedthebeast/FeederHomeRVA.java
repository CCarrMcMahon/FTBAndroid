package com.example.feedthebeast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class FeederHomeRVA extends RecyclerView.Adapter<FeederHomeRVA.ViewHolder> {
    private final static String TAG = WiFiDetails.class.getSimpleName();

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
    private List<FeedingTime> feedingTimes = new ArrayList<>();

    // The adapter creates ViewHolder objects as needed
    public FeederHomeRVA(Context context) {
        parentContext = context;
    }

    public void addToFeedingTimes(Calendar startCalendar, Calendar endCalendar, float cups) {
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

        notifyDataSetChanged();
    }

    public void clearFeedingTimes() {
        feedingTimes.clear();
        notifyDataSetChanged();
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
