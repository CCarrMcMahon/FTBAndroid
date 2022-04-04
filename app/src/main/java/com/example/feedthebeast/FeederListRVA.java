package com.example.feedthebeast;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FeederListRVA extends RecyclerView.Adapter<FeederListRVA.ViewHolder> {
    public class Feeder {
        private String mac = "";
        private String name = "";

        public Feeder(String mac, String name) {
            this.mac = mac;
            this.name = name;
        }

        public String getMac() {
            return mac;
        }

        public String getName() {
            return name;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private Context parentContext;
    private List<Feeder> feeders = new ArrayList<>();

    // The adapter creates ViewHolder objects as needed
    public FeederListRVA(Context context) {
        parentContext = context;
    }

    public void addToFeeders(String mac, String name) {
        for (Feeder feeder: feeders) {
            if (feeder.getMac().equals(mac)) {
                return;
            }
        }

        feeders.add(new Feeder(mac, name));
        notifyDataSetChanged();
    }

    public void clearFeeders() {
        feeders.clear();
        notifyDataSetChanged();
    }

    // The ViewHolder is a wrapper around a view that contains the layout for each individual item
    // in the list.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView feederMac;
        private final TextView feederName;
        private final ConstraintLayout feederRow;

        public ViewHolder(@NonNull View view) {
            super(view);

            this.feederMac = view.findViewById(R.id.tv_FeederMac);
            this.feederName = view.findViewById(R.id.tv_FeederName);
            this.feederRow = view.findViewById(R.id.cl_FeederListRow);
        }
    }

    // This method is called when the RecyclerView needs to create a new ViewHolder.
    // Each element in the recycler list is defined by a ViewHolder object.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parentContext);
        View view = layoutInflater.inflate(R.layout.feeder_list_row, parent, false);

        return new ViewHolder(view);
    }

    // RecyclerView calls this method to associate a ViewHolder with its data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feeder feeder = feeders.get(position);

        holder.feederMac.setText(feeders.get(position).getMac());
        holder.feederName.setText(feeders.get(position).getName());

        holder.feederRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentContext, FeederHome.class);
                intent.putExtra("mac", feeder.mac);
                intent.putExtra("name", feeder.name);
                parentContext.startActivity(intent);
            }
        });
    }

    // Returns the amount of items in the recycler list.
    @Override
    public int getItemCount() {
        return feeders.size();
    }
}
