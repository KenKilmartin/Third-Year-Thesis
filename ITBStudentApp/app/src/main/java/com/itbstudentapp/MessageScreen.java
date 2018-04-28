package com.itbstudentapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.itbstudentapp.ChatSystem.Chat;
import com.itbstudentapp.ChatSystem.ChatSystemController;
import com.itbstudentapp.ChatSystem.ContactCard;
import com.itbstudentapp.ChatSystem.ContactRepository;

public class MessageScreen extends AppCompatActivity implements View.OnClickListener{

    private TextView reciepentList;
    private TextView contactListButton, sendButton;
    private EditText userInputField;

    private ImageController ic;
    private ChatSystemController csr;
    private boolean isNewMessage = true;
    private boolean isGroupMessage = false;
    private ContactCard contactCard;
    private String reciever;

    private Uri imageUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_screen);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));
        Bundle b = getIntent().getExtras();
        isNewMessage = (b.getString("message_id").equalsIgnoreCase("none")); // if we have none as a id, its a new message

        reciepentList = findViewById(R.id.message_screen_reciever_list);
        contactListButton = findViewById(R.id.message_screen_contact);
        sendButton = findViewById(R.id.message_screen_send);

        contactListButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        userInputField = findViewById(R.id.message_screen_text_box);

        csr = new ChatSystemController(this, isNewMessage, this); // get a controller

        if(isNewMessage) {
            contactListButton.setVisibility(View.VISIBLE);
            // if it is a new message, we allow the option of a contact list
        } else
        {
            // else we want to hide the contact button and load the messages
            contactListButton.setVisibility(View.INVISIBLE);
            reciever = b.getString("message_id");
            reciepentList.setText(UtilityFunctions.formatTitles(reciever));

            // if we have a group message
            if(reciever.contains("group_messages")) {
                loadGroupMessages(reciever);
                isGroupMessage = true;
                isNewMessage = false;
            }
            else
                // get the messages
                csr.loadChatMessages(reciever, (LinearLayout) findViewById(R.id.messages_screen_messages), (ScrollView) findViewById(R.id.chat_scroll_view));
        }

    }

    private void loadGroupMessages(String message_id)
    {
        // load the group messages
        String[] spiltLink = message_id.split("/");
        reciever = spiltLink[spiltLink.length - 1];
        csr.loadGroupMessages(reciever, (LinearLayout) findViewById(R.id.messages_screen_messages), (ScrollView) findViewById(R.id.chat_scroll_view));
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == contactListButton.getId())
        {
            csr.populateUserList(v);// if we have choosen to get users, load the list
        }

        if(v.getId() == sendButton.getId()) {
            handleNewMessage(); // send a new message

            if(isNewMessage)
            {
                // if its a new message, we check to make sure we have a reciever
                if(contactCard !=null) {
                    // load the message now
                    reciever = contactCard.getUser_id();
                    csr.loadChatMessages(reciever, (LinearLayout) findViewById(R.id.messages_screen_messages), (ScrollView) findViewById(R.id.chat_scroll_view));
                    // it is no longer a new message
                    isNewMessage = false;
                }
            }
        }
    }

    public void setReciever(ContactCard userToMessage)
    {
        // adds the reciever to the top bar
        this.contactCard = userToMessage;
        reciepentList.setText(userToMessage.getUser_name());
    }

    private void handleNewMessage()
    {
        if(contactCard != null || reciever != null){
            String message = userInputField.getText().toString();

            if(!UtilityFunctions.doesUserHaveConnection(this))
            {
                Toast.makeText(this, "No network connection. Please try again later.", Toast.LENGTH_LONG).show();
                return; // make sure we have network
            }

            if(message.length() <= 0) // make sure we have a message
            {
                Toast.makeText(this, "Enter a message to send.", Toast.LENGTH_LONG).show();
                return;
            }

            String param = (reciever == null ? contactCard.getUser_id(): reciever ); // get the reciever

            // allow the controller to handle the rest
            if(!isGroupMessage)
                csr.sendMessage(param, userInputField.getText().toString(), imageUpload);
            else
                csr.sendGroupMessage(param, userInputField.getText().toString(), imageUpload);

            // clear the fields
            userInputField.setText("");
            imageUpload = null;
        }
        else
            // if we have no reciever
            Toast.makeText(this, "You must specify someone to send the message to.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            imageUpload = data.getData();
            ic.setUploadUri(imageUpload);
        }
    }

}
