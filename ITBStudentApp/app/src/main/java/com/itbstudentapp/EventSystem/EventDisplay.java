package com.itbstudentapp.EventSystem;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class EventDisplay implements View.OnClickListener {
    private Activity context;
    private TextView eventDisplay;

    private final AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
    private final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);

    private long lastCall =0;
    private int currentMessage = 0;
    private ArrayList<Event> events = new ArrayList<>();

    public Handler displayHandler;

    public EventDisplay(Activity context, TextView eventDisplay)
    {
        this.eventDisplay = eventDisplay;
        eventDisplay.setOnClickListener(this);
        this.context = context;


        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        fadeOut.setDuration(1200);
        fadeOut.setFillAfter(true);

        lastCall = Calendar.getInstance().getTimeInMillis();

        if(!UtilityFunctions.doesUserHaveConnection(context))
        {
            eventDisplay.setText("No network connection"); // this is network senstive,
            eventDisplay.setVisibility(View.VISIBLE);
            checkForReconnection(); // cause its on the main activity we can recheck for network
        } else{
            getEvents(); // if we have connection
        }
    }

    public void getEvents()
    {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("events");

        events = new ArrayList<>();

        // for each event, we add to the list
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot snap : dataSnapshot.getChildren())
                    {
                        Event e = snap.getValue(Event.class);

                        if(e.getEventValidTill() > Calendar.getInstance().getTimeInMillis())
                        {
                            events.add(e);
                        } else{
                            reference.child(dataSnapshot.getKey()).getRef().setValue(null);
                        }
                    }

                    if(events.size() > 0) // if we have events, we want to play the anim
                        displayAction(100);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkForReconnection()
    {
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() { // recursive function to check if we have connection

                if(UtilityFunctions.doesUserHaveConnection(context))
                {
                    cancel();
                    getEvents();
                    t.cancel();
                }
            }
        }, 1000, 10000);
    }

    private void displayAction(final int time)
    {
        displayHandler = new android.os.Handler();
        displayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean firstTurn = (time < 1000);
                doThreadAction(firstTurn);
            }
        }, time); // used to show the different events


    }


    private void doThreadAction(boolean firstTurn)
    {
        if((Calendar.getInstance().getTimeInMillis()-lastCall) < 9000 && !firstTurn)
            return; //we want to make sure the event is up for the right amount of time

        lastCall = Calendar.getInstance().getTimeInMillis(); // change the event
        context.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                currentMessage = (currentMessage + 1) % events.size();
                eventDisplay.setVisibility(View.INVISIBLE); // hide the current event
                eventDisplay.setText(events.get(currentMessage).getEventTitle()); // change
                eventDisplay.startAnimation(fadeIn); // start fadein
                displayAction(10000); // call next event change
            }
        });
    }


    @Override
    public void onClick(View v)
    {
        if(v.getId() == eventDisplay.getId())
        { // we want to get more information of the event
            Event event = events.get(currentMessage);

            // show dialog with event info
            final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.event_read_dialog);
            TextView eventTitle = dialog.findViewById(R.id.event_title);
            eventTitle.setText(event.getEventTitle());

            // if we have a image, show the image
            ImageView eventImage = dialog.findViewById(R.id.event_image);
            if(event.getEventImage() == null)
            {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                eventImage.setVisibility(View.INVISIBLE);
                eventImage.setLayoutParams(params);
            } else {
                UtilityFunctions.loadEventImageToView(event.getEventImage(), context,  eventImage);
            }

            TextView eventDesc = dialog.findViewById(R.id.event_dialog_description);

            eventDesc.setText(event.getEventMessage());

            TextView eventValid = dialog.findViewById(R.id.event_till);
            eventValid.setText(eventValid.getText() + " " + UtilityFunctions.milliToDate(event.getEventValidTill()));

            TextView dismiss = dialog.findViewById(R.id.event_diss);
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
}
