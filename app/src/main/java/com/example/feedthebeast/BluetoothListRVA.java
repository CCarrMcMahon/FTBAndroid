package com.example.feedthebeast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothListRVA extends RecyclerView.Adapter<BluetoothListRVA.ViewHolder> {
    private Context parentContext;
    private List<BluetoothDevice> devices = new ArrayList<>();

    // The adapter creates ViewHolder objects as needed
    public BluetoothListRVA(Context context) {
        parentContext = context;
    }

    public void addToDevices(BluetoothDevice device) {
        devices.add(device);
    }

    public void clearDevices() {
        devices.clear();
    }

    // The ViewHolder is a wrapper around a view that contains the layout for each individual item
    // in the list.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView bluetoothName;
        private final TextView bluetoothAddress;
        private final ConstraintLayout bluetoothRow;

        public ViewHolder(@NonNull View view) {
            super(view);

            this.bluetoothName = view.findViewById(R.id.tv_BluetoothName);
            this.bluetoothAddress = view.findViewById(R.id.tv_BluetoothAddress);
            this.bluetoothRow = view.findViewById(R.id.cl_BluetoothListRow);
        }
    }

    // This method is called when the RecyclerView needs to create a new ViewHolder.
    // Each element in the recycler list is defined by a ViewHolder object.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parentContext);
        View view = layoutInflater.inflate(R.layout.bluetooth_list_row, parent, false);

        return new ViewHolder(view);
    }

    // RecyclerView calls this method to associate a ViewHolder with its data.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        holder.bluetoothName.setText(String.format("Name: %s", device.getName()));
        holder.bluetoothAddress.setText(String.format("MAC: %s",  device.getAddress()));

        holder.bluetoothRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentContext, WiFiDetails.class);
                intent.putExtra("device", device);
                parentContext.startActivity(intent);
            }
        });
    }

    // Returns the amount of items in the recycler list.
    @Override
    public int getItemCount() {
        return devices.size();
    }
}
