package com.itbstudentapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Forum extends AppCompatActivity implements View.OnClickListener{

    private boolean isMenuMain;
    private GridLayout gridLayout;
    private ProgressDialog progress;
    private ForumManager forumManager;

    private DatabaseReference ref;
    private Forum instance;
    private LinearLayout layout;
    private String path;
    private boolean isModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        forumManager = new ForumManager(); // create a forum managers
        gridLayout = findViewById(R.id.forum_menu_grid);

        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));

        Bundle b = getIntent().getExtras();
        path = "forum/sections/";

        if (b != null) {
            if (b.getString("sectionChoice").equals("0")) {
                path = "forum/sections/0/module/";
                isModule = true; // checks if we are in a subsection
            }
        } else {
            path = "forum/sections/"; // otherwise
        }

        setUpMainMenu();
    }

    private void setUpMainMenu()
    {
        isMenuMain = true;

        RelativeLayout[] layouts = {findViewById(R.id.forum_modules), findViewById(R.id.forum_campus), findViewById(R.id.forum_area), findViewById(R.id.forum_transport),
                findViewById(R.id.forum_group), findViewById(R.id.forum_relax)};

        for(int i = 0; i < layouts.length; i++)
        {
            layouts[i].setOnClickListener(this); // add action listener to each option
        }
    }


    @Override
    public void onClick(View v) {
        handleMenuMain(v);
    }

    private void handleMenuMain(View panel)
    {
        // did we enter something with subsections
        if(panel.getId() == R.id.forum_group || panel.getId() == R.id.forum_modules)
        {
            String key = (panel.getId() == R.id.forum_group) ? "forum_groups" : "forum_module";
            setUpProgress();
            gridLayout.removeAllViews();
            ((ViewGroup)gridLayout.getParent()).removeView(gridLayout);
            forumManager.getMenuFromDB((LinearLayout) findViewById(R.id.section_list), key, progress);

        }
        else
        {
            // otherwise show the list
            String sectionName = getSectionNameFromID(panel.getId());
            Intent intent = new Intent(gridLayout.getContext(), ForumList.class);
            intent.putExtra("path", sectionName);
            gridLayout.getContext().startActivity(intent);
        }
    }

    private void setUpProgress()
    {
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.show();
    }

    private String getSectionNameFromID(int id)
    {
        switch (id)
        {
            case R.id.forum_modules:
                return "forum_modules";
            case R.id.forum_area:
                return "forum_area";
            case R.id.forum_transport:
                return "forum_transport";
            case R.id.forum_campus:
                return "forum_campus";
            case R.id.forum_group:
                return "forum_group";
            case R.id.forum_relax:
                return "forum_relax";
        }

        return null;
    }
    
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}


