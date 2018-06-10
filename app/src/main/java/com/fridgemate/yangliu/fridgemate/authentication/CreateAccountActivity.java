package com.fridgemate.yangliu.fridgemate.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.TitleWithButtonsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.os.SystemClock.sleep;

/**
 * A login screen that offers login via email/password.
 */
public class CreateAccountActivity extends TitleWithButtonsActivity {
    private FirebaseAuth mAuth;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_create_account);
        setBackArrow();
        setTitle(R.string.title_activity_create_account);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the signup form.
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSignUp();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.spin_progress);

        Button mSignupButton = (Button) findViewById(R.id.email_signup_button);
        mSignupButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){
                attemptSignUp();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignUp() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user signup attempt.
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Signup success, send verification email
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    user.sendEmailVerification();
                                    Toast.makeText(getApplication(), R.string.email_verification,
                                            Toast.LENGTH_LONG).show();

                                    // Add user to database
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("email", email);

                                    db.collection("Users").document(email)
                                            .set(userData);
                                }
                                finish();
                                showProgress(false);

                            } else {
                                int errorMessage;

                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    errorMessage = R.string.error_invalid_password;
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    errorMessage = R.string.error_invalid_email;
                                } catch(FirebaseAuthUserCollisionException e) {
                                    errorMessage = R.string.error_duplicate_email;
                                } catch(Exception e) {
                                    errorMessage = R.string.error_general;
                                }
                                Toast.makeText(getApplication(), errorMessage,
                                        Toast.LENGTH_LONG).show();

                                // Refresh current activity if invalid email or password
                                if(errorMessage == R.string.error_invalid_email ||
                                        errorMessage == R.string.error_invalid_password){
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                                else{
                                    finish();
                                }
                            }
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) { return email.contains("@"); }

    private boolean isPasswordValid(String password) { return password.length() >= 6; }

    // Return to previous screen on back button
    @Override
    public boolean onSupportNavigateUp(){
        finish();

        return true;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

