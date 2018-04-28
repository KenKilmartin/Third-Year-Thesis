package com.itbstudentapp.QuizSystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

import java.util.ArrayList;
import java.util.Vector;

/**
 *  used to create new quizes
 */
public class QuizManagement extends AppCompatActivity implements View.OnClickListener {
    
    private boolean isNew;
    private String reference;

    private ArrayList<Question> questions;
    private EditText quizTitle;

    private boolean hasDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!UtilityFunctions.doesUserHaveConnection(this))
        {
            Toast.makeText(this, "No network connection. Please try again later", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish(); // network senstive section
        }

        setContentView(R.layout.activity_quiz_management);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));

        questions = new ArrayList<>();
        quizTitle = findViewById(R.id.quiz_title);
        
        Bundle bundle = getIntent().getExtras();
        isNew = bundle.getBoolean("new_quiz");
        reference = bundle.getString("quiz_topic");
        
        if(!isNew) // if its not a new quiz we are creating
        {
            loadQuestionsFromQuiz(); // load the questions for the quiz
        }

        initializeSettings();
    }

    private void initializeSettings()
    {
        TextView save_button, add_question;

        save_button = findViewById(R.id.quiz_save);
        save_button.setOnClickListener(this);

        add_question = findViewById(R.id.addQuestion);
        add_question.setOnClickListener(this);

    }

    // for each question in the quiz, we want to display it
    public void addQuestionToDisplay(final Question question, int index)
    {
        final View view = LayoutInflater.from(this).inflate(R.layout.event_detail, null);
        final QuizManagement quizManagement = this;

        TextView questionTitle = view.findViewById(R.id.event_title);

        String questionText = question.getQuestion();

        if(questionText.length() > 15) { // make sure its not over 19 charactors long
            String edittedQuestion = questionText.substring(0, 15) + "...";
            questionTitle.setText(edittedQuestion);
        } else {
            questionTitle.setText(questionText);
        }

        ImageView edit_question = view.findViewById(R.id.event_edit); // add button to edit it
        edit_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new QuestionDialog(v.getContext(), quizManagement, question, questions.indexOf(question));
            }
        });

        ImageView remove_question = view.findViewById(R.id.event_remove); // remove the question
        remove_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questions.remove(question);
                ((ViewGroup)view.getParent()).removeView(view);
                hasDeleted = true;
            }
        });

        if(index < 0) // if its the first question to be added
            ((LinearLayout)findViewById(R.id.question_section)).addView(view);
        else // makes sure that it is added in the right place
            ((LinearLayout)findViewById(R.id.question_section)).addView(view, index);
    }

    public void pushQuestionToList(Question question)
    {
        questions.add(question);
        addQuestionToDisplay(question, -1);
    }

    // gets each of our questions in the quiz and adds it to the list
    private void loadQuestionsFromQuiz()
    {
        final Context ct = this;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(reference);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProgressDialog progressDialog = new ProgressDialog(ct);
                progressDialog.setTitle("Retrieving quiz list");
                progressDialog.setMessage("Loading");
                progressDialog.show();

                String spacedTitle = dataSnapshot.getKey().replace("_", " ");
                quizTitle.setText(spacedTitle);

                for(DataSnapshot snap : dataSnapshot.getChildren())
                {

                    Question q = new Question();
                    q.setQuestion(snap.child("question").getValue(String.class));
                    q.setAnswer((ArrayList<String>) snap.child("answer").getValue());
                    q.setPossibleAnswers((ArrayList<String>)snap.child("possibleAnswers").getValue());

                    pushQuestionToList(q);
                }

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.addQuestion)
        {
            new QuestionDialog(this, this);
        }

        if(v.getId() == R.id.quiz_save)
        {
            if(quizTitle.getText().length() <= 0)
            {
                Toast.makeText(this, "You must have a quiz title.", Toast.LENGTH_SHORT).show();
                return;
            } else if(questions == null || questions.size() == 0)
            {
                Toast.makeText(this, "You must add questions to your quiz", Toast.LENGTH_SHORT).show();
                return;
            }

            saveQuestionToDatabase();
            startActivity(new Intent(this, QuizPanel.class));
            finish();
        }
    }

    private void saveQuestionToDatabase() // save the new questions to the database
    {
        String removedSpaces = quizTitle.getText().toString().replace(" ", "_");
        DatabaseReference ref;

        if(!isNew)
            ref = FirebaseDatabase.getInstance().getReference(reference);
        else
            ref = FirebaseDatabase.getInstance().getReference("quiz/" + reference + "/" + removedSpaces);

        if(!isNew && hasDeleted)
        {
            ref.removeValue();
        }

        for(int i = 0; i < questions.size(); i++)
        {
            ref.child(String.valueOf(i + 1)).setValue(questions.get(i));
        }
    }


    public void editQuestion(Question question_instance, int questionID)
    {
        ((LinearLayout)findViewById(R.id.question_section)).removeViewAt(questionID);
        addQuestionToDisplay(question_instance, questionID);
        questions.remove(questionID);
        questions.add(questionID, question_instance);
    }


}


