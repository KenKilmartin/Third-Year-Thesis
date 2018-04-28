package com.itbstudentapp.AdminSystem;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itbstudentapp.ForumManager;
import com.itbstudentapp.ForumPost;
import com.itbstudentapp.R;
import com.itbstudentapp.Reply;
import com.itbstudentapp.UtilityFunctions;

import java.util.Calendar;

public class ReportedPostModal extends Dialog implements View.OnClickListener {

    private String dbRef;
    private Context ct;
    private ForumPost fp;
    private boolean hasDeleted = false;

    /**
     *  modal that shows the slimmed down reported post
     * @param context
     * @param forumPost
     * @param ref
     * @param dRef
     */
    public ReportedPostModal(@NonNull Context context, ForumPost forumPost, String ref, final DatabaseReference dRef)
    {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        setContentView(R.layout.reported_post_model);

        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        dbRef = ref;
        ct = context;
        fp = forumPost;

        setPostsInView();
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                if(hasDeleted) // if the user decides to delete the post, on dismiss we delete post
                {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReferenceFromUrl(dRef.toString());
                    reference.removeValue();
                }
            }
        });

        show();
    }

    public void setPostsInView() {
        LinearLayout layout = findViewById(R.id.forum_post_section);
        loadPostIntoView(layout);
    }

    /**
     *  adds the post to the view
     * @param layout
     */
    private void loadPostIntoView(LinearLayout layout)
    {
        View view = LayoutInflater.from(ct).inflate(R.layout.report_post_item, null);

        TextView user_id = view.findViewById(R.id.reported_post_user);
        TextView date = view.findViewById(R.id.reported_post_date);
        TextView post = view.findViewById(R.id.reported_post_text);
        final ImageView image = view.findViewById(R.id.reported_post_image);
        ImageView button = view.findViewById(R.id.reported_post_remove);

        user_id.setText(fp.getPosterID());
        date.setText(UtilityFunctions.milliToDate(fp.getPostTime()));
        post.setText(fp.getPostComment());

        if(fp.getFileUpload() != null)
        {
            image.setVisibility(View.VISIBLE);
            image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));

            StorageReference reference = FirebaseStorage.getInstance().getReference("forumImages/" + fp.getFileUpload());

            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ct).load(uri).into(image);
                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReferenceFromUrl(dbRef);
                reference.removeValue();
                hasDeleted = true;
                getContext().startActivity(new Intent(getContext(), ReportedPost.class));
                dismiss();
            }
        });

        layout.addView(view, 0);

        for(int i = 0; i < fp.getPostReplies().size(); i++)
        {
            addRepliesToView(i, fp.getPostReplies().get(i), layout);
        }



    }

    /**
     * adds each of the post replys to the dialog
     * @param i
     * @param reply
     * @param layout
     */
    private void addRepliesToView(final int i, Reply reply, final LinearLayout layout)
    {
        final View view = LayoutInflater.from(ct).inflate(R.layout.report_post_item, null);

        TextView user_id = view.findViewById(R.id.reported_post_user);
        TextView date = view.findViewById(R.id.reported_post_date);
        TextView post = view.findViewById(R.id.reported_post_text);
        final ImageView image = view.findViewById(R.id.reported_post_image);
        ImageView button = view.findViewById(R.id.reported_post_remove);

        user_id.setText(reply.getPosterID());
        date.setText(UtilityFunctions.milliToDate(reply.getPostTime()));
        post.setText(reply.getPosterComment());

        if(reply.getImageLink() != null)
        {
            image.setVisibility(View.VISIBLE);
            image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));

            StorageReference reference = FirebaseStorage.getInstance().getReference("forumImages/" + reply.getImageLink());

            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ct).load(uri).into(image);
                }
            });
        }

        // if the user has deleted the post
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReferenceFromUrl(dbRef + "/replies/");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int counter = 0;

                        for (DataSnapshot snap : dataSnapshot.getChildren())
                        {
                            if(counter == i)
                            {
                                snap.getRef().removeValue();
                                hasDeleted = true;
                                Toast.makeText(getContext(), "Reply deleted.", Toast.LENGTH_SHORT).show();
                                layout.removeView(view);
                            }

                            counter++;
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        layout.addView(view, i + 1);
    }

    @Override
    public void onClick(View v) {

    }


}
