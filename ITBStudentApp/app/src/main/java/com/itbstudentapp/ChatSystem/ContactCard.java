package com.itbstudentapp.ChatSystem;

public class ContactCard {
    private String user_id;
    private String user_name;
    private String user_image;
    private UserType userAccountType;

    public ContactCard(String user_id, String user_name, String user_image, UserType userAccountType) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_image = user_image;
        this.userAccountType = userAccountType;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public UserType getUserAccountType() {
        return userAccountType;
    }

    public void setUserAccountType(UserType userAccountType) {
        this.userAccountType = userAccountType;
    }
}
