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


public class Links extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        String[] links = getResources().getStringArray(R.array.links); // get the links from resource file

        linearLayout = findViewById(R.id.link_grid);

        for(int i = 0; i < links.length; i++) { // for each link
            String temp[] = links[i].split("_");
            String name = temp[0];
            final String link = temp[1];

            // add to the view
            View view = LayoutInflater.from(this).inflate(R.layout.contact_button, null);
            LinearLayout layout = view.findViewById(R.id.contact_button);
            layout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + getHexColor(i))));

            TextView textView = view.findViewById(R.id.contact_text);
            textView.setText(name);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // when clicked, open browser and give the link to it
                    Uri uriUrl = Uri.parse(link);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 20);

            linearLayout.addView(view, i, params);
        }

    }

    private String getHexColor(int index)
    {
        // random colours
        String[] colorHexes = getResources().getStringArray(R.array.colours);
        return colorHexes[index % colorHexes.length];
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}