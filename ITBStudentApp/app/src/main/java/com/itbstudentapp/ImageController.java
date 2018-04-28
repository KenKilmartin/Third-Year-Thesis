package com.itbstudentapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itbstudentapp.Interfaces.OnImageUploaded;

import java.net.URI;
import java.util.UUID;

public class ImageController extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private Activity caller;
    private static final int request_code = 1;
    private Uri uploadFile = null;
    private String fileId = null;

    private OnImageUploaded imageUploaded;

    public ImageController()
    {}

    /**
     *  image controller must have the activity that calls it as we need to call method in that when the
     *  image is uploaded. we get the activity from context
     */

    public ImageController(Context context)
    {
        this.context = context;
    }

    public ImageController(Context context, OnImageUploaded imageUploaded)
    {
        this.context = context;
        this.imageUploaded = imageUploaded;
        this.caller = (Activity) context;
    }

    public ImageController(Activity activity)
    {
        this.context = activity.getBaseContext();
        this.caller = activity;
    }

    public void setUploadUri(Uri file)
    {
        this.uploadFile = file;
    }

    @Override
    public void onClick(View v) {
        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT); // get the file system from device
        gallery.setType("image/*"); // look only for images
        caller.startActivityForResult(Intent.createChooser(gallery, "Pick a file to upload"), request_code); // request code is the code when success
    }

    public void ImageUpload(final Context context, Uri filePath, String directory) {
        if (filePath != null) { // make sure we have a image
            // tell the user we are uploading
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String file = UUID.randomUUID().toString(); // random name for image

            // add to firebase storage
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(directory + "/" + file);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "File uploaded", Toast.LENGTH_SHORT).show();
                    fileId = file;

                    if(imageUploaded != null)
                        imageUploaded.onImageUploaded(file); // give the link to the image as needed for the post

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "File uploaded", Toast.LENGTH_SHORT).show();
                    fileId = "";
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage((int) progress + "% complete.");
                }
            });
        }
    }

    public Uri getUploadedUri() {

        return this.uploadFile;
    }

    public String getFileId()
    {
        return this.fileId;
    }

    public void setImageInView(final ImageView imageView, final String imageLink)
    {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(imageLink);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(imageView.getContext()).load(uri.toString()).into(imageView);
            }
        });
    }
}
