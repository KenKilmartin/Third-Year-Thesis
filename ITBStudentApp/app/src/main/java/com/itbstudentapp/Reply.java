package com.itbstudentapp;


public class Reply {

    // must have
    private String posterID;
    private String posterComment;
    private long postTime;

    // may not have
    private String imageLink;

    public Reply(){}

    public Reply(String posterID, String posterComment, long postTime)
    {
        this.posterID = posterID;
        this.posterComment = posterComment;
        this.postTime = postTime;
    }

    public Reply(String posterID, String posterComment, long postTime, String imageLink)
    {
        this.posterID = posterID;
        this.posterComment = posterComment;
        this.postTime = postTime;
        this.imageLink = imageLink;
    }

    public String getPosterID() {
        return posterID;
    }

    public void setPosterID(String posterID) {
        this.posterID = posterID;
    }

    public String getPosterComment() {
        return posterComment;
    }

    public void setPosterComment(String posterComment) {
        this.posterComment = posterComment;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

}
