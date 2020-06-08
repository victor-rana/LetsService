package com.lets.service;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lets.service.adapter.LocationListAdapter;
import com.lets.service.db.LocationRepository;
import com.lets.service.db.MyLocation;
import com.lets.service.service.BackgroundLocationService;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final int PERMISSION_REQUEST_CODE = 200;
    public BackgroundLocationService gpsService;
    LocationRepository locationRepository;
    private GoogleMap mMap;
    LocationListAdapter locationListAdapter;
    RecyclerView recyclerView;
    List<MyLocation> myLocationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rvLocationList);
        locationListAdapter = new LocationListAdapter(myLocationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(locationListAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationRepository = new LocationRepository(getApplicationContext());

        startService();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("onLocationBroadcast"));

        fetechLoaction();
    }

    void startService(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final Intent intent = new Intent(this.getApplication(), BackgroundLocationService.class);
            this.getApplication().startService(intent);
            this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Log.d("TAG", "requestPermissions: ");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    void stopService(){
        final Intent intent = new Intent(this.getApplication(), BackgroundLocationService.class);
        this.getApplication().stopService(intent);
        this.getApplication().unbindService(serviceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("TAG", "onRequestPermissionsResult: "+requestCode);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
            }
        }
    }

    void fetechLoaction(){
        locationRepository = new LocationRepository(getApplicationContext());
        locationRepository.getLocations().observe(this, new Observer<List<MyLocation>>() {
            @Override
            public void onChanged(List<MyLocation> myLocations) {
                Log.d("TAG", "onChanged: "+myLocations.isEmpty());
                if(!myLocations.isEmpty()){
                    if(!myLocationList.isEmpty()){
                        myLocationList.clear();
                    }
                    myLocationList.addAll(myLocations);
                    locationListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            fetechLoaction();
            double lat = intent.getDoubleExtra("lat",0);
            double lon = intent.getDoubleExtra("lon",0);
            Log.d("receiver", "Got lat: " + lat);
            onUpdateMap(lat,lon);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsService.stopTracking();
        stopService();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundLocationService")) {
                gpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
                gpsService.startTracking();
                Log.d("TAG", "onServiceConnected: GPS Ready");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundLocationService")) {
                gpsService = null;
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    void onUpdateMap(double lat, double lon){
        Log.d("TAG", "onUpdateMap: ");
        LatLng latLng = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(cameraUpdate);
    }
}
