package com.lets.service.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lets.service.R;
import com.lets.service.db.MyLocation;

import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationListViewHolder> {

    public LocationListAdapter(List<MyLocation> myLocationList) {
        this.myLocationList = myLocationList;
    }

    List<MyLocation> myLocationList;

    @NonNull
    @Override
    public LocationListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_layout,parent,false);
        return new LocationListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationListViewHolder holder, int position) {
        MyLocation myLocation = myLocationList.get(position);
        holder.tvItemLat.setText("Lat "+myLocation.getLatitude());
        holder.tvItemLon.setText("Lon "+myLocation.getLongitude());
    }

    @Override
    public int getItemCount() {
        return myLocationList.size();
    }

    public class LocationListViewHolder extends RecyclerView.ViewHolder{

        TextView tvItemLat, tvItemLon;

        public LocationListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemLat = itemView.findViewById(R.id.tvItemLat);
            tvItemLon = itemView.findViewById(R.id.tvItemLon);
        }
    }
}
