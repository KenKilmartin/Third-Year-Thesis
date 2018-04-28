package com.itbstudentapp.ChatSystem;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itbstudentapp.User;

import java.util.ArrayList;

public class ContactRepository
{
    private ArrayList<ContactCard> userInformation;
    private ArrayList<ContactCard> temp;

    public ContactRepository()
    {
        userInformation = new ArrayList<>(); // how we store the list of the users
        getUserInformation(); // gets the users in the list
    }

    private void getUserInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren())
                {
                    String user_id = d.getKey();
                    String username = d.child("username").getValue(String.class);
                    String userImage = d.child("imageLink").getValue(String.class);
                    String email = d.child("email").getValue(String.class);

                    UserType ccType = getUserTypeFromEmail(email);

                    ContactCard cc = new ContactCard(user_id, username, userImage, ccType);
                    userInformation.add(cc);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private UserType getUserTypeFromEmail(String email) // sets the users account type
    {
        email = email.toLowerCase();

        if(email.contains("admin"))
            return UserType.ADMIN;
        if(email.contains("student"))
            return UserType.STUDENT;
        if(email.contains("itb.ie"))
            return UserType.LECTURER;
        if(email.contains("nln"))
            return UserType.NLN;

        return UserType.ADMIN;
    }

    public ArrayList<ContactCard> getContactInformation()
    {
        return this.userInformation;
    }

    public ContactCard findByUserID(String userID)
    {
        for(ContactCard card : userInformation)
        {
            if(card.getUser_id().equalsIgnoreCase(userID))
                return card;
        }

        return null;
    }


    public ArrayList<ContactCard> filterByType(String currentChoice)
    {
        ArrayList<ContactCard> cards = new ArrayList<>();

        for(ContactCard card : userInformation)
        {
            if(card.getUserAccountType().toString().equalsIgnoreCase(currentChoice))
            {
                cards.add(card);
            }
        }

        temp = cards;
        return cards;
    }

    public ArrayList<ContactCard> filterByUserName(String name, boolean reset, String accountType)
    {
        ArrayList<ContactCard> users = new ArrayList<>();
        if(reset)
        {
            temp = userInformation;
            filterByType(accountType);
        }

        if(temp == null)
        {
            temp = userInformation;
        }

        for(ContactCard card : temp)
        {
            boolean shouldAdd = true;

            for(int i = 0; i < name.length(); i++)
            {
                String testName = card.getUser_name().toLowerCase();
                name = name.toLowerCase();

                if(name.charAt(i) != testName.charAt(i))
                {
                    shouldAdd = false;
                    break;
                }
            }

            if(shouldAdd)
            {
                users.add(card);
            }
        }

        temp = users;

        return users;
    }
}
