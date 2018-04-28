package com.itbstudentapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.itbstudentapp.utils.ForgottenPasswordModal;

public class LoginScreen extends AppCompatActivity implements View.OnClickListener {

    private EditText user, password;
    private Button login, register;
    private TextView forgotten_password;

    private ProgressDialog progress;

    private FirebaseAuth auth; // firebase system for handling logging in
    private boolean isLoggingIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        user = (EditText) findViewById(R.id.user_name_field);
        password = (EditText) findViewById(R.id.password_field);

        login = (Button) findViewById(R.id.login_button);
        register = (Button) findViewById(R.id.register_button);
        forgotten_password = (TextView) findViewById(R.id.forgotten_password);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        forgotten_password.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkLogin() {
        final String user_name = user.getText().toString();
        final String user_password = password.getText().toString();

        // make sure we have a name to log in as
        if (user_name.length() == 0 || TextUtils.isEmpty(user_name)) {
            Toast.makeText(getApplicationContext(), "username must not be blank.", Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }

        // user entered a password
        if (password.length() == 0 || TextUtils.isEmpty(user_password)) {
            Toast.makeText(getApplicationContext(), "Password must not be blank.", Toast.LENGTH_SHORT).show();
            progress.dismiss();
            return;
        }

        loginUser(user_name, user_password);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.login_button) {
            progress = new ProgressDialog(this);
            progress.setTitle("Logging in");
            progress.setMessage("Please wait");
            progress.setCancelable(false);
            progress.show();

            checkLogin();
        } else if (v.getId() == R.id.register_button) {
            Intent intent = new Intent(this, RegisterUser.class);
            startActivity(intent);
            finish();
        } else {
           new ForgottenPasswordModal(this);
        }
    }

    private void loginUser(String username, final String user_password) {
        if (!UtilityFunctions.doesUserHaveConnection(this)) {
            Toast.makeText(this, "No internet connection. Please try again later", Toast.LENGTH_SHORT).show();
            return; // network sensitive
        }

        // create our services
        final Intent fBase = new Intent(this, MyFirebaseMessagingService.class);
        final Intent fbaseId = new Intent(this, MyFirebaseInstanceIDService.class);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + prepareLink(username));
        ref.addValueEventListener(new ValueEventListener() { // get the user name to check if they exist
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                    return;
                    // if no user exists
                }

                if(auth == null) // make sure we have an instance of firebase auth
                    auth = FirebaseAuth.getInstance();

                // sign in with the user name and password
                auth.signInWithEmailAndPassword(dataSnapshot.child("email").getValue(String.class), user_password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if(!isLoggingIn) { // make sure we dont try multiple log ins which causes a weird trigger
                                        isLoggingIn = true;
                                        Intent intent = new Intent(progress.getContext(), MainActivity.class);
                                        progress.dismiss(); // dismiss logging in dialog
                                        startService(fBase); // start our services
                                        startService(fbaseId);
                                        MyFirebaseInstanceIDService.saveTokenToDb(); // save device id to db
                                        startActivity(intent); // start main and destory this activity
                                        finish();
                                    }
                                } else {
                                    // log in failed
                                    Toast.makeText(getApplicationContext(), "Sign in failed.", Toast.LENGTH_SHORT).show();
                                    user.setText("");
                                    password.setText("");
                                    progress.dismiss();
                                }
                            }
                        });

                auth = null;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String prepareLink(String user_id) {

        // get right formatted id for firebase

        UserManager userManager = new UserManager(this);
        String entered_user_id = userManager.prepareFirebaseLink(user_id.toLowerCase());

        if (entered_user_id.contains("@"))
            return entered_user_id.split("@")[0];

        return entered_user_id;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
