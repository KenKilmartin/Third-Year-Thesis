package com.itbstudentapp.NotificationSystem;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *  what happens here is the information is sent to a firebase database which has a cloud function set to onWrite which is used when a database has a
 *  field entered into it. From this the function decides how to handle it, such as if its a chat message, it will just notify the reciever. if its a new
 *  event, all users are notified
 */

public class FirebaseNotificationManager {

    public static void sendNotificationToUser(Notification notification/*String user, String message */)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications"); // get the notification database

        String key = UUID.randomUUID().toString(); // give a random id

        Map userNotification = new HashMap(); // create a hashmap to send to firebase

        if(notification.getMessageSender() != null)
        {
            userNotification.put("user", notification.getMessageSender()); // give the user
        }

        userNotification.put("type", notification.getNotificationType());
        userNotification.put("title", notification.getTitle());
        userNotification.put("body", notification.getBody());

        reference.push().setValue(userNotification);
    }
}
