package com.itbstudentapp.EventSystem;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbstudentapp.MainActivity;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;
import com.itbstudentapp.utils.UserSettings;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *  used to edit and create events
 */
public class EventsHandler extends AppCompatActivity implements View.OnClickListener {

    private TextView addEvent;
    private EventDialog eventDialog;
    private ArrayList<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_handler);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        UserSettings.currentIntent = this.getIntent();

        addEvent = findViewById(R.id.add_event);
        addEvent.setOnClickListener(this);
        loadEvents();
    }

    public void setEventDialog(EventDialog eventDialog)
    {
        this.eventDialog = eventDialog;
    }

    /**
     * gets the list of the current events
     */
    private void loadEvents() {
        if (!UtilityFunctions.doesUserHaveConnection(this)) {
            Toast.makeText(this, "No network connection. Please try again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        final LinearLayout events_section = findViewById(R.id.events_section);
        final EventsHandler _instance = this;

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("events");
        events = new ArrayList<>();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot snap : dataSnapshot.getChildren())
                {
                    final Event e = snap.getValue(Event.class);
                    final View event_detail = LayoutInflater.from(events_section.getContext()).inflate(R.layout.event_detail, null);
                    TextView event_title_box = event_detail.findViewById(R.id.event_title);
                    String event_title = e.getEventTitle();
                    String event_name = event_title.substring(0,1).toUpperCase() + event_title.substring(1,event_title.length()).toLowerCase();

                    if(event_name.length() > 19)
                    {
                        event_name = event_name.substring(0, 15) + "..."; // looks wierd when its longer the 19 characters
                    }

                    event_title_box.setText(event_name);

                    ImageView edit = event_detail.findViewById(R.id.event_edit);
                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new EventDialog(_instance,_instance, e, snap.getKey()); // if we want to edit the event
                        }
                    });

                    ImageView delete = event_detail.findViewById(R.id.event_remove); // delete the event
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snap.getRef().setValue(null);
                            events_section.removeView(event_detail);
                        }
                    });

                    events_section.addView(event_detail);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == addEvent.getId()) {
            eventDialog = new EventDialog(this, this);
        }
    }

    public void reloadIntent()
    {
        startActivity(getIntent());
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            eventDialog.setUri(data.getData());
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
