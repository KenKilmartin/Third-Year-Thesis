package com.itbstudentapp.QuizSystem;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.Random;

public class Quiz extends AppCompatActivity implements View.OnClickListener {

    private int numberOfQuestions = 5;
    private ArrayList<Question> questions;
    private Question[] askedQuestions;
    private QuizAnswerSheet[] answerSheets;

    private TextView submit, home;
    private boolean quizEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!UtilityFunctions.doesUserHaveConnection(this))
        {
            Toast.makeText(this, "No network connection. Please try again.", Toast.LENGTH_SHORT ).show();
            startActivity(new Intent(this, MainActivity.class));
            finish(); // network sensitive section
        }

        setContentView(R.layout.activity_quiz2);// load the quiz view
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        questions = new ArrayList<>(); // create a array list to hold questions

        Bundle bundle = getIntent().getExtras();
        String quiz = bundle.getString("quiz"); // the quiz we choose

        String quiz_title = quiz.split("/")[1].replace("_", " ");
        quiz_title = quiz_title.substring(0,1).toUpperCase() + quiz_title.substring(1, quiz_title.length()).toLowerCase();
        TextView title = findViewById(R.id.quiz_title);
        title.setText(quiz_title);

        getQuestions(quiz);
        setupButtons();
    }

    private void setupButtons()
    {
        submit = findViewById(R.id.quiz_submit);
        submit.setOnClickListener(this);

        home = findViewById(R.id.quiz_home);
        home.setOnClickListener(this);
    }

    private void getQuestions(String quiz)
    {
        // get the questions within that quiz
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("quiz/" + quiz);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    Question q = new Question();
                    q.setQuestion(snap.child("question").getValue(String.class));
                    q.setAnswer((ArrayList<String>) snap.child("answer").getValue());
                    q.setPossibleAnswers((ArrayList<String>) snap.child("possibleAnswers").getValue());

                    // get all the questions
                    questions.add(q);
                }

                // choose a selection from them
                getPickedQuestions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPickedQuestions()
    {
        if(questions.size() < numberOfQuestions) // if we have less questions then the max, allow all
        {
            askedQuestions = questions.toArray(new Question[questions.size()]);
            answerSheets = new QuizAnswerSheet[questions.size()];
            displayQuestions();
            questions = null;
            return;
        } else{
            // else get a array of them
            askedQuestions = new Question[numberOfQuestions];
            answerSheets = new QuizAnswerSheet[questions.size()];
        }

        Random rnd = new Random(); // random choice

        // get the questions
        for(int i = 0; i < askedQuestions.length; i++)
        {
            int randChoice = rnd.nextInt(questions.size());
            askedQuestions[i] = questions.remove(randChoice);
        }

        displayQuestions();
        questions = null;
    }

    // show the questions we have choosen
    private void displayQuestions()
    {
        LinearLayout linearLayout = findViewById(R.id.quiz_questions);

        for(int i = 0; i < askedQuestions.length; i++)
        {
            View questions_panel = LayoutInflater.from(this).inflate(R.layout.question_panel, null);
            TextView question = questions_panel.findViewById(R.id.question);
            question.setText(askedQuestions[i].getQuestion());

            ArrayList<String> answers = askedQuestions[i].getPossibleAnswers();

            answerSheets[i] = new QuizAnswerSheet(answers.size(), askedQuestions[i].answer.size());
            randomizeQuestions(answers, questions_panel, askedQuestions[i].answer, answerSheets[i]);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 20, 20,20);


            linearLayout.addView(questions_panel, params);
        }
    }

    // make sure they are swap around to avoid patterns emerging
    private void randomizeQuestions(ArrayList<String> answers, View questions_panel, ArrayList<String> correctAnswers, final QuizAnswerSheet answerSheet)
    {
        Random rnd = new Random();

        ViewGroup answers_panel = questions_panel.findViewById(R.id.answer_panel);
        int counter = 0;
        int correctAmount = 0;

        while (answers.size() > 0)
        {
            View answer_section = LayoutInflater.from(this).inflate(R.layout.answer_panel, null);
            TextView answerText = answer_section.findViewById(R.id.answer_text);

            int answerSelection = rnd.nextInt(answers.size());

            if(correctAnswers.contains(answers.get(answerSelection)))
            {
                answerSheet.correctBoxes[correctAmount++] = counter;
            }

            answerSheet.checkedBoxes[counter] = false;
            answerText.setText(answers.remove(answerSelection));

            final CheckBox checkBox = answer_section.findViewById(R.id.answer_check);
            checkBox.setTag(counter);
            counter++;

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if(isChecked)
                    {
                        answerSheet.checkedBoxes[(int)checkBox.getTag()] = true;
                    } else{
                        answerSheet.checkedBoxes[(int) checkBox.getTag()] = false;
                    }
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(60, 5, 5,5);

            answers_panel.addView(answer_section, params);
        }
    }

    /**
     * used to make sure the student took the quiz as intended
     */
    private void handleQuizEnd()
    {
        float currentScore = 0;
        boolean questionsWrong[] = new boolean[answerSheets.length];

        for(int i = 0; i < answerSheets.length;i++)
        {
            boolean hasSomeFalse = false;
            int correct = 0;

            for(int x = 0; x < answerSheets[i].checkedBoxes.length; x++)
            {
                int scorePerRound = 0;
                float amountPerCorrect = 1.0f/ answerSheets[i].correctBoxes.length;

                if(!answerSheets[i].checkedBoxes[x])
                {
                    hasSomeFalse = true;
                }

                for(int p = 0; p < answerSheets[i].correctBoxes.length; p++)
                {
                    if(answerSheets[i].correctBoxes[p] == x)
                    {
                        if(answerSheets[i].checkedBoxes[x])
                        {
                            scorePerRound += amountPerCorrect;
                            correct++;
                        }
                    }
                }

                currentScore += scorePerRound;
            }

            if(!hasSomeFalse)
            {
                Toast.makeText(this,"You can not just click all the answers", Toast.LENGTH_SHORT).show();
                return;
            }

            if(correct != answerSheets[i].correctBoxes.length)
            {
                questionsWrong[i] = true;
            }
        }

        showResults(questionsWrong, currentScore);
    }

    // gets the quiz score of the student
    private void showResults(boolean[] questionsWrong, float currentScore)
    {
        LinearLayout linearLayout = findViewById(R.id.quiz_questions);
        TextView result_section = findViewById(R.id.quiz_result);

        for(int i = 0; i < questionsWrong.length; i++)
        {
            View view = linearLayout.getChildAt(i + 2);
            ImageView imageView = view.findViewById(R.id.res_image);

            if(questionsWrong[i] == true)
            {
                view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + "ef2300")));
                setImageView(imageView, false);
            } else{
                view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#cc" + "1fd304")));
                setImageView(imageView, true);
            }
        }
        float res = (100 / questionsWrong.length ) * currentScore;
        result_section.setText("Result : " + res + " % ");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        result_section.setLayoutParams(params);
    }

    private void setImageView(ImageView imageView, boolean correct)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
        params.setMargins(0,40,40,40);
        imageView.setLayoutParams(params);
        params.gravity = Gravity.RIGHT;

        if(correct)
            imageView.setImageResource(R.drawable.tick);
        else
            imageView.setImageResource(R.drawable.cross);

    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.quiz_submit) {
            handleQuizEnd();

            submit.setText("Retry");

            if(quizEnd)
            {
                startActivity(getIntent());
                finish();
            }

            quizEnd = true;

        }
        else if(v.getId() == R.id.quiz_home)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private class QuizAnswerSheet
    {
        public boolean[] checkedBoxes;
        public int[] correctBoxes;

        public QuizAnswerSheet(int numberOfAnswers, int numOfCorrectAnswers)
        {
            this.checkedBoxes = new boolean[numberOfAnswers];
            this.correctBoxes = new int[numOfCorrectAnswers];
        }
    }
}
