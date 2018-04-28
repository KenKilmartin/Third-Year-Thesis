package com.itbstudentapp.EventSystem;

public class Event
{
    private String eventTitle;
    private String eventMessage;
    private long eventPostDate;
    private long eventValidTill;
    private String eventImage;

    public Event(){}

    public Event(String eventTitle, String eventMessage, long eventPostDate, long eventValidTill, String eventImage)
    {
        this.eventTitle = eventTitle;
        this.eventMessage = eventMessage;
        this.eventPostDate = eventPostDate;
        this.eventValidTill = eventValidTill;
        this.eventImage = eventImage;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public long getEventPostDate() {
        return eventPostDate;
    }

    public void setEventPostDate(long eventPostDate) {
        this.eventPostDate = eventPostDate;
    }

    public long getEventValidTill() {
        return eventValidTill;
    }

    public void setEventValidTill(long eventValidTill) {
        this.eventValidTill = eventValidTill;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }
}
