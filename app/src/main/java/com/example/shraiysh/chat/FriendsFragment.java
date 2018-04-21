package com.example.shraiysh.chat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private View mMainView;
    FirebaseRecyclerAdapter friendsRecyclerViewAdapter;
    private String mCurrent_user_id;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends").child(mCurrent_user_id);

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query, Friends.class)
                .build();
        friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsFragment.FriendsViewHolder friendsViewHolder, int i, @NonNull final Friends friends) {
//                friendsViewHolder.setName(friends.getName());
//                friendsViewHolder.setStatus(friends.getStatus());
//                friendsViewHolder.setDisplayImage(friends.getThumb_image());
                String list_user_id = getRef(i).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        String online = dataSnapshot.child("online").getValue().toString();
                        if(online.equals("true"))
                            friendsViewHolder.setOnlineVisibility(true);
                        else
                            friendsViewHolder.setOnlineVisibility(false);



                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setStatus(status);
                        friendsViewHolder.setDisplayImage(thumb_image);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                final String uid = getRef(i).getKey();

                friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                        profileIntent.putExtra("user_id", uid);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
                return new FriendsViewHolder(view);
            }
        };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        friendsRecyclerViewAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        friendsRecyclerViewAdapter.stopListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView friendStatusView = mView.findViewById(R.id.user_single_status);
            friendStatusView.setText(status);
        }

        public void setDisplayImage(String thumb_image) {
            CircleImageView mCircleImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(mCircleImageView);
        }

        public void setOnlineVisibility(boolean online) {
            ImageView mOnlineIcon = mView.findViewById(R.id.online_icon);

            if(online) {
                mOnlineIcon.setVisibility(View.VISIBLE);
            }
            else
                mOnlineIcon.setVisibility(View.INVISIBLE);

        }

    }

}



