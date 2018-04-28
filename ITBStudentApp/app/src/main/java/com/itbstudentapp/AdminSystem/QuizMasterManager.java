package com.itbstudentapp.AdminSystem;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.itbstudentapp.ChatSystem.ContactRepository;
import com.itbstudentapp.MainActivity;
import com.itbstudentapp.R;

import java.util.ArrayList;

public class QuizMasterManager extends Dialog {

    private ContactRepository contactRepository;
    private boolean isPickingUser = false;

    public QuizMasterManager(@NonNull Context context)
    {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);

        contactRepository = new ContactRepository(); // gets the list of users

        setContentView(R.layout.admin_setting_panel);

        if(!isPickingUser)
            generateQuizMasterList();

        setupHomeButton();
        show();
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

    private void generateQuizMasterList()
    {
        // this gets the lsit of our current quiz masters
        final LinearLayout list_section = findViewById(R.id.option_section);
        TextView title = findViewById(R.id.title);
        title.setText("Quiz Master Settings");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("quiz");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /**
                 *  for each quiz we have in the database, this gets the quiz master that is assocated with the quiz and displays them
                 */
                for(final DataSnapshot d : dataSnapshot.getChildren())
                {
                    View item = LayoutInflater.from(list_section.getContext()).inflate(R.layout.quiz_master_item, null);

                    TextView nameOfSubject = item.findViewById(R.id.quiz_title);
                    String nameOfQuiz = d.getKey();
                    nameOfQuiz = nameOfQuiz.substring(0,1).toUpperCase() + nameOfQuiz.substring(1, nameOfQuiz.length()).toLowerCase();
                    nameOfSubject.setText(nameOfQuiz);

                    final TextView quiz_master_text = item.findViewById(R.id.quiz_master_name);
                    final String masterId = d.child("quiz_master").getValue(String.class);

                    /**
                     *  if we have a master for the quiz we get the information of that user from the user database
                     */
                    if(masterId != null)
                    {
                        DatabaseReference user_db = FirebaseDatabase.getInstance().getReference("users/" + masterId);
                        user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.child("username").getValue(String.class);

                                String[] usernameSplit = username.split(" ");
                                username = "";
                                for(int i = 0; i < usernameSplit.length; i++)
                                {
                                    String name = usernameSplit[i].substring(0,1).toUpperCase() + usernameSplit[i].substring(1, usernameSplit[i].length());
                                    username += name + " ";
                                }

                                quiz_master_text.setText(username);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        quiz_master_text.setText("No master");
                    }

                    // sets up the quiz master edit button for changing master
                    ImageView edit = item.findViewById(R.id.master_edit);
                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNewQuizMaster(d.getKey(), masterId);
                        }
                    });

                    list_section.addView(item);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setNewQuizMaster(final String quiz_key, String currentMasterID)
    {
        final LinearLayout list_section = findViewById(R.id.option_section);
        list_section.removeAllViews();
        TextView title = findViewById(R.id.title);
        title.setText("Pick Quiz master");
        isPickingUser = true;

        /**
         *  if we are choosing a new quiz master, we only want them to be lecturers so we filter the choice
         */
        final ArrayList<ContactCard> contactCards = contactRepository.filterByType("LECTURER");
        for(int i = 0; i < contactCards.size(); i++)
        {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.user_choice_item, null);
            TextView usernameField = view.findViewById(R.id.user_name);

            String user_name = contactCards.get(i).getUser_name();

            if( contactCards.get(i).getUser_id().equalsIgnoreCase(currentMasterID))
                continue; // if this user is already the quiz master, we dont add to the list

            String usernameArray[] = user_name.split(" ");

            user_name = "";

            for(int p = 0; p < usernameArray.length; p++)
            {
                user_name += usernameArray[p].substring(0,1).toUpperCase() + usernameArray[p].substring(1,usernameArray[p].length()).toLowerCase() + " ";
            }

            usernameField.setText(user_name);

            /**
             *  if the user has changed the quiz master, we save it to the database
             */
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("quiz/"  + quiz_key);
                    ref.child("quiz_master").setValue(contactCards.get(finalI).getUser_id());
                    Toast.makeText(getContext(), "Quiz master changed.", Toast.LENGTH_SHORT).show();

                    list_section.removeAllViews();
                    generateQuizMasterList();
                    isPickingUser = false;
                }
            });

            list_section.addView(view);
        }
    }

    @Override
    public void onBackPressed() {

        if (isPickingUser)
        {
            LinearLayout list_section = findViewById(R.id.option_section);
            list_section.removeAllViews();
            generateQuizMasterList();
            isPickingUser = false;
            return;
        }

        super.onBackPressed();
    }
}
