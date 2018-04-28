package com.itbstudentapp.NotificationSystem;

import java.util.HashMap;
import java.util.Map;



public class Notification
{
    private String notificationType;

    private String title;
    private String body;

    private String messageSender;

    public Notification(String notificationType, String title, String body, String messageSender)
    {
        this.notificationType = notificationType;
        this.title = title;
        this.body = body;
        this.messageSender = messageSender;
    }

    public Notification(String notificationType, String title, String body) {
        this.notificationType = notificationType;
        this.title = title;
        this.body = body;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public HashMap<String, String> getHashmapWithUser()
    {
        HashMap <String, String> notification = new HashMap<>();

        notification.put("type", notificationType);
        notification.put("title", title);
        notification.put("body", body);
        notification.put("messageSender", messageSender);

        return notification;
    }

    public HashMap<String, String> getHashmapWithoutUser()
    {
        HashMap <String, String> notification = new HashMap<>();

        notification.put("type", notificationType);
        notification.put("title", title);
        notification.put("body", body);

        return notification;
    }
}
