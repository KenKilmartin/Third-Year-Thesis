package com.itbstudentapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itbstudentapp.ChatSystem.Chat;
import com.itbstudentapp.EventSystem.EventDisplay;
import com.itbstudentapp.QuizSystem.QuizHome;
import com.itbstudentapp.utils.UserSettings;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout forum, transport, map, timetable, quiz, chat, links, phone, notes;
    private EventDisplay eventDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserSettings.currentIntent = this.getIntent();

        UserSettings.checkIfInit(this, UtilityFunctions.getUserNameFromFirebase());
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        
        forum =  findViewById(R.id.forum);
        transport = findViewById(R.id.transport);
        map = findViewById(R.id.map);
        timetable = findViewById(R.id.timetable);
        quiz = findViewById(R.id.quiz);
        chat = findViewById(R.id.chat);
        links = findViewById(R.id.links);
        phone = findViewById(R.id.phone);
        notes = findViewById(R.id.notes);

        String[] colorHexes = getResources().getStringArray(R.array.colours);
        transport.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[0 % colorHexes.length])));
        timetable.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[1 % colorHexes.length])));
        quiz.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[2 % colorHexes.length])));
        map.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[3 % colorHexes.length])));
        forum.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[4 % colorHexes.length])));
        chat.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[5 % colorHexes.length])));
        links.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[6 % colorHexes.length])));
        phone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[7 % colorHexes.length])));
        notes.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + colorHexes[8 % colorHexes.length])));

        phone.setOnClickListener(this);
        transport.setOnClickListener(this);
        map.setOnClickListener(this);
        quiz.setOnClickListener(this);
        chat.setOnClickListener(this);
        links.setOnClickListener(this);
        forum.setOnClickListener(this);
        timetable.setOnClickListener(this);
        notes.setOnClickListener(this);

        eventDisplay = new EventDisplay(this, (TextView) findViewById(R.id.event_message));

    }

    // this interputs the button which clicked
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.transport:
                startActivity(new Intent(this, Transport.class));
                onActivityChange();
                finish();
                break;
            case R.id.forum:
                startActivity(new Intent(this, Forum.class));
                onActivityChange();
                finish();
                break;
            case R.id.map:
                startActivity(new Intent(this, MapActivity.class));
                onActivityChange();
                finish();
                break;
            case R.id.quiz:
                startActivity(new Intent(this, QuizHome.class));
                onActivityChange();
                finish();
                break;
            case R.id.links:
                startActivity(new Intent(this, Links.class));
                onActivityChange();
                finish();
                break;
            case R.id.chat:
                startActivity(new Intent(this, Chat.class));
                onActivityChange();
                finish();
                break;
            case R.id.phone:
                startActivity(new Intent(this, Phone.class));
                onActivityChange();
                finish();
                break;
            case R.id.timetable:
                startActivity(new Intent(this, Timetable.class));
                onActivityChange();
                finish();
                break;
            case R.id.notes:
                startActivity(new Intent(this, NoteMain.class));
                onActivityChange();
                finish();
                break;
        }

    }

    private void onActivityChange()
    {
        if(eventDisplay.displayHandler != null)
            eventDisplay.displayHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // when main activity opens, gets the preferences

        SharedPreferences preferences = this.getSharedPreferences(UtilityFunctions.PREF_FILE, this.MODE_PRIVATE);
        String username = preferences.getString("username", "");

        if(username == null || username.equalsIgnoreCase(""))
        {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
