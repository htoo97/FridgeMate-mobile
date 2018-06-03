package com.example.yangliu.fridgemate.fridge_family;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberProfileActivity extends TitleWithButtonsActivity {

    private ConstraintLayout mEditFormView;

    private TextView name;
    private CircleImageView profilePhoto;
    private TextView email;
    private Button saveBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_member_profile);
        setBackArrow();
        setTitle("Profile");


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user == null){
            Toast.makeText(getApplication(), R.string.error_load_data,
                    Toast.LENGTH_LONG).show();
            mAuth.signOut();

            Intent i = new Intent(MemberProfileActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }


        name = findViewById(R.id.user_name);
        profilePhoto = findViewById(R.id.profile_image);
        email = findViewById(R.id.email);

        name.setText(user.getDisplayName());
//        profilePhoto.setImageBitmap();
        email.setText(user.getEmail());
    }

}
