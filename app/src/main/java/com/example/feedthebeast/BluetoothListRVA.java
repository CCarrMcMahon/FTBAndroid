package com.example.feedthebeast;

import android.companion.BluetoothDeviceFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class BluetoothListRVA extends RecyclerView.Adapter<BluetoothListRVA.ViewHolder> {
    public List<String> names = new ArrayList<String>();

    // The adapter creates ViewHolder objects as needed
    public BluetoothListRVA() {

    }

    // The ViewHolder is a wrapper around a view that contains the layout for each individual item
    // in the list.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView bluetoothName;

        public ViewHolder(@NonNull View view) {
            super(view);

            this.bluetoothName = (TextView) view.findViewById(R.id.tv_BluetoothName);
        }
    }

    // This method is called when the RecyclerView needs to create a new ViewHolder.
    // Each element in the recycler list is defined by a ViewHolder object.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.bluetooth_list_row, parent, false);

        return new ViewHolder(view);
    }

    // RecyclerView calls this method to associate a ViewHolder with its data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bluetoothName.setText(names.get(position));
    }

    // Returns the amount of items in the recycler list.
    @Override
    public int getItemCount() {
        return names.size();
    }
}
