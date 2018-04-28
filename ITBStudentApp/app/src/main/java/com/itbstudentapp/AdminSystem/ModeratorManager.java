package com.itbstudentapp.AdminSystem;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbstudentapp.ChatSystem.ContactCard;
import com.itbstudentapp.ChatSystem.ContactList;
import com.itbstudentapp.ChatSystem.ContactRepository;
import com.itbstudentapp.MainActivity;
import com.itbstudentapp.R;

import java.util.ArrayList;

class ModeratorManager extends Dialog
{
    private ContactRepository contactRepository;
    ContactList contactList;
    private ContactCard contactCard;

    public ModeratorManager(Context adminPanel)
    {
        super(adminPanel, android.R.style.Theme_Light_NoTitleBar_Fullscreen); // sets the dialog up
        contactRepository = new ContactRepository(); // get the contact list from our user repo
        setContentView(R.layout.admin_setting_panel); // sets the layout
        TextView title = findViewById(R.id.title);
        title.setText("Moderator List");

        setPanelVisiblity(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT), View.VISIBLE);

        addModeratorListener();  // listens for the new modarator button to be selected
        getListOfModerators(); // initializes the list of current modarators
        setupHomeButton();
        show(); // shows the dialog
    }

    private void addModeratorListener()
    {
        View view = findViewById(R.id.mod_add);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setPanelVisiblity(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0), View.INVISIBLE);
                contactList = new ContactList(v.getContext(), contactRepository);

                contactList.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        contactCard = contactList.getChoosenCard();
                        if(contactCard != null)
                        {
                            // adds the user to the modarator list
                            addUserToModList(contactCard);
                        }

                        setPanelVisiblity(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT), View.VISIBLE);
                    }
                });

            }
        });
    }

    private void addUserToModList(ContactCard contactCard)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("moderators");
        reference.child(contactCard.getUser_id()).child("username").setValue(contactCard.getUser_name());
        addToList(contactCard.getUser_name(), contactCard.getUser_id());
    }

    private void setPanelVisiblity(LinearLayout.LayoutParams params,  int visibility)
    {
        LinearLayout modAdd = findViewById(R.id.mod_add_panel);
        modAdd.setVisibility(visibility);
        modAdd.setLayoutParams(params);
    }

    /**
     *  method that checks the modarator database for users
     */
    private void getListOfModerators()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("moderators");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snap : dataSnapshot.getChildren())
                {
                    addToList(snap.child("username").getValue(String.class), snap.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addToList(String username, String user_id)
    {
        final LinearLayout list_section = findViewById(R.id.option_section);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.user_choice_item, null);
        TextView usernameField = view.findViewById(R.id.user_name);

        String user_name = username;
        String usernameArray[] = user_name.split(" ");
        user_name = "";

        for(int p = 0; p < usernameArray.length; p++)
        {
            user_name += usernameArray[p].substring(0,1).toUpperCase() + usernameArray[p].substring(1,usernameArray[p].length()).toLowerCase() + " ";
        }

        handleModDelete(user_id, view);
        usernameField.setText(user_name);

        list_section.addView(view);
    }

    /**
     * removes modarators off the list to allow deletion of modarators
     * @param user_id
     * @param parent
     */
    private void handleModDelete(final String user_id, final View parent)
    {
        ImageView view = parent.findViewById(R.id.delete_mod);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("moderators/" + user_id);
                reference.setValue(null);

                ((ViewGroup)parent.getParent()).removeView(parent);

            }
        });
    }

    private void setupHomeButton()
    {
        TextView home = findViewById(R.id.report_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getContext().startActivity(new Intent(getContext(), MainActivity.class));
                ((Activity)getContext()).finish();

            }
        });
    }
}
