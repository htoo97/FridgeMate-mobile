package com.example.yangliu.fridgemate.fridge_family;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.view.View;
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
    private ProgressBar mProgressView;

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

        mEditFormView = findViewById(R.id.profile_form);
        mProgressView = findViewById(R.id.progress);

        name = findViewById(R.id.user_name);
        profilePhoto = findViewById(R.id.profile_image);
        email = findViewById(R.id.email);

        name.setText(user.getDisplayName());
//        profilePhoto.setImageBitmap();
        email.setText(user.getEmail());
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mEditFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEditFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEditFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mEditFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
