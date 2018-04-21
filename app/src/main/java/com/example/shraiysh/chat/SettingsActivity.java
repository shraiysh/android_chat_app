package com.example.shraiysh.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    static final int GALLERY_PICK = 1;

    private DatabaseReference mdbRef;
    private FirebaseUser mCurrentUser;

    private CircleImageView mCircleImageView;
    private TextView mName;
    private TextView mStatus;

    private Button mChangeName;
    private Button mChangeStatus;

    private StorageReference mImagesStorage;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mCircleImageView = (CircleImageView)findViewById(R.id.settings_circle_image);
        mName = (TextView)findViewById(R.id.settings_display_name);
        mStatus = (TextView)findViewById(R.id.settings_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();

        mdbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mdbRef.keepSynced(true);
        mChangeName = (Button)findViewById(R.id.settings_change_name);
        mChangeStatus = (Button)findViewById(R.id.settings_change_status);
        mImagesStorage = FirebaseStorage.getInstance().getReference();

        mdbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
//                Picasso.get().load(image).into(mCircleImageView);

                if(!image.equals("default_location_for_image")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mCircleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.avatar).into(mCircleImageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void goToChangeStatus(View view) {
        Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
        intent.putExtra("current_status",mStatus.getText().toString());
        startActivity(intent);
    }

    public void changeDp(View view) {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

//        CropImage.activity()
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .start(SettingsActivity.this);
    }

//    @Override
//    public void onBackPressed() {
//        startActivity(new Intent(getApplicationContext(),MainActivity.class));
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
                Uri imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .setMinCropWindowSize(500,500)
                        .start(this);

            }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    mProgress = new ProgressDialog(SettingsActivity.this);
                    mProgress.setTitle("Uploading Image...");
                    mProgress.setMessage("Please wait...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    Uri resultUri = result.getUri();

                    File thumb_path = new File(resultUri.getPath());

                    String current_user_id = mCurrentUser.getUid();

                    Bitmap thumb_bitmap;


                    thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] bitmap_data = baos.toByteArray();


                    final StorageReference thumb_filepath = mImagesStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");
                    StorageReference filepath = mImagesStorage.child("profile_images").child(current_user_id+".jpg");


                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {

                                final String download_url = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(bitmap_data);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                        String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();

                                        if(thumb_task.isSuccessful()) {

                                            Map update_hashmap = new HashMap<>();
                                            update_hashmap.put("image", download_url);
                                            update_hashmap.put("thumb_image", thumb_download_url);


                                            mdbRef.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        mProgress.dismiss();
                                                        Toast.makeText(SettingsActivity.this, "Success uploading", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            mProgress.dismiss();
                                            Toast.makeText(SettingsActivity.this, "Error uploading.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else {
                                mProgress.dismiss();
                                Toast.makeText(SettingsActivity.this, "Error uploading.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });


                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
        catch (IOException exp) {
            Toast.makeText(SettingsActivity.this, "Error uploading.", Toast.LENGTH_LONG).show();
        }


    }
//    public static String random() {
//        Random generator = new Random();
//        StringBuilder randomStringBuilder = new StringBuilder();
//        int randomLength = generator.nextInt(10);
//        char tempChar;
//        for (int i = 0; i < randomLength; i++){
//            tempChar = (char) (generator.nextInt(96) + 32);
//            randomStringBuilder.append(tempChar);
//        }
//        return randomStringBuilder.toString();
//    }
}

