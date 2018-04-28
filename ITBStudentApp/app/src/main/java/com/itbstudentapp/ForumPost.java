package com.itbstudentapp;

import java.util.ArrayList;

public class ForumPost {

    private String postTitle;
    private String posterID;
    private String postComment;
    private long postTime;

    private ArrayList<Reply> postReplies;
    private String fileUpload;

    public ForumPost()
    {
        postReplies = new ArrayList<Reply>();
    }

    public ForumPost(String postTitle, String posterID, String postComment, long postTime) {
        this.postTitle = postTitle;
        this.posterID = posterID;
        this.postComment = postComment;
        this.postTime = postTime;

        postReplies = new ArrayList<Reply>();
    }

    public ForumPost(String postTitle, String posterID, String postComment, long postTime, String fileUpload) {
        this.postTitle = postTitle;
        this.posterID = posterID;
        this.postComment = postComment;
        this.postTime = postTime;
        this.fileUpload = fileUpload;

        postReplies = new ArrayList<Reply>();
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPosterID() {
        return posterID;
    }

    public void setPosterID(String posterID) {
        this.posterID = posterID;
    }

    public String getPostComment() {
        return postComment;
    }

    public void setPostComment(String postComment) {
        this.postComment = postComment;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public ArrayList<Reply> getPostReplies() {
        return postReplies;
    }

    public void setPostReplies(ArrayList<Reply> postReplies) {
        this.postReplies = postReplies;
    }

    public void AddReplyToTopic(Reply r)
    {
        this.postReplies.add(r);
    }

    public String getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(String fileUpload) {
        this.fileUpload = fileUpload;
    }

    public void addReplyToList(Reply r)
    {
        if(postReplies == null)
            postReplies = new ArrayList<Reply>();

        this.postReplies.add(r);
    }
}
