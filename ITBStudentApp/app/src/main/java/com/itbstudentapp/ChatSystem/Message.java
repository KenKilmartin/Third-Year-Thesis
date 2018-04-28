package com.itbstudentapp.ChatSystem;

public class Message {

    private String sender;
    private String message;
    private long sendTime;
    private String imageLink;


    public Message(){} // must be done for firebase

    public Message(String sender, String message, long sendTime)
    {
        this.sender = sender;
        this.message = message;
        this.sendTime = sendTime;
    }

    public Message(String sender, String message, long sendTime, String imageLink)
    {
        this.sender = sender;
        this.message = message;
        this.sendTime = sendTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
