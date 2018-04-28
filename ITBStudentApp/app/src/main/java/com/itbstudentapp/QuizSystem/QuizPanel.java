package com.itbstudentapp.QuizSystem;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

public class QuizPanel extends AppCompatActivity {

    private LinearLayout quizSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_panel);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        quizSection = findViewById(R.id.quiz_subjects);


        getListOfQuizes();

    }

    // gets the list of quizes and adds a listener to allow them to be selected
    private void getListOfQuizes() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("quiz");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                for (final DataSnapshot quiz : dataSnapshot.getChildren()) {
                    String quizMaster = quiz.child("quiz_master").getValue(String.class);

                    if (quizMaster.equalsIgnoreCase(UtilityFunctions.getUserNameFromFirebase())) {
                        View view = LayoutInflater.from(quizSection.getContext()).inflate(R.layout.quiz_subject_detail, null);

                        TextView subjectName = view.findViewById(R.id.quiz_subject);

                        String title = quiz.getKey();
                        String[] split = title.split("_");

                        title = "";

                        for(int i = 0; i < split.length; i++)
                        {
                            title += split[i].substring(0,1).toUpperCase() +  split[i].substring(1,split[i].length()).toLowerCase() + " ";
                        }

                        subjectName.setText(title);

                        TextView quiz_numbers = view.findViewById(R.id.quiz_amount);
                        quiz_numbers.setText(quiz.getChildrenCount() - 2 + " Quizes availible");

                        ImageView editQuiz = view.findViewById(R.id.quiz_edit);
                        editQuiz.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadQuizEditScreen(quiz.getKey());
                            }
                        });

                        ImageView addQuiz = view.findViewById(R.id.quiz_add);
                        addQuiz.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadQuiz(quiz.getKey(), true);
                            }
                        });

                        quizSection.addView(view);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadQuiz(String key, boolean isNew) {
        Intent newQuiz = new Intent(this, QuizManagement.class);
        newQuiz.putExtra("quiz_topic", key);
        newQuiz.putExtra("new_quiz", isNew);

        startActivity(newQuiz);
        finish();
    }

    private void loadQuizEditScreen(String quizKey)
    {
        Dialog menu = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        menu.setContentView(R.layout.quiz_choice);
        menu.show();

        final LinearLayout layout = menu.findViewById(R.id.quiz_display_section);
        final Context ct = this;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("quiz/" + quizKey);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                for(final DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(snapshot.getKey().equalsIgnoreCase("course_ids") || snapshot.getKey().equalsIgnoreCase("quiz_master"))
                        continue;

                    View view = LayoutInflater.from(ct).inflate(R.layout.quiz_title_detail, null);
                    TextView title = view.findViewById(R.id.quiz_name);
                    title.setText(snapshot.getKey());

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent quizEdit = new Intent(layout.getContext(), QuizManagement.class);
                            quizEdit.putExtra("isNew", false);
                            String path = "quiz/" + dataSnapshot.getKey() + "/" + snapshot.getKey();
                            quizEdit.putExtra("quiz_topic", path);
                            layout.getContext().startActivity(quizEdit);
                            ((Activity)ct).finish();
                        }
                    });

                    layout.addView(view);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}

