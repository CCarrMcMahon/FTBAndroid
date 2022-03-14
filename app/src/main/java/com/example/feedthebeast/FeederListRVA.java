package com.example.feedthebeast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FeederListRVA extends RecyclerView.Adapter<FeederListRVA.ViewHolder> {
    public List<String> names = new ArrayList<>();

    // The adapter creates ViewHolder objects as needed
    public FeederListRVA() {

    }

    // The ViewHolder is a wrapper around a view that contains the layout for each individual item
    // in the list.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView feederName;

        public ViewHolder(@NonNull View view) {
            super(view);

            this.feederName = (TextView) view.findViewById(R.id.tv_FeederName);
        }
    }

    // This method is called when the RecyclerView needs to create a new ViewHolder.
    // Each element in the recycler list is defined by a ViewHolder object.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.feeder_list_row, parent, false);

        return new ViewHolder(view);
    }

    // RecyclerView calls this method to associate a ViewHolder with its data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.feederName.setText(names.get(position));
    }

    // Returns the amount of items in the recycler list.
    @Override
    public int getItemCount() {
        return names.size();
    }
}
