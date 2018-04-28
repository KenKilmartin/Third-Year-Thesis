package com.itbstudentapp.DublinBus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

public class RouteChoice extends AppCompatActivity{

    private LinearLayout linearLayout;
    private ProgressDialog progressDialog;
    private Thread loader;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Retrieving routes");
        progressDialog.setMessage("Loading");
        progressDialog.show();

        final DublinBusRouteFinder dbhandler = new DublinBusRouteFinder(this);

        loader = new Thread(new Runnable() {
            @Override
            public void run() {
                dbhandler.execute();
            }
        });
        // loads the list of routes in the area

        loader.start();


        setContentView(R.layout.activity_route_choice);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        linearLayout = (LinearLayout) findViewById(R.id.butt_screen);
    }


    int buttonCounter = 0;
    private void drawButtons(String route)
    {
        View button = LayoutInflater.from(this).inflate(R.layout.contact_button, null);
        button.setTag(route);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + UtilityFunctions.getHexColor(buttonCounter++))));

        TextView routeText = button.findViewById(R.id.contact_text);
        routeText.setText(route);
        routeText.setPadding(20,20,20,20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        params.setMargins(0, 0, 0, 25);

        final RouteChoice rc = this;
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            Intent intent = new Intent(rc, StopList.class);
            intent.putExtra("route", v.getTag().toString());
            startActivity(intent);
            finish();
            }
        });

        ((LinearLayout)linearLayout).addView(button, params);
    }

    public void display(String[] routes)
    {
        for(String route : routes)
        {
            drawButtons(route);
        }

        progressDialog.dismiss();

        try {
            loader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
