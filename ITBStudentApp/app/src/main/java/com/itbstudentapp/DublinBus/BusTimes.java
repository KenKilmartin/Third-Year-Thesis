package com.itbstudentapp.DublinBus;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.itbstudentapp.Interfaces.OnThreadComplete;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

public class BusTimes extends AppCompatActivity implements OnMapReadyCallback, OnThreadComplete {

    private  BusTimeInfo[] times;
    int numOfRoutes = 0;

    private String stop_long,stop_lat, route, stop, stop_name;
    private BusTimeReciever btr;
    private ProgressDialog progressDialog;
    private Thread th;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_times);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));

        route = getIntent().getStringExtra("route");
        stop = getIntent().getStringExtra("stop_num");
        stop_name = getIntent().getStringExtra("stop_name");
        stop_long = getIntent().getStringExtra("stop_long");
        stop_lat = getIntent().getStringExtra("stop_lat");

        btr = new BusTimeReciever(this); // gets the times from the server
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Retrieving times");
        progressDialog.setMessage("Loading");
        progressDialog.show();

        th = new Thread(new Runnable() {
            @Override
            public void run() {
                 btr.execute(route, stop); // async class
            }
        });

        th.start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

    }

    private void populateList(String route, String stop, String stop_name, BusTimeInfo times[])
    {
        LinearLayout layout = findViewById(R.id.times_list); // get the layout

        if(times == null || times.length == 0) {
            Toast.makeText(getApplicationContext(), "No buses currently listed", Toast.LENGTH_LONG).show();
            return;
        }
        for(BusTimeInfo time : times) // for each loop
        {
            View v = LayoutInflater.from(this).inflate(R.layout.bus_time_display, layout, false); // new instance of view
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + UtilityFunctions.getHexColor(numOfRoutes))));

            TextView time_text = (TextView) v.findViewById(R.id.bus_time_text); // get textviews in that view
            TextView dest_text = (TextView) v.findViewById(R.id.dest_text);
            TextView bus_src = (TextView) v.findViewById(R.id.bus_src);

            time_text.setText(time.getBus_time() + " Mins");
            dest_text.setText(route + " " + time.getBus_dest()); // destination
            bus_src.setText("via " + stop_name);


            ((LinearLayout) layout).addView(v); // add to the view
        }
    }

    @Override
    // shows where the stop is located
    public void onMapReady(GoogleMap googleMap) {

        LatLng lat = new LatLng(Double.parseDouble(stop_lat), Double.parseDouble(stop_long));
        googleMap.setMinZoomPreference(15);
        googleMap.setMaxZoomPreference(45);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 15f));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(lat);
        markerOptions.title(stop_name);
        googleMap.addMarker(markerOptions);

        if(UtilityFunctions.askForLocationPermission(this))
        {
            googleMap.setMyLocationEnabled(true);
        } else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onThreadCompleteCall() // when we complete getting the information, we get the timee
    {
        times = btr.getTimes();
        populateList(route, stop, stop_name, times); // method that populates the screen
        progressDialog.dismiss();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
