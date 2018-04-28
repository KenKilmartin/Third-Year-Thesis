package com.itbstudentapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbstudentapp.UtilityFunctions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class UserSettings
{
    public static boolean play_sounds = true;
    public static boolean vibrate = true;
    public static boolean flash = true;
    public static boolean location = true;

    public static String username;
    public static String studentCourse;
    public static String student_groups[];
    public static String accountType;

    private static void setupPlayerOptions(Context ct) // sets the users defaults in preferences
    {
        SharedPreferences preferences = ct.getSharedPreferences(UtilityFunctions.PREF_FILE, ct.MODE_PRIVATE);

        username = preferences.getString("username", "");
        accountType = preferences.getString("accountType", "");
        studentCourse = preferences.getString("courseID", "");

        String[] groups = preferences.getString("studentGroups", "").split(":");
        student_groups = groups;

        vibrate = preferences.getBoolean("vibrate", true);
        play_sounds = preferences.getBoolean("sound", true);
        flash = preferences.getBoolean("led", true);
        location = preferences.getBoolean("geo", true);
    }

    public static Intent currentIntent;

    // checks if we have set the users prefs
    public static void checkIfInit(final Context ct, String username)
    {
        final SharedPreferences pref = ct.getSharedPreferences(UtilityFunctions.PREF_FILE, ct.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        username = username.replace(".", "_");

        if(!pref.contains("moderator")) {
            checkIfUserModerator(username, ct);
            Log.e("ERROR", "checkIfInit: " + "Check failed" );
        }

        if(pref.getBoolean("hasChecked", false))
        {
            if(username == null || username == "")
                setupPlayerOptions(ct);

            return;
        }

        FirebaseAuth.getInstance().signOut();


        Log.e("USERNAME", "checkIfInit: " + username );
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + username);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                editor.putBoolean("hasChecked", true);
                editor.putString("username", dataSnapshot.child("username").getValue(String.class).split("@")[0]);
                editor.putString("accountType", dataSnapshot.child("accountType").getValue(String.class));

                if(dataSnapshot.child("accountType").getValue(String.class).equalsIgnoreCase("student"))
                {
                    editor.putString("studentGroups", dataSnapshot.child("groups").getValue(String.class));
                    editor.putString("courseID", dataSnapshot.child("courseID").getValue(String.class));
                }

                editor.apply();
                setupPlayerOptions(ct);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        checkIfUserModerator(username, ct);
    }

    // used to check if we are a moderator
    private static void checkIfUserModerator(final String username, final Context ct)
    {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("moderators");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(username).exists())
                {
                    SharedPreferences.Editor editor = ct.getSharedPreferences(UtilityFunctions.PREF_FILE, ct.MODE_PRIVATE).edit();
                    editor.putBoolean("moderator", true);
                    editor.apply();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // clears the current preferences
    public static void clearFile(Context ct)
    {
        SharedPreferences.Editor editor = ct.getSharedPreferences(UtilityFunctions.PREF_FILE, ct.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }
}
