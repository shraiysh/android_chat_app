package com.example.shraiysh.chat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolBar;

    private ViewPager mPager;
    private SectionsPagerAdapter mAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        mPager = (ViewPager)findViewById(R.id.pager1);
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mPager);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Chatting");



    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuth.getCurrentUser()!=null) {
            mUserRef.child("online").setValue(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null) {
            sendToStart();
        }
        else {
            mUserRef.child("online").setValue(true);
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.main_settings_btn) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if(item.getItemId() == R.id.main_all_btn) {
            startActivity(new Intent(MainActivity.this, UsersActivity.class));
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i=new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
    }
}
