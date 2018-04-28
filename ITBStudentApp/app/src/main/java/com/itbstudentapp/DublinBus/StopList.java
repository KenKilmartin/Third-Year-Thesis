package com.itbstudentapp.DublinBus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.itbstudentapp.Interfaces.OnThreadComplete;
import com.itbstudentapp.Manifest;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

import java.util.ArrayList;

public class StopList extends AppCompatActivity implements OnMapReadyCallback, OnThreadComplete {

    private String route;
    private Stop stops[];
    private LinearLayout linearLayout;

    private ProgressDialog progressDialog;
    private Thread loader;
    private GoogleMap map;
    private StopInformationFinder stopInformationFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_list);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        linearLayout = (LinearLayout) findViewById(R.id.bus_stop_list);
        route = getIntent().getStringExtra("route"); // get route from the last intent

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Retrieving stops");
        progressDialog.setMessage("Loading");
        progressDialog.show();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        stopInformationFinder = new StopInformationFinder(this);
        loader  = new Thread(new Runnable() {
            @Override
            public void run() {
                stopInformationFinder.execute(route);
            }
        });

        loader.start();

    }

    int buttonCounter = 0;
    private void drawButttons(final Stop st)
    {
        View button = LayoutInflater.from(this).inflate(R.layout.contact_button, null);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + UtilityFunctions.getHexColor(buttonCounter++))));

        TextView routeText = button.findViewById(R.id.contact_text);
        routeText.setText(st.getStop_name());
        button.setTag(st.getStop_number() + ":" + st.getStop_name());
        routeText.setPadding(20,20,20,20);

        map.clear();

        LatLng coord = new LatLng(Double.parseDouble(st.getLatitude()),Double.parseDouble(st.getLongatude()));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(coord);
        markerOptions.title(st.getStop_name());
        map.addMarker(markerOptions);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        params.setMargins(0, 0, 0, 25);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info[] = v.getTag().toString().split(":"); // because we want to hold 2 bits of info in tag
                GetNextIntent(st); // start next intenet

            }
        });

        LinearLayout.LayoutParams linear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ((LinearLayout)linearLayout).addView(button, params); // add to view
    }

    private void GetNextIntent(Stop stop)
    {
        Intent intent = new Intent(this, BusTimes.class); // create new intent
        intent.putExtra("route", this.route); // bundle resources
        intent.putExtra("stop_num", stop.getStop_number());
        intent.putExtra("stop_name", stop.getStop_name());
        intent.putExtra("stop_long", stop.getLongatude());
        intent.putExtra("stop_lat", stop.getLatitude());
        startActivity(intent); // start
        finish();
    }

    public void onThreadCompleteCall()
    {
        giveStopList(stopInformationFinder.getStops());
    }

    public void giveStopList(ArrayList<Stop> stops)
    {
        for(Stop s : stops)
        {
            drawButttons(s);
        }
        progressDialog.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        LatLng lat = new LatLng(53.4048029,-6.3791624);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setMinZoomPreference(15);
        googleMap.setMaxZoomPreference(45);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 15f));

        if(UtilityFunctions.askForLocationPermission(this))
        {
            googleMap.setMyLocationEnabled(true);
        } else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}
