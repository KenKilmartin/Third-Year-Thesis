package com.itbstudentapp.ChatSystem;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itbstudentapp.MainActivity;
import com.itbstudentapp.MessageScreen;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

import java.util.*;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout message_scrollview;

    private TextView new_message_button;
    private TextView message_list_button;
    private TextView message_groups_button;
    private boolean isShowingUserMessages = true;
    private ChatSystemController csr;

    private EditText search_messages;
    private Chat instance;

    private ArrayList<View> views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        views = new ArrayList<>();
        instance = this;

        csr = new ChatSystemController(this);

        new_message_button = (TextView) findViewById(R.id.chat_new_message);
        new_message_button.setOnClickListener(this);

        message_groups_button = (TextView) findViewById(R.id.chat_group_messages);
        message_groups_button.setOnClickListener(this);

        message_list_button = findViewById(R.id.chat_user_messages);
        message_list_button.setOnClickListener(this);

        message_scrollview = (LinearLayout) findViewById(R.id.message_list);


        csr.setScrollView(message_scrollview);
        csr.loadUserMessages();
    }

    public void onClick(View v)
    {
        if(v.getId() == R.id.chat_new_message)
        {
            loadNewMessageScreen();
        }

        if(v.getId() == R.id.chat_group_messages && isShowingUserMessages)
        {
            getGroupMessages();
            isShowingUserMessages = false;
        } else if(v.getId() == R.id.chat_user_messages && !isShowingUserMessages)
        {
            message_scrollview.removeAllViews();
            csr.loadUserMessages();
            isShowingUserMessages = true;
        }
    }

    private void loadNewMessageScreen()
    {
        Intent intent = new Intent(this, MessageScreen.class);
        intent.putExtra("message_id", "none");
        startActivity(intent);
    }

    private void getGroupMessages()
    {
        message_scrollview.removeAllViews();
        csr.loadGroupMessageList(message_scrollview);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

