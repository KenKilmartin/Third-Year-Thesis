package com.itbstudentapp.AdminSystem;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.itbstudentapp.MainActivity;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

public class AdminPanel extends AppCompatActivity implements View.OnClickListener {

    /**
     * creates the admin panel
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel2);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));

        setupButtons();
    }

    /**
     * setting up the buttons on the panel
     */
    private void setupButtons()
    {
        TextView admin_mod = findViewById(R.id.admin_moderator);
        admin_mod.setOnClickListener(this);

        TextView admin_quiz = findViewById(R.id.admin_quiz_panel);
        admin_quiz.setOnClickListener(this);

        TextView admin_report = findViewById(R.id.admin_forum_reports);
        admin_report.setOnClickListener(this);

        TextView home_button = findViewById(R.id.admin_home);
        home_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.admin_home)
        {
            startActivity(new Intent(this, MainActivity.class)); // brings the user to the home page
            finish(); // ends the current activity
        } else if(v.getId() == R.id.admin_moderator)
        {
            showModeratorDialog(); // the dialog that sets the modarators for the app
        } else if(v.getId() == R.id.admin_quiz_panel)
        {
            setQuizMaster(); // setting a new quiz master
        } else if(v.getId() == R.id.admin_forum_reports)
        {
            startActivity(new Intent(this, ReportedPost.class)); // the reported posts from the forum
            finish();
        }

    }

    private void showModeratorDialog()
    {
        new ModeratorManager(this);
    } // creates the class of the dialog of each

    private void setQuizMaster()
    {
        new QuizMasterManager(this);
    }// creates the quiz master dialog

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this, MainActivity.class)); // if we dont do this step. our event system can get messed up
                                                                            // and the back button can be unpredictable
        finish();
    }
}
