package com.itbstudentapp.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.itbstudentapp.R;
import com.itbstudentapp.UtilityFunctions;

/**
 * used for the user to reset there password
 */
public class ForgottenPasswordModal extends Dialog
{
    public ForgottenPasswordModal(@NonNull Context context) {
        super(context, android.R.style.Theme_Light_NoTitleBar);
        setContentView(R.layout.forgotten_password_modal);

        addButtonListeners();
        show();
    }

    private void addButtonListeners()
    {
        TextView submit = findViewById(R.id.submit_reset);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });

        TextView cancel = findViewById(R.id.dismiss_reset);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void onSubmit()
    {
        EditText emailAddress = findViewById(R.id.reset_email);
        if(emailAddress.getText().toString() == null || emailAddress.getText().toString().length() <= 0)
        {
            Toast.makeText(getContext(), "The email address can not be blank", Toast.LENGTH_SHORT).show();
            return; // ensure we have a email
        }

        if(!UtilityFunctions.doesUserHaveConnection(getContext()))
        {
            Toast.makeText(getContext(), "No network connection available. Please retry later.", Toast.LENGTH_SHORT).show();
            return; // network sensitive
        }

        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Sending email");
        dialog.setTitle("Password Reset");
        dialog.show(); // show to the user its sending reset info

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(emailAddress.getText().toString().toLowerCase()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) // if sent, show success
                {
                    Toast.makeText(getContext(), "Email with your reset details sent", Toast.LENGTH_SHORT).show();
                    dismiss();
                    dialog.dismiss();
                } else{ // else tell the user it failed
                    Toast.makeText(getContext(), "Password reset failed", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        });
    }
}
