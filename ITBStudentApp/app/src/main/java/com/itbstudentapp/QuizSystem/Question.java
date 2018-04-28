package com.itbstudentapp.QuizSystem;

import java.util.ArrayList;



public class Question
{
    public String question;
    public ArrayList<String> answer;
    public ArrayList<String> possibleAnswers;

    public Question()
    {
        possibleAnswers = new ArrayList<>();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<String> getAnswer() {
        return answer;
    }

    public void setAnswer(ArrayList answer) {
        this.answer = answer;
    }

    public ArrayList<String> getPossibleAnswers() {
        return possibleAnswers;
    }

    public void setPossibleAnswers(ArrayList<String> possibleAnswers) {
        this.possibleAnswers = possibleAnswers;
    }
}
