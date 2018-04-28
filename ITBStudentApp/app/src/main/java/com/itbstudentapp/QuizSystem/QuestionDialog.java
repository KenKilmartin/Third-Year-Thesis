package com.itbstudentapp.QuizSystem;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itbstudentapp.QuizSystem.Question;
import com.itbstudentapp.QuizSystem.QuizManagement;
import com.itbstudentapp.R;

import java.util.ArrayList;
import java.util.Vector;

/**
 * used for adding new questions and quizes
 */
public class QuestionDialog extends Dialog implements View.OnClickListener
{
    private QuizManagement quizManagement;
    private EditText question;

    private ArrayList<EditText> answers;
    private ArrayList<CheckBox> answerRadio;
    private LinearLayout answerPanel;

    private boolean isEditing = false;
    private int questionID;

    public QuestionDialog(@NonNull Context context, QuizManagement quizManagement)
    {
        super(context);
        this.quizManagement = quizManagement;
        setContentView(R.layout.question_add_dialog);
        answerPanel = this.findViewById(R.id.dialog_answer_panel);
        question = this.findViewById(R.id.dialog_question_text);

        setupButtonListeners();
        show();
    }

    public QuestionDialog(@NonNull Context context, QuizManagement quizManagement, Question edit_question, int questionId)
    {

        this(context, quizManagement);

        isEditing = true;
        this.questionID = questionId;

        setQuestionDetails(edit_question);
        setupButtonListeners();
        show();
    }

    private void setQuestionDetails(Question q)
    {
        question.setText(q.getQuestion());

        for(int i = 0; i < q.possibleAnswers.size(); i++)
        {
            this.addAnswerPanel();
            String currentAnswer = q.possibleAnswers.get(i);
            answers.get(i).setText(currentAnswer);

            if(q.answer.contains(currentAnswer))
                answerRadio.get(i).setChecked(true);

        }
    }

    private void setupButtonListeners()
    {
        TextView addAnswer, cancel, save;

        addAnswer = this.findViewById(R.id.dialog_add_answer);
        addAnswer.setOnClickListener(this);

        cancel = this.findViewById(R.id.dialog_question_cancel);
        cancel.setOnClickListener(this);

        save = this.findViewById(R.id.dialog_question_add);
        save.setOnClickListener(this);

        if(isEditing)
            save.setText("Edit");
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.dialog_add_answer)
        {
            addAnswerPanel(); // if we want to add another answer

        } else if(v.getId() == R.id.dialog_question_cancel)
        {
            dismiss(); // we cancel adding a question
        } else if(v.getId() == R.id.dialog_question_add)
        {

            if(question.getText().length() <= 0) { // make sure the question has text
                Toast.makeText(this.getContext(), "You must enter a question", Toast.LENGTH_SHORT).show();
                return;
            }

            Question question_instance = new Question();
            question_instance.setQuestion(question.getText().toString());

            if(answers == null || answers.size() < 2)
            {
                // we want to make sure we have at least two options
                Toast.makeText(this.getContext(), "You must have at least two possible answers", Toast.LENGTH_SHORT).show();
                return;
            } else {

                boolean hasEnteredTwoAnswers = false;
                int counter = 0;

                for(int i = 0; i < answers.size(); i++)
                {
                    String curAnswer = answers.get(i).getText().toString();

                    if(curAnswer.length() > 0 && !curAnswer.equalsIgnoreCase(" "))
                    {
                        question_instance.possibleAnswers.add(curAnswer);
                        counter++;

                        if(counter >= 2 )
                        {
                            hasEnteredTwoAnswers = true; //  makes sure the second answer has text
                        }
                    }
                }

                if(!hasEnteredTwoAnswers)
                {
                    question_instance.possibleAnswers = new ArrayList<>();
                    Toast.makeText(this.getContext(), "You must have at least two possible answers", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean hasAnswers = false;
                int answerCounter = 0; // we can allow multiple answers

                for(int i = 0; i < answerRadio.size(); i++)
                {
                    if(answerRadio.get(i).isChecked()) {
                        hasAnswers = true;
                        answerCounter++;
                    }
                }

                if(!hasAnswers) // to ensure there is at least one answer
                {
                    Toast.makeText(this.getContext(), "You must have at least one answers", Toast.LENGTH_SHORT).show();
                    return;
                } else if(answerCounter >= answerRadio.size())
                {
                    Toast.makeText(this.getContext(), "You must have at least one wrong answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<String> currentAnswers = new ArrayList<>();

                for(int i = 0; i < answerRadio.size(); i++)
                {
                    if(answerRadio.get(i).isChecked()) // adds the answers to the array list
                        currentAnswers.add(answers.get(i).getText().toString());
                }
                question_instance.setAnswer(currentAnswers); // sets the question with the answers
                if(isEditing) // if we are editting the question,
                {
                    quizManagement.editQuestion(question_instance, questionID);
                } else { // otherwise its a new question
                    quizManagement.pushQuestionToList(question_instance);
                }
                dismiss();
            }
        }
    }

    private void addAnswerPanel()
    {
        View answer_section = LayoutInflater.from(this.getContext()).inflate(R.layout.answer_edit_section, null);
        final CheckBox answer_radio = answer_section.findViewById(R.id.answer_radio);

        EditText answer_text = answer_section.findViewById(R.id.dialog_answer_text);

        if(answers == null)
            answers = new ArrayList<>();

        if(answerRadio == null)
            answerRadio = new ArrayList<>();

        answers.add(answer_text);
        answerRadio.add(answer_radio);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(20,30,30,20);

        answerPanel.addView(answer_section,params);
    }
}