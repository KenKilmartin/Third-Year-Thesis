package com.itbstudentapp.ChatSystem;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itbstudentapp.ImageController;
import com.itbstudentapp.MessageScreen;
import com.itbstudentapp.NotificationSystem.FirebaseNotificationManager;
import com.itbstudentapp.NotificationSystem.Notification;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatSystemController {

    private Context context;
    private String[] groupList;
    private boolean checkedGroupList = false;

    private ArrayList<View> views;
    private LinearLayout messageScrollView;
    private ContactRepository contactRepository;
    private MessageScreen messageScreen;

    private boolean shouldListen = false;

    public ChatSystemController(Context context) {
        this.context = context;

        if (context.getClass().getName().equals(Chat.class.getName())) {
            loadUserDetails();
            views = new ArrayList<>();
        }
    }

    public ChatSystemController(Context context, boolean isNew, MessageScreen messageScreen) {
        this.context = context;
        this.messageScreen = messageScreen;

        if (isNew)
            contactRepository = new ContactRepository();
    }

    public void loadGroupMessageList(final LinearLayout message_scrollview) {
        shouldListen = false;
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!checkedGroupList) ; // just to add a delay for firebase to get all the information

                for (int i = 0; i < groupList.length; i++) { // for each of the group messages
                    final DatabaseReference db = FirebaseDatabase.getInstance().getReference("group_messages/" + groupList[i]);
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) { // if we have a message, add it to the message list
                                loadMessageToList(message_scrollview, dataSnapshot);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        th.start();
    }

    /**
     *  generates the view for each of the messages
     * @param message_scrollview
     * @param dataSnapshot
     */
    private void loadMessageToList(LinearLayout message_scrollview, final DataSnapshot dataSnapshot) {
        View v = LayoutInflater.from(context).inflate(R.layout.message_preview, null);

        TextView messageName = v.findViewById(R.id.message_sender);
        String chatTitle = UtilityFunctions.formatTitles(dataSnapshot.getKey());
        messageName.setText(chatTitle);

        TextView timeStamp = v.findViewById(R.id.message_date); // gets the proper time
        String tStamp = UtilityFunctions.milliToTime(dataSnapshot.child("message_info").child("time_stamp").getValue(Long.class));
        timeStamp.setText(tStamp);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageScreen.class);
                intent.putExtra("message_id", dataSnapshot.getRef().toString());
                context.startActivity(intent);
            }
        });
        message_scrollview.addView(v);
    }

    public void loadUserDetails() { //gets the users list of groups
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userGroups = dataSnapshot.child("groups").getValue(String.class);
                groupList = userGroups.split(":");
                checkedGroupList = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setScrollView(LinearLayout scrollView) {
        this.messageScrollView = scrollView;
    }

    public void loadUserMessages() {
        shouldListen = false; // to avoid messages generating multiple times
        setUpMenuListener();
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // this is used to put messages in time order

                Map<Long, DataSnapshot> chatDB = new TreeMap<Long, DataSnapshot>(Collections.<Long>reverseOrder()); // add the messages to a map and then reverse it

                for (DataSnapshot data : dataSnapshot.child("messages").getChildren()) {
                    chatDB.put(data.child("message_info").child("time_stamp").getValue(Long.class), data);
                }

                setUpMessages(chatDB);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void setUpMenuListener()
    {
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase() + "/messages");
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(shouldListen) // once all the messages are in, we can listen for new and add them to the list
                    setMessageInView(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if(shouldListen) // if we have a child that gets a new message, we must add it to the top of the view
                   moveMessageToTop(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void moveMessageToTop(String viewTag)
    {
        shouldListen = false;

        for(View v : views)
        {
            if(v.getTag().toString().equalsIgnoreCase(viewTag))
            {
                View parent = (View) v.getParent();

                ((ViewGroup) parent).removeView(v);

                ((ViewGroup) parent).addView(v, 0);
            }
        }

        shouldListen = true;
    }

    private void setUpMessages(Map<Long, DataSnapshot> messageSnapshots) {

        for (Map.Entry data : messageSnapshots.entrySet()) {
            final DataSnapshot message = (DataSnapshot) data.getValue();
            setMessageInView(message);
        }

        shouldListen = true; // now we have finished listening to the messages we can start listening
    }

    private void setDetails(View v, DataSnapshot ds) {
        String read_status = ds.child("message_info").child("read_status").getValue(String.class);
        long lastUpdate; // get the latest time for the messgae

        if( ds.child("message_info").child("time_stamp").getValue() != null)
        {
            lastUpdate = ds.child("message_info").child("time_stamp").getValue(Long.class);
        }
        else{
            lastUpdate = Calendar.getInstance().getTimeInMillis();
        }

        if (read_status.equals(String.valueOf(UtilityFunctions.UNREAD))) {
            v.setBackgroundColor(context.getResources().getColor(R.color.unread_message));
        }

        TextView time_sent = v.findViewById(R.id.message_date);
        time_sent.setText(UtilityFunctions.milliToTime(lastUpdate));
    }

    /**
     *  method that adds each of our messages to the the view
     * @param ds
     */
    private void setMessageInView(final DataSnapshot ds) {
        final View v = LayoutInflater.from(context).inflate(R.layout.message_preview, null);
        final LinearLayout linear = v.findViewById(R.id.message_bg);

        setDetails(linear, ds);

        TextView sender = v.findViewById(R.id.message_sender);
        getUsername(ds.getKey(), sender);

        CircleImageView circleImageView = v.findViewById(R.id.message_user_image);
        UtilityFunctions.loadImageToView(ds.getKey(), context, circleImageView);//loadUserImage(circleImageView, snap.getKey());

        if (v.getParent() != null) {
            ((ViewGroup) v.getParent()).removeView(v);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageScreen.class);
                intent.putExtra("message_id", ds.getKey());
                ds.child("message_info").child("read_status").getRef().setValue(String.valueOf(UtilityFunctions.READ));
                linear.setBackgroundColor(context.getResources().getColor(R.color.read_message));
                context.startActivity(intent);
            }
        });

        v.setTag(ds.getKey());
        messageScrollView.addView(v);
        views.add(views.size(), v);
    }

    public void populateUserList(View v) {
        ArrayList<ContactCard> contactCards = contactRepository.getContactInformation();
        openDialog();
    }

    private void openDialog() { // gets the user list dialog
        ContactList contactList = new ContactList(context, contactRepository.getContactInformation(), this);
    }

    public void setCurrentContact(ContactCard userToMessage) {
        messageScreen.setReciever(userToMessage); // adds the user to be the reciever to the message
    }

    /**
     * gets users real name from the database
     * @param username
     * @param nameField
     */
    private void getUsername(String username, final TextView nameField) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + username);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);

                String[] name = username.split(" ");
                username = "";
                for (int i = 0; i < name.length; i++) {
                    name[i] = name[i].substring(0, 1).toUpperCase() + name[i].substring(1, name[i].length()).toLowerCase();
                    username += name[i] + " ";
                }

                nameField.setText(username);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public ContactCard findUser(String userID) {
        return contactRepository.findByUserID(userID);
    }

    public ArrayList<ContactCard> filterByType(String currentChoice) {
        return contactRepository.filterByType(currentChoice);
    }

    public ArrayList<ContactCard> filterByName(String name, boolean resetList, String accountType) {
        return contactRepository.filterByUserName(name, resetList, accountType);
    }

    public void sendMessage(String userID, String userInput, Uri fileUpload) {

        String myUsername = UtilityFunctions.getUserNameFromFirebase(); // gets our username to add to message
        String[] users = {userID, myUsername}; // array of the users will always be 2 for non group chats

        long time = Calendar.getInstance().getTimeInMillis(); // gets the current time
        String filePath = null;

        if (fileUpload != null) { // if we have a file, upload it
            ImageController ic = new ImageController();
            ic.ImageUpload(context, fileUpload, "chat");
        }

        Message message = new Message(myUsername, userInput, time, filePath); // create a message for the chat

        for (int i = 0; i < users.length; i++) { // loop the write to each of the users database
            int currentSender = (i + 1) % users.length;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + users[i] + "/messages/" + users[currentSender]);

            if (myUsername.equalsIgnoreCase(users[i])){
                reference.child("message_info").child("read_status").setValue(String.valueOf(UtilityFunctions.READ));
            } else {
                reference.child("message_info").child("read_status").setValue(String.valueOf(UtilityFunctions.UNREAD));
                Notification notification = new Notification("chat", myUsername + " sent you a message!", userInput,  users[i]);
                FirebaseNotificationManager.sendNotificationToUser(notification);
            }
            reference.child("message_info").child("time_stamp").setValue(time);

            reference.child(String.valueOf(time)).setValue(message);
        }
    }

    /**
     * method for loading the message to the screen
     * @param userId
     * @param view
     * @param scrollView
     */
    public void loadChatMessages(String userId, final LinearLayout view, final ScrollView scrollView) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase()
                + "/messages/" + userId + "/");  // gets the message the user wants to view

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                handleMessageViews(dataSnapshot, view, scrollView); // when we add a child to this database
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
     * loads the group message the user wants to view
     * @param groupMessage
     * @param view
     * @param scrollView
     */
    public void loadGroupMessages(String groupMessage, final LinearLayout view, final ScrollView scrollView) {
        shouldListen = false;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("group_messages/" + groupMessage);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                handleMessageViews(dataSnapshot, view, scrollView);
            }


            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
     * adds the each of the messages dialog to the screen
     * @param dataSnapshot
     * @param view
     * @param scrollView
     */
    private void handleMessageViews(final DataSnapshot dataSnapshot, final LinearLayout view, final ScrollView scrollView)
    {
        if (dataSnapshot.getKey().equalsIgnoreCase("message_info"))
            return;

        Message currentMessage = dataSnapshot.getValue(Message.class);

        View v = LayoutInflater.from(view.getContext()).inflate(R.layout.message_dialog_box, null);
        RelativeLayout dialog_box = v.findViewById(R.id.chat_message_dialog_box); // gets the dialog layout

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, scrollView.getBottom());
            }
        }); // force chat to the bottom

        if (currentMessage.getSender().equalsIgnoreCase(UtilityFunctions.getUserNameFromFirebase())) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dialog_box.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            dialog_box.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#91bbff"))); // if you are the sender, style the dialog this way
        }

        TextView userName = v.findViewById(R.id.chat_message_user_name); // add the user name to message
        if (currentMessage.getSender().equalsIgnoreCase(UtilityFunctions.getUserNameFromFirebase())) {
            userName.setText("You said :");
        } else {
            getUsername(currentMessage.getSender(), userName);
            userName.setText(userName.getText() + " said :");
        }


        TextView user_message = v.findViewById(R.id.chat_message_user_message);
        user_message.setText(currentMessage.getMessage());

        TextView time_display = v.findViewById(R.id.chat_message_date);
        time_display.setText(UtilityFunctions.milliToTime(currentMessage.getSendTime()));

        if (currentMessage.getImageLink() != null) { //if we have a image, make visible
            ImageView imageView = v.findViewById(R.id.chat_message_image);
            new ImageController().setImageInView(imageView, "chat_images/" + currentMessage.getImageLink());
            imageView.setVisibility(View.VISIBLE);
        }

        view.addView(v);
    }

    /**
     * method for sending group message
     * @param param
     * @param userInput
     * @param imageUpload
     */
    public void sendGroupMessage(String param, String userInput, Uri imageUpload)
    {
        long time = Calendar.getInstance().getTimeInMillis();
        String filePath = null;

        if (imageUpload != null) {
            ImageController ic = new ImageController();
            ic.ImageUpload(context, imageUpload, "chat");

            while (filePath == null)
            {
                filePath = ic.getFileId();
            }
        }

        Message message = new Message(UtilityFunctions.getUserNameFromFirebase(), userInput, time, filePath);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("group_messages/" + param);
        reference.child("message_info").child("time_stamp").setValue(time);
        reference.child(String.valueOf(time)).setValue(message);
    }
}