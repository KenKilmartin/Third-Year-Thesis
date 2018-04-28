package com.itbstudentapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itbstudentapp.AdminSystem.AdminPanel;
import com.itbstudentapp.EventSystem.EventsHandler;
import com.itbstudentapp.QuizSystem.QuizPanel;
import com.itbstudentapp.utils.UserSettings;

import java.text.SimpleDateFormat;
import java.util.Date;


public class UtilityFunctions {


    public static String studentCourse = "bn104";
    public static String student_group = "group_1";

    public static final int READ = 0;
    public static final int UNREAD = 1;

    public static final String PREF_FILE = "user_pref.xml";

    public static final int noImageUser = R.drawable.ic_launcher_web;

    // gets the user name from the database
    public static String getUserNameFromFirebase()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null)
            return  null;

        String user_name = auth.getCurrentUser().getEmail().toString();
        user_name = user_name.split("@")[0].toLowerCase();
        user_name = user_name.replace(".","_");

        return  user_name;
    }


    // converts a millisecond time to a string time
    public static String milliToTime(long currentTime)
    {
        Date messageDate = new Date(currentTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yy");
        String date = simpleDateFormat.format(messageDate);
        return  date;
    }

    // checks if the user has connection to a network
    public static boolean doesUserHaveConnection(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo == null)
            return false;

        return networkInfo.isConnected();
    }

    // formats the titles into a correct format for forum
    public static String formatTitles(String forum_topic)
    {
        String topicArray[] = forum_topic.split("/");
        String topic = topicArray[topicArray.length - 1];

        if(topic.contains("forum")) {
            topic = topic.split("_")[1];
        } else {
            topic = topic.replace("_", " ");
        }

        topic = topic.substring(0,1).toUpperCase() + topic.substring(1, topic.length()).toLowerCase();

        return topic;
    }

    // sets the new toolbar
    public static Toolbar getApplicationToolbar(final Activity context)
    {
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.tool_bar);
        final SharedPreferences preferences = context.getSharedPreferences(UtilityFunctions.PREF_FILE, context.MODE_PRIVATE);

        // sets the users options
        final ImageView menuButton = toolbar.findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(context.getBaseContext(), menuButton);

                // option to edit profile
                menu.getMenu().add("Profile Settings");

                // if we have the permissions
                if(preferences.getString("accountType", "").equalsIgnoreCase("admin"))
                {
                    menu.getMenu().add("Admin panel");
                    menu.getMenu().add("Events");
                }

                // if we are a lecturer or staff
                if(preferences.getString("accountType", "").equalsIgnoreCase("lecturer")
                        ||preferences.getString("accountType", "").equalsIgnoreCase( "admin"))
                {
                    menu.getMenu().add("Set quiz");
                }


                // contact us option
                menu.getMenu().add("Contact us");
                menu.getMenu().add("Logout"); // log out

                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        String menuItem = item.getTitle().toString().toLowerCase();

                        switch (menuItem)
                        {
                            case "profile settings":
                                loadMenuIntent(context, ProfileSettings.class);
                                break;
                            case "admin panel":
                                context.startActivity(new Intent(context, AdminPanel.class));
                                break;
                            case "set quiz":
                                context.startActivity(new Intent(context, QuizPanel.class));
                                break;
                            case "contact us":
                                // opens a new message to the admin
                                Intent contact = new Intent(context, MessageScreen.class);
                                contact.putExtra("message_id", "admin");
                                context.startActivity(contact);
                                break;
                            case "events":
                                context.startActivity(new Intent(context, EventsHandler.class));
                                break;
                            case "logout":
                                // logs out the user
                                removeInstanceIDFromDB(context);
                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                auth.signOut();
                                auth = null;
                                Intent intent = new Intent(context, LoginScreen.class);
                                UserSettings.clearFile(context);
                                context.startActivity(intent);
                                ((Activity)context).finish();
                                break;
                        }
                        return true;
                    }
                });
            }
        });

        return toolbar;
    }

    // stops the services and subcriptions
    private static void removeInstanceIDFromDB(Activity context)
    {
        context.stopService(new Intent(context, MyFirebaseMessagingService.class));
        context.stopService(new Intent(context, MyFirebaseInstanceIDService.class));

        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_"+ UtilityFunctions.getUserNameFromFirebase());
        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_admin");


        FirebaseMessaging.getInstance().unsubscribeFromTopic("events");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
        ref.child("instance_id").setValue(null);
    }

    private static void loadMenuIntent(Activity context, Class activityClass)
    {
        Intent intent = new Intent(context, activityClass);
        context.startActivity(intent);
        context.finish();
    }

    public static String getHexColor(int index)
    {

        String[] colorHexes = {"d6d322", "0011cc","ce371c", "299308", "91057c", "069b71",  "8c010a", "33b5e5"};

        return colorHexes[index % colorHexes.length];
    }

    // loads a image into the image view
    public static void loadImageToView(final String userName, final Context context, final ImageView imageView)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + userName);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("imageLink") != null) {
                    String imageUrl = dataSnapshot.child("imageLink").getValue(String.class);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("userImages/" + imageUrl);
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).into(imageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void loadEventImageToView(final String event, final Context context, final ImageView imageView)
    {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("events/" + event);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(imageView);
            }
        });
    }



    // convers milli to date
    public static String milliToDate(long time)
    {
        Date messageDate = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy");
        String date = simpleDateFormat.format(messageDate);
        return  date;
    }

    public static boolean askForLocationPermission(Activity activity)
    {
        if(ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        } else{
            return false;
        }
    }

    public static String capitalizeStringWithSplit(String stringToCap, String split)
    {
        String stringArray[] = stringToCap.split(split);
        Log.e("Split", stringArray.length + "" + stringToCap);
        String returnString = "";

        for(int i = 0; i < stringArray.length; i++)
        {
            String capitalizedString = stringArray[i].substring(0,1).toUpperCase() + stringArray[i].substring(1, stringArray[i].length()) + " ";
            returnString += capitalizedString;
        }

        return returnString;
    }
}
