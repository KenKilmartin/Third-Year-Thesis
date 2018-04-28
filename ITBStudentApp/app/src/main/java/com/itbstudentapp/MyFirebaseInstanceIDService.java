package com.itbstudentapp;

import android.app.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();

        // get instance id from db
        if(UtilityFunctions.getUserNameFromFirebase() != null) {
            String username = UtilityFunctions.getUserNameFromFirebase();
            username = username.replace(".", "_");

            FirebaseDatabase.getInstance().getReference("users").child(username).child("instance_id").setValue(token);
        }

    }

    // save instance id
    public static void saveTokenToDb()
    {
        String token = FirebaseInstanceId.getInstance().getToken();

        String username = UtilityFunctions.getUserNameFromFirebase();

        username = username.replace(".", "_");

        FirebaseDatabase.getInstance().getReference("users").child(username).child("instance_id").setValue(token);
    }


}
