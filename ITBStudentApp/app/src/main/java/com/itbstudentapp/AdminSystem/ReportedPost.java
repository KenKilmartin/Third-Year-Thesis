package com.itbstudentapp.AdminSystem;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.itbstudentapp.ForumManager;
import com.itbstudentapp.ForumPost;
import com.itbstudentapp.MainActivity;
import com.itbstudentapp.R;
import com.itbstudentapp.Reply;
import com.itbstudentapp.UtilityFunctions;

public class ReportedPost extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported_post);

        /**
         *  this makes sure the user has network connection as it depends on networking
         */
        if(!UtilityFunctions.doesUserHaveConnection(this))
        {
            Toast.makeText(this, "No network connection available. Please try again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setHomeButton();
        getReportedPosts();
    }

    private void setHomeButton()
    {
        final View view = findViewById(R.id.report_home);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(view.getContext(), MainActivity.class));
                finish();
            }
        });
    }

    /**
     * finds the list of reported posts
     */
    private void getReportedPosts()
    {
        final LinearLayout reportedPostsSection = findViewById(R.id.reported_posts_list);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("reported_posts");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /**
                 * if we have no reported posts, we just add a view to tell that to the user
                 */
                if(dataSnapshot.getChildrenCount() <= 0)
                {
                    View layout = getLayoutInflater().inflate(R.layout.reported_post_item, null);
                    TextView title = layout.findViewById(R.id.post_title);
                    title.setText("No posts reported");

                    ImageView cancelButton = layout.findViewById(R.id.post_report_delete);
                    cancelButton.setVisibility(View.INVISIBLE);

                    return; // we dont want to do anything else so we bail out
                }

                // otherwise, we get the list of reported posts and add each of them to the view
                for(final DataSnapshot d : dataSnapshot.getChildren())
                {
                    final LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.reported_post_item, null);
                    TextView title = layout.findViewById(R.id.post_title); // set the topic name as the report title
                    title.setText(d.getKey());

                    ImageView cancelButton = layout.findViewById(R.id.post_report_delete); // if we deem not a probelm, cancel the report
                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.getRef().setValue(null);
                            ((ViewGroup)layout.getParent()).removeView(layout);
                        }
                    });

                    // if we want to see the topic, this gives the modarator a slimmed down version
                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            final String dataLink = d.getValue(String.class);

                            DatabaseReference post_ref = FirebaseDatabase.getInstance().getReferenceFromUrl(dataLink);
                            post_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final ForumPost forumPost = dataSnapshot.getValue(ForumPost.class);

                                    for (DataSnapshot d : dataSnapshot.child("replies").getChildren()) {
                                        forumPost.addReplyToList(d.getValue(Reply.class));
                                    }

                                    new ReportedPostModal(reportedPostsSection.getContext(), forumPost, dataLink, d.getRef());

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    reportedPostsSection.addView(layout);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
