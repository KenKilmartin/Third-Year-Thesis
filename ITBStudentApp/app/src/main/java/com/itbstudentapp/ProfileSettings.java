package com.itbstudentapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itbstudentapp.utils.ForgottenPasswordModal;
import com.itbstudentapp.utils.UserSettings;

import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.microedition.khronos.opengles.GL;

public class ProfileSettings extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private TextView changeProfilePicture, changePassword, backToHome;
    private Switch led, vibrate, sound, geo;
    private static final int request_code = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        setSupportActionBar(UtilityFunctions.getApplicationToolbar(this));

        // change image button
        changeProfilePicture = findViewById(R.id.profile_change_image);
        changeProfilePicture.setOnClickListener(this);

        // settings switchs
        led = findViewById(R.id.switch_led);
        vibrate = findViewById(R.id.switch_vibrate);
        sound = findViewById(R.id.switch_sound);
        geo = findViewById(R.id.switch_location);

        led.setOnCheckedChangeListener(this);
        vibrate.setOnCheckedChangeListener(this);
        sound.setOnCheckedChangeListener(this);
        geo.setOnCheckedChangeListener(this);

        getCurrentPrefs();

        // get change password button
        changePassword = findViewById(R.id.profile_reset_password);
        changePassword.setOnClickListener(this);

        backToHome = findViewById(R.id.profile_back_home);
        backToHome.setOnClickListener(this);

        loadProfilePicture();
    }

    private void loadProfilePicture()
    {
        // gets the current image from firebase
        final Context context = this;
        final ImageView profile = findViewById(R.id.profile_image);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("imageLink")!= null)
                {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference("userImages/" + dataSnapshot.child("imageLink").getValue(String.class));

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).into(profile);
                        }
                    });
                }
            }

            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == changeProfilePicture.getId())
        {
            changeProfilePicture();
        }
        else if(v.getId() == changePassword.getId())
        {
            changeUserPassword();
        }
        else if(v.getId() == backToHome.getId())
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void changeUserPassword()
    {
        new ForgottenPasswordModal(this);
    }

    // uploading a new profile image
    private void changeProfilePicture()
    {
        if(!UtilityFunctions.doesUserHaveConnection(this))
        {
            Toast.makeText(this, "No network connection. Please wait to upload image.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Pick a file to upload"), request_code);
    }

    @Override
    // the result of our new upload
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if(requestCode == request_code)
        {
            if(resultCode == RESULT_OK)
            {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + UtilityFunctions.getUserNameFromFirebase());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userImageLink;

                        if(dataSnapshot.child("imageLink").exists() && dataSnapshot.child("imageLink").getValue(String.class) != null)
                        {
                            // grab the old link to overwrite
                            userImageLink = dataSnapshot.child("imageLink").getValue(String.class);
                        } else{
                            // user has no image. easy upload
                            userImageLink = UUID.randomUUID().toString();
                            reference.child("imageLink").setValue(userImageLink);
                            Log.e("Key", reference.toString());
                        }

                        handleImageUpload(userImageLink, progressDialog, data.getData());
                    }

                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
    }

    private void handleImageUpload(final String userImageLink, final ProgressDialog progressDialog, final Uri data)
    {
        final Context context = this;
        final ImageView userImage = findViewById(R.id.profile_image);

        StorageReference storage = FirebaseStorage.getInstance().getReference("userImages/");
        storage.child(userImageLink).putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(progressDialog.getContext(), "File uploaded", Toast.LENGTH_SHORT).show();
                Glide.with(getApplicationContext()).load(data).into(userImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(progressDialog.getContext(), "Upload failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage((int) progress + "% complete.");
            }
        });
    }

    private void getCurrentPrefs()
    {
        SharedPreferences preferences = getSharedPreferences(UtilityFunctions.PREF_FILE, MODE_PRIVATE);

        vibrate.setChecked(preferences.getBoolean("vibrate", true));
        sound.setChecked(preferences.getBoolean("sound", true));
        led.setChecked(preferences.getBoolean("led", true));
        geo.setChecked(preferences.getBoolean("geo", true));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(buttonView.getId() == vibrate.getId())
            handleChange("vibrate", isChecked);
        else if(buttonView.getId() == sound.getId())
            handleChange("sound", isChecked);
        else if(buttonView.getId() == led.getId())
            handleChange("led", isChecked);
        else if(buttonView.getId() == geo.getId())
            handleChange("geo", isChecked);

    }

    private void handleChange(String option, boolean isChecked)
    {
        SharedPreferences.Editor editor = getSharedPreferences(UtilityFunctions.PREF_FILE, MODE_PRIVATE).edit();

        if(option.equalsIgnoreCase("vibrate"))
        {
            UserSettings.vibrate = isChecked;
            editor.putBoolean("vibrate", UserSettings.vibrate);
        }

        if(option.equalsIgnoreCase("sound"))
        {
            UserSettings.play_sounds = isChecked;
            editor.putBoolean("sound", UserSettings.play_sounds);
        }

        if(option.equalsIgnoreCase("geo"))
        {
            UserSettings.location = isChecked;
            editor.putBoolean("geo", UserSettings.location);
        }

        if(option.equalsIgnoreCase("led"))
        {
            UserSettings.flash = isChecked;
            editor.putBoolean("led", UserSettings.flash);
        }

        editor.apply();
    }
}
