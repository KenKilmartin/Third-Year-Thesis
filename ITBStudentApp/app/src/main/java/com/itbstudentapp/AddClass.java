package com.itbstudentapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;

public class AddClass extends AppCompatActivity {

    private Button btnSave, btnBack;
    private EditText class_event, startTime, endTime, room;
    private String selectedDay;

    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        btnSave = findViewById(R.id.btnSaveTimetableEntry);
        btnBack = findViewById(R.id.btnBckTimetableEntry);
        class_event = findViewById(R.id.addClassOrEvent);
        startTime = findViewById(R.id.addTimetableStartTime);
        endTime = findViewById(R.id.addTimetableEndTime);
        room = findViewById(R.id.addTimetableRoom);
        databaseHelper = new DatabaseHelper(this);

        Intent receivedIntent = getIntent();

        //now get the day we passed as an extra
        selectedDay = receivedIntent.getStringExtra("day");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String class_event = AddClass.this.class_event.getText().toString();
                String startTime = AddClass.this.startTime.getText().toString();

                if(!isValidTime(startTime))
                    return;

                String endTime = AddClass.this.endTime.getText().toString();

                if(!isValidTime(endTime))
                    return;

                String room = AddClass.this.room.getText().toString();
                if(!class_event.equals("")&&!startTime.equals("")&&!endTime.equals("")&&!room.equals("")){
                    databaseHelper.addData(startTime, endTime, class_event, selectedDay, room);
                    toastMessage("Saved");
                    Intent intent = new Intent(AddClass.this, DayView.class);
                    intent.putExtra("day",selectedDay);
                    startActivity(intent);
                    finish();
                }else{
                    toastMessage("All fields must be completed");
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddClass.this, DayView.class);
                intent.putExtra("day",selectedDay);
                startActivity(intent);
                finish();
            }
        });
    }
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidTime(String time)
    {
        try {
            time = time.replace(":", ".");
            float timeValue = Float.parseFloat(time);

            int hour = (int)Math.floor(timeValue);

            if(hour > 24 || hour < 0)
            {
                Toast.makeText(getApplicationContext(), "Invalid hour entered. Enter between 0 - 24", Toast.LENGTH_SHORT).show();
                return false;
            }

            float minutes = (float) timeValue - hour;
           
            if(minutes < 0.0 || minutes >= 0.60)
            {
                Toast.makeText(getApplicationContext(), "Invalid minutes entered. Enter between 0 - 59", Toast.LENGTH_SHORT).show();
                return false;
            }


            return true;

        } catch (NumberFormatException e)
        {
            Toast.makeText(getApplicationContext(), "You must enter a valid time.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
