package com.itbstudentapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class UserManager
{

    private Context context;

    private String username;
    private String password;
    private String emailAddress;

    public UserManager(Context context)
    {
        this.context = context;
    }

    // checks if we have an email that is permitted
    public boolean isTheUserEmailValid(String email)
    {
        String[] emailAddress = context.getResources().getStringArray(R.array.permitted_emails);

        // loops through the permitted prefixes and returns true when it finds a match
        for(int i = 0; i < emailAddress.length; i++)
        {
            if(emailAddress[i].equalsIgnoreCase(email))
                return true;
        }

        return false;
    }

    // checks the fields for blank
    public boolean areFieldsBlank(String ... texts)
    {
        for(int i = 0; i < texts.length; i++)
        {
            if(texts[i].length() <= 0)
                return true;
        }

        return false;
    }

    // checks the password for correct format
    private boolean validPasswordFormat(String password)
    {
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        boolean numberOfCharacters = (password.length() >= 8);

        for(int i = 0; i < password.length(); i++)
        {
            if(!numberOfCharacters)
                return numberOfCharacters;

            if(Character.isUpperCase(password.charAt(i)))
                hasUppercase = true;

            if(Character.isLowerCase(password.charAt(i)))
                hasLowercase = true;

            if(Character.isDigit(password.charAt(i)))
                hasNumber = true;

           if(Character.isSpaceChar(password.charAt(i)))
               return false;
        }

        return hasUppercase && hasLowercase && hasNumber;
    }

    // registers the user
    public void registerUser(final String username, String emailAddress, String password)
    {
        emailAddress = emailAddress.trim(); // takes any white space away

        if(areFieldsBlank(username, emailAddress, password))
        {
            Toast.makeText(context, "Ensure that the fields are not blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if(emailAddress.split("@").length != 2) // makes sure we have a email address
        {
            Toast.makeText(context, "Your email is not a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isTheUserEmailValid(emailAddress.split("@")[1]))
        {
            Toast.makeText(context, "Your email is not an ITB email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!validPasswordFormat(password))
        {
            Toast.makeText(context, "Invalid password format", Toast.LENGTH_SHORT).show();
            return;
        }

        // checks for conflict
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(username.split("@")[0]).exists())
                {
                    Toast.makeText(context, "User is already signed up.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        this.username = username;
        this.emailAddress = emailAddress.toLowerCase();
        this.password = password;

        if(this.emailAddress.contains("student"))
            askUserForCourse(); // if its a user we get the course
        else{
            // else write as a staff member
           writeToDatabase(getUserAccountType(emailAddress), null);
            String user_id = emailAddress.split("@")[0];
            user_id = user_id.replace(".", "_");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + user_id);
            ref.child("groups").setValue("staff:");
            ref.child("staffUser").setValue(true);
            ((Activity)context).startActivity(new Intent(context, LoginScreen.class));
        }
    }

    // get the users group
    private void getuserGroup(final String course)
    {
        final Dialog groupDialog = new Dialog(context);
        groupDialog.setContentView(R.layout.modal_course_choice);
        final LinearLayout modalLayout = groupDialog.findViewById(R.id.course_list);

        if(!UtilityFunctions.doesUserHaveConnection(context))
        {
            Toast.makeText(context, "No network connection. Please try again later with network connection.", Toast.LENGTH_SHORT).show();
            ((Activity)context).startActivity(new Intent(context, LoginScreen.class));
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("group_messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                for(final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    final View groupItem = LayoutInflater.from(context).inflate(R.layout.course_item_list, null);
                    final TextView textView = groupItem.findViewById(R.id.course_title);
                    textView.setText(snapshot.getKey());

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String group = snapshot.getKey() + ":";
                            addUserGroupToAccount(group,course);
                            groupDialog.dismiss();
                        }
                    });

                    modalLayout.addView(groupItem);
                }

                groupDialog.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // add the user to the group
    private void addUserGroupToAccount(String group, String course_id)
    {
        writeToDatabase(getUserAccountType(emailAddress), course_id);
        String user_id = emailAddress.split("@")[0];
        user_id = user_id.replace(" ", "_");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + user_id);
        ref.child("groups").setValue(group);
        ref.child("staffUser").setValue(false);
    }

    // ask the user from a list of courses which is theirs
    private void askUserForCourse()
    {
        final Dialog courseChoice = new Dialog(context);

        courseChoice.setContentView(R.layout.modal_course_choice);
        LinearLayout modalLayout = courseChoice.findViewById(R.id.course_list);

        final String[] courses = context.getResources().getStringArray(R.array.courses);

        for(int i = 0; i < courses.length; i++)
        {
            View courseItem = LayoutInflater.from(context).inflate(R.layout.course_item_list,null);
            final TextView textView = courseItem.findViewById(R.id.course_title);
            textView.setText(courses[i]);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    courseChoice.dismiss();
                    getuserGroup(textView.getText().toString());

                }
            });

            modalLayout.addView(courseItem);
        }

        courseChoice.show();
    }

    private String getUserAccountType(String emailAddress)
    {
        String emailLink = emailAddress.split("@")[1];

        if(emailLink.contains("student"))
            return "student";

        if(emailLink.contains("admin"))
            return "admin";

        return "itb-staff";
    }

    private void writeToDatabase(String accountType, String courseId)
    {
        if(!UtilityFunctions.doesUserHaveConnection(context))
        {
            Toast.makeText(context, "Please wait for network connection.", Toast.LENGTH_SHORT).show();
            return; // failed due to no network
        }

        User user = new User(username, courseId, accountType, emailAddress);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(prepareFirebaseLink(emailAddress.split("@")[0])).setValue(user);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(emailAddress, password);

        if(accountType.equalsIgnoreCase("itb-staff"))
            databaseReference.child("users").child(prepareFirebaseLink(emailAddress.split("@")[0])).child("groups").setValue("staff");

        Toast.makeText(context, "Account created successfully.", Toast.LENGTH_SHORT).show();
        context.startActivity(new Intent(context, LoginScreen.class));
        ((Activity)context).finish();
    }

    public String prepareFirebaseLink(String id)
    {
       return id.replace('.', '_');
    }
}
