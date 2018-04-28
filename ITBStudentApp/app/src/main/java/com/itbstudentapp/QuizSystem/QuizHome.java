package com.itbstudentapp.QuizSystem;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import java.util.Random;

public class QuizHome extends AppCompatActivity {

    private LinearLayout quizSection; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_home);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        quizSection = findViewById(R.id.quiz_subjects);
        
        UserSettings.checkIfInit(this, UtilityFunctions.getUserNameFromFirebase());
        

        if (!UtilityFunctions.doesUserHaveConnection(this)) {
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
            onBackPressed(); // network senstive section
        }


        loadOptions();
    }

    /**
     *  loads the quizes for the user to choose from
     */
    private void loadOptions() {
        final LinearLayout quizSection = findViewById(R.id.quiz_subjects);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("quiz");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {
                Random rnd = new Random();

                int counter = rnd.nextInt(10) + 1;
                for (final DataSnapshot quizTopics : dataSnapshot.getChildren()) {

                    View view = LayoutInflater.from(quizSection.getContext()).inflate(R.layout.contact_button, null);
                    LinearLayout layout = view.findViewById(R.id.contact_button);
                    view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + UtilityFunctions.getHexColor(counter++))));

                    TextView textView = view.findViewById(R.id.contact_text);

                    String title = quizTopics.getKey();
                    String[] split = title.split("_");

                    title = "";

                    for(int i = 0; i < split.length; i++)
                    {
                        title += split[i].substring(0,1).toUpperCase() +  split[i].substring(1,split[i].length()).toLowerCase() + " ";
                    }

                    textView.setText(title);

                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            quizSection.removeAllViews();
                            loadQuizTopics(quizTopics.getKey());
                        }
                    });

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 20);

                    quizSection.addView(view, params);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // gets the quizes for the choosen subject
    private void loadQuizTopics(String key) 
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("quiz/" + key);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                Random rnd = new Random();
                int counter = rnd.nextInt(10) + 1;

                for (final DataSnapshot quizTitle : dataSnapshot.getChildren()) {
                    if(quizTitle.getKey().equalsIgnoreCase("course_ids") || quizTitle.getKey().equalsIgnoreCase("quiz_master"))
                        continue;

                    View view = LayoutInflater.from(quizSection.getContext()).inflate(R.layout.contact_button, null);
                    LinearLayout layout = view.findViewById(R.id.contact_button);

                    view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + UtilityFunctions.getHexColor(counter++))));

                    TextView textView = view.findViewById(R.id.contact_text);
                    String quiz_name = quizTitle.getKey().replace("_", " ");
                    String[] nameSplit = quiz_name.split(" ");

                    quiz_name = "";
                    for(int i = 0; i < nameSplit.length;i++)
                    {
                        quiz_name += nameSplit[i].substring(0,1).toUpperCase() + nameSplit[i].substring(1, nameSplit[i].length()).toLowerCase() + " ";
                    }

                    textView.setText(quiz_name);

                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startQuiz(dataSnapshot.getKey() +"/" + quizTitle.getKey());
                        }
                    });

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, 20);

                    quizSection.addView(view, params);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startQuiz(String key)
    {
        Intent intent = new Intent(this, Quiz.class);
        intent.putExtra("quiz", key);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
