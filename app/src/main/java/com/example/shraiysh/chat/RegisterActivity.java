package com.example.shraiysh.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextView mDisplayName;
    private TextView mEmail;
    private TextView mPassword;
    private Button mCreateBtn;

    private Toolbar mToolbar;

    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mdbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mDisplayName = (TextView) findViewById(R.id.reg_display_name);
        mEmail = (TextView) findViewById(R.id.reg_email);
        mPassword = (TextView) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.create_reg_btn);

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!(TextUtils.isEmpty(display_name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

                    mRegProgress.setTitle("Registering");
                    mRegProgress.setMessage("Please wait...");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(display_name, email, password);
                }
            }
        });

    }

    private void register_user(final String display_name, final String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mRegProgress.dismiss();
                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mdbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("image", "default_location_for_image");
                    userMap.put("status", "Hey there.");
                    userMap.put("thumb_image", "default_thumb_image");
                    userMap.put("device_token", deviceToken);

                    mdbRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                            else {
                                mRegProgress.hide();
                                Toast.makeText(RegisterActivity.this, "You Got Some error.", Toast.LENGTH_LONG).show();
                            }

                        }
                    });



                } else {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, "You Got Some error.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}