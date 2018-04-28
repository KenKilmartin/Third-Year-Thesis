package com.itbstudentapp.ItbShuttleBus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

public class ItbShuttleMenu extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout nac, itb, cmine, shoppingCentre;
    private int firstChoice = -1; // used to decide the direction
    private boolean towardCollege = false;
    private TextView userPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itb_shuttle_menu);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        userPrompt = findViewById(R.id.user_choice);

        nac = findViewById(R.id.nac);
        itb = findViewById(R.id.itb_campus);
        cmine = findViewById(R.id.cmine);
        shoppingCentre = findViewById(R.id.shopping_centre);

        nac.setOnClickListener(this);
        itb.setOnClickListener(this);
        cmine.setOnClickListener(this);
        shoppingCentre.setOnClickListener(this);
    }

    private int getChoiceNumber(View v)
    {
        switch (v.getId())
        {
            case R.id.itb_campus:
                return  1;
            case R.id.nac:
                return  2;
            case R.id.shopping_centre:
                return  3;
            case R.id.cmine:
                return  4;
        }

        return -1;
    }

    @Override
    public void onClick(View v)
    {
        if(firstChoice < 0) // we have just picked the firs stop
        {
            firstChoice = getChoiceNumber(v);
            userPrompt.setText("Pick your end point"); // change the prompt
        }
        else
        {
            int secondChoice = getChoiceNumber(v);

            if(secondChoice == firstChoice) // makes sure we dont pick the same start and finish point
            {
                Toast.makeText(this, "Pick a end point that is not the start point.", Toast.LENGTH_LONG).show();
                return;
            }

            if(firstChoice > secondChoice)
            {
                towardCollege = true; // we know the direction is towards itb now
            }

            Intent timesScreen = new Intent(this, ItbShuttleTimes.class); // start the next activity with the information
            timesScreen.putExtra("direction", towardCollege);
            timesScreen.putExtra("startPoint", getStartPoint(firstChoice));
            startActivity(timesScreen);
            finish();
        }
    }

    private String getStartPoint(int firstChoice)
    {
        switch (firstChoice)
        {
            case 1:
                return  "ITB Campus";
            case 2:
                return  "National Aquatic Centre";
            case 3:
                return  "Blanchardstown Shopping Centre";
            case 4:
                return  "Coolmine Train Station";
        }

        return null;
    }

    @Override
    public void onBackPressed()
    {
        if(firstChoice != -1) // if we just want to go back to the first choice
        {
            firstChoice = -1;
            userPrompt.setText("Pick your Starting point");

        } else{
            super.onBackPressed();
        }
    }
}
