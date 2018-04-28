package com.itbstudentapp;



import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Phone extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        String[] contacts = getResources().getStringArray(R.array.contacts); // get the contacts from resource file

        linearLayout = findViewById(R.id.contact_grid);

        // set each of the contacts to the view
        for(int i = 0; i < contacts.length; i++) {
            String temp[] = contacts[i].split("/");
            String name = temp[0];
            final String number = temp[1];

            View view = LayoutInflater.from(this).inflate(R.layout.contact_button, null);
            LinearLayout layout = view.findViewById(R.id.contact_button);
            layout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + getHexColor(i))));

            TextView textView = view.findViewById(R.id.contact_text);
            textView.setText(name);

            // on click, open the call with the data needed
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(number));
                    startActivity(callIntent);
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 20);

            linearLayout.addView(view, i, params);
        }

    }

    private String getHexColor(int index)
    {
        String[] colorHexes = getResources().getStringArray(R.array.colours);
        return colorHexes[index % colorHexes.length];
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
