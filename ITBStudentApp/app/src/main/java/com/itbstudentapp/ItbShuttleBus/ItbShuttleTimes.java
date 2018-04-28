package com.itbstudentapp.ItbShuttleBus;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.itbstudentapp.MainActivity;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// shows the itb shuttle bus times
public class ItbShuttleTimes extends AppCompatActivity implements OnMapReadyCallback{

    private String startingPoint;
    private LinearLayout stopTimesLayout;
    private boolean direction;
    private GoogleMap map;
    private boolean noBuses = true;

    private LatLng coord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itb_shuttle_times);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        stopTimesLayout = findViewById(R.id.times_list);

        Bundle b = getIntent().getExtras();
        startingPoint = b.getString("startPoint"); // takes the start point
        direction = b.getBoolean("direction"); // takes the end point
        String file = null;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);

        mapFragment.getMapAsync(this);


        if(direction)
            file = getFileFromAssets("towards_itb.json"); // depending on the way the journey is going, load a different file
        else
            file = getFileFromAssets("from_itb.json");

        readFile(file);

        if(noBuses) // if its out of hours
        {
            Toast.makeText(this, "No buses available", Toast.LENGTH_LONG).show();
        }

    }

    private String getFileFromAssets(String path) // gets the file data
    {
        AssetManager assetManager = getAssets();
        String json = null;

        try {
            InputStream inputStream = assetManager.open(path);
            int size = inputStream.available();
            byte[] data = new byte[size];
            inputStream.read(data);
            inputStream.close();
            json = new String(data, "UTF-8");

        } catch (IOException e)
        { // if we have a probelm reading the data
            Toast.makeText(this, "There was a problem with this request", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        return json;

    }

    private void readFile(String file)
    {
        // used to read and decode the json information
        try {
            JSONObject timetable = new JSONObject(file);
            JSONArray listOfTimes = timetable.getJSONArray("journey");

            for(int i = 0; i < listOfTimes.length(); i++)
            {
                JSONObject obj = listOfTimes.getJSONObject(i);

                String jsonKey = obj.keys().next();
                long timeFromKey = getTimeFromString(jsonKey);
                long currentTime = getCurrentTime();
                long timeBuffer = getTimeBuffer();

                if(currentTime < timeFromKey && timeFromKey < timeBuffer)
                {

                    JSONArray stops = obj.getJSONArray(jsonKey);

                    for(int x = 0; x < stops.length(); x++)
                    {
                        JSONObject object = stops.getJSONObject(x);
                        if(object.getString("stop").equalsIgnoreCase(startingPoint))
                        {
                            noBuses = false;
                            View v = LayoutInflater.from(this).inflate(R.layout.bus_time_display, null);
                            TextView dest = v.findViewById(R.id.dest_text);
                            dest.setText((direction) ? "ITB campus": "Coolmine Station" );

                            TextView minsTill = v.findViewById(R.id.bus_time_text);
                            long mins;

                            try {
                                mins  = (timeFromKey - currentTime) / 60000;
                            }catch (ArithmeticException e)
                            {
                                mins = 0;
                            }


                            minsTill.setText(mins + " Mins");

                            TextView startPoint = v.findViewById(R.id.bus_src);
                            startPoint.setText("From " + startingPoint);

                            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc"+UtilityFunctions.getHexColor(i))));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
                            params.setMargins(0, 0, 0, 25);

                            stopTimesLayout.addView(v, params);
                            coord = findCoordForStop(startingPoint);

                            if(map != null)
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15f));

                            continue;
                        }
                    }
                }
            }


        } catch (JSONException jsonException)
        {
            jsonException.printStackTrace();
        }
    }

    // gets the time of the bus
    private long getTimeFromString(String time)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        Calendar calendar = Calendar.getInstance();

        try {

            Date todaysDate = Calendar.getInstance().getTime();

            Date currentTime = sdf.parse(time);
            calendar.set(todaysDate.getYear() + 1900, todaysDate.getMonth(), todaysDate.getDate(), currentTime.getHours(), currentTime.getMinutes(), 0);
            long timeMilli = calendar.getTimeInMillis();
            return timeMilli;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // we want to check the buses within the hour so we have to make sure it covers the next hour
    private long getTimeBuffer()
    {
        int miniutesToCheckAhead = 60;

        Calendar calendar = Calendar.getInstance();
        Date todaysDate = Calendar.getInstance().getTime();

        int newMinute = todaysDate.getMinutes() + miniutesToCheckAhead;
        int hour = 0;

        if(newMinute >= 60)
        {
            newMinute = newMinute % 60;
            hour = todaysDate.getHours() + 1;
        } else {
            hour = todaysDate.getHours();
        }

        calendar.set(todaysDate.getYear() + 1900, todaysDate.getMonth(), todaysDate.getDate(),
                    hour, newMinute, 0);

        return calendar.getTimeInMillis();
    }

    // gets the current time
    private long getCurrentTime()
    {
        Calendar calendar = Calendar.getInstance();
        Date todaysDate = Calendar.getInstance().getTime();
        calendar.set(todaysDate.getYear() + 1900, todaysDate.getMonth(), todaysDate.getDate(),
                todaysDate.getHours(), todaysDate.getMinutes(), 0);


        return calendar.getTimeInMillis();
    }

    @Override
    // used to show the stop location
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng lat;
        if(coord == null)
            lat = new LatLng(53.4048029,-6.3791624);
        else
            lat = coord;

        googleMap.setMinZoomPreference(15);
        googleMap.setMaxZoomPreference(25);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 15f));

        MarkerOptions mo = new MarkerOptions();
        mo.position(lat);
        googleMap.addMarker(mo);

        if(UtilityFunctions.askForLocationPermission(this))
        {
            googleMap.setMyLocationEnabled(true);
        } else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private LatLng findCoordForStop(String stop)
    {
        stop = stop.replace(" ", "_").toLowerCase();
        String[] coord;
        double lat, lng;

        switch (stop.toLowerCase())
        {
            case "itb_campus":
                coord = getResources().getStringArray(R.array.itb);
                lat = Double.parseDouble(coord[0]); //todo code breaking
                lng = Double.parseDouble(coord[1]);
                Log.e("Test", "findCoordForStop: " + lat +"/" + lng );
                return new LatLng(lat, lng);
            case "coolmine _train_station":
                coord = getResources().getStringArray(R.array.coolmine_stop);
                lat = Double.parseDouble(coord[0]);
                lng = Double.parseDouble(coord[1]);
                return new LatLng(lat, lng);
            case "blanchardstown_shopping_centre":
                coord = getResources().getStringArray(R.array.blanchardstown);
                lat = Double.parseDouble(coord[0]);
                lng = Double.parseDouble(coord[1]);
                return new LatLng(lat, lng);
            case "national_aquatic_centre":
                coord = getResources().getStringArray(R.array.nac);
                lat = Double.parseDouble(coord[0]);
                lng = Double.parseDouble(coord[1]);
                return new LatLng(lat, lng);
        }

        return null;
    }
}
