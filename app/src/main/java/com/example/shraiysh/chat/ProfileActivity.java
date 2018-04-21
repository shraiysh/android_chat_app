package com.example.shraiysh.chat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineButton;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificatoionDatabase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificatoionDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_username);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.profile_totalfriends);
        mProfileSendReqBtn = (Button)findViewById(R.id.profile_send_request_btn);
        mDeclineButton = (Button)findViewById(R.id.profile_decline_friend_request_btn);
        mDeclineButton.setVisibility(View.INVISIBLE);
        mDeclineButton.setEnabled(false);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loding User Data");
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mCurrent_state = "not_friends";


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                if(!image.equals("default_location_for_image")) Picasso.get().load(image).placeholder(R.drawable.avatar).into(mProfileImage);

                //---------FRIENDS LIST / REQUEST FEATURE-------------

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("recieved")) {
                                mCurrent_state = "req_recieved";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mDeclineButton.setVisibility(View.VISIBLE);
                                mDeclineButton.setEnabled(true);
                            }
                            else if(req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                            }
                        }
                        else {
                            mFriendDatabase .child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend This Person");

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        mProgress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                //--------------------NOT FRIENDS STATE---------------

                if(mCurrent_state.equals("not_friends")) {

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {



                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").
                                        setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUser.getUid());
                                        notificationData.put("type","request");

                                        mNotificatoionDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mProfileSendReqBtn.setEnabled(true);
                                                mCurrent_state = "req_sent";
                                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                                Toast.makeText(ProfileActivity.this, "Request sent.", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                        mProfileSendReqBtn.setEnabled(true);
                                        mCurrent_state = "req_sent";
                                        mProfileSendReqBtn.setText("Cancel Friend Request");

                                        Toast.makeText(ProfileActivity.this, "Request sent.", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }

                //---------------------REQUEST SENT STATE----------------

                if(mCurrent_state == "req_sent") {
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }

                //---------------------REQUEST RECIEVED STATE----------------
                if(mCurrent_state == "req_recieved") {

                    final String current_date = DateFormat.getDateInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(current_date)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(current_date)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                    mCurrent_state = "friends";
                                                                    mProfileSendReqBtn.setText("Unfriend this Person");
                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                            });
                                }
                            });
                }

                //--------------------ALREADY FRIENDS STATE-------------------

                if(mCurrent_state == "friends") {

                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                }
                            });

                        }
                    });
                }

            }
        });

    }
}
