package com.itbstudentapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class ReplyModal extends DialogFragment implements View.OnClickListener {

    private View v;
    private static String postLink;

    private LinearLayout messageSection;
    private EditText userReply;
    private TextView replyButton;
    private TextView imageUpload;

    private ForumPost forumPost;
    private static int request_code = 1;
    private Uri image;


    static ReplyModal newInstance()
    {
        return new ReplyModal();
    }

    public void init(ForumPost forumPost, String dbPath)
    {
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.forumPost = forumPost;
        this.postLink = dbPath;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.forum_comments_modal, container, false);

        this.forumPost = forumPost;
        this.postLink = postLink;

        messageSection = v.findViewById(R.id.forum_reply_posts);
        userReply = v.findViewById(R.id.forum_reply_text);
        replyButton = v.findViewById(R.id.forum_reply_button);
        replyButton.setOnClickListener(this);

        imageUpload = v.findViewById(R.id.forum_replay_image);
        imageUpload.setOnClickListener(this);

        addCommentsToModal();
        return v;
    }

    private void addCommentsToModal()
    {
        ForumManager forumManager = new ForumManager(v.getContext());
        forumManager.setListenerBoolToFalse();
        forumManager.giveFragmentManager(getFragmentManager());
        forumManager.addReplysToModal(messageSection, forumPost, postLink);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == imageUpload.getId())
        {
            uploadImage();
            return;
        }

        if(userReply.getText().length() <= 0)
        {
            Toast.makeText(v.getContext(), "You must enter a message to post", Toast.LENGTH_SHORT).show();
            return;
        }

        ForumManager forumManager = new ForumManager(v.getContext());
        boolean success = forumManager.addReplyToDatabase(postLink, forumPost.getPostReplies().size(), userReply.getText().toString(), image);

        if(success)
        {
            userReply.setText("");
            image = null;
        }

    }

    private void uploadImage()
    {
        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Pick a file to upload"), request_code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == request_code)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                image = data.getData();
            }
        }
    }
}



