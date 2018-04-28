package com.itbstudentapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itbstudentapp.ChatSystem.Chat;
import com.itbstudentapp.utils.UserSettings;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    NotificationManager notificationManager;

    public MyFirebaseMessagingService()
    {
        super();
        // subscribe user to messages
        FirebaseMessaging.getInstance().subscribeToTopic("user_"+ UtilityFunctions.getUserNameFromFirebase());
        FirebaseMessaging.getInstance().subscribeToTopic("events");
    }

    @Override
    // handle new messages
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        Map<String, String> params = remoteMessage.getData();

        if(params.get("type").equalsIgnoreCase("event"))
            setupNotifyOfNewEvent(params);
        else if(params.get("type").equalsIgnoreCase("chat"))
            setupNotifyOfNewMessage(params);
    }

    private void setupNotifyOfNewMessage(Map<String, String> params)
    {
        if(notificationManager == null) // get the notification manager
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.ic_launcher_web);// set notification icon

        setNotificationForUserSettings(nBuilder);
        nBuilder.setContentText(params.get("body")); // set the text

        //set the user name
        String[] nameArray = params.get("username").split(" ");
        String capitalizedName ="";

        // pretty name
        for (int i = 0; i < nameArray.length; i++)
        {
            capitalizedName += nameArray[i].substring(0,1).toUpperCase() + nameArray[i].substring(1,nameArray[i].length()).toLowerCase() + " ";
        }

        // set the title
        nBuilder.setContentTitle(capitalizedName + " sent you a message!");
        Random rnd = new Random();
        int channel = rnd.nextInt(1000000) + 1;

        notificationManager.notify(channel, nBuilder.build());
    }

    private void setupNotifyOfNewEvent(Map<String, String> details)
    {
        if(notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // set icon
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_web);

        setNotificationForUserSettings(nBuilder);

        // event detials
        nBuilder.setContentTitle("New event posted");
        nBuilder.setContentText(details.get("title"));

        notificationManager.notify(generateID(), nBuilder.build());
    }

    private void setNotificationForUserSettings(NotificationCompat.Builder nBuilder)
    {

        if(UserSettings.play_sounds) // if the user allows sounds, play
        {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            nBuilder.setSound(sound, notificationManager.IMPORTANCE_DEFAULT);
        }

        // vibrate
        if(UserSettings.vibrate)
        {
            long[] pattern = {500,1000};
            nBuilder.setVibrate(pattern);
        }

        // LED flash
        if(UserSettings.flash)
        {
            nBuilder.setLights(Color.GREEN, 500, 1000);
        }

        nBuilder.setAutoCancel(true);

    }

    // get id
    private int generateID()
    {
        Random random = new Random();
        int divider = random.nextInt(10) + 1;

        Date date = new Date();
        int randId = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(date)) / divider;

        return randId;
    }
}
