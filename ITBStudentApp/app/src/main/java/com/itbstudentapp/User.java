package com.itbstudentapp;

public class User {

    private String username;
    private String courseID;
    private String accountType;
    private String email;
    private String imageLink;


    public User(){}

    public User(String username, String courseID, String accountType, String email) {
        this.username = username;
        this.courseID = courseID;
        this.accountType = accountType;
        this.email = email;
    }

    public User(String username, String courseID, String accountType, String email, String imageLink) {
        this.username = username;
        this.courseID = courseID;
        this.accountType = accountType;
        this.email = email;
        this.imageLink = imageLink;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}