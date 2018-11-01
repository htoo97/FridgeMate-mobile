package com.fridgemate.yangliu.fridgemate.authentication;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fridgemate.yangliu.fridgemate.IntroActivity;
import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressBar mLoginProgressBar;

    private SignInButton mGoogleBtn;
    private static final int RC_SIGN_IN = 101;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "login with google";

    private Button mEmailSignInButton;
    AnimationDrawable anim;

    // fb auth
    CallbackManager mCallbackManager;
    LoginButton mFBBtn;

    //anym auth
    private Button anonymBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginProgressBar = findViewById(R.id.login_progressBar);

        mAuth = FirebaseAuth.getInstance();

        boolean firstTime = getPreferences(MODE_PRIVATE).getBoolean("Intro", true);
        if (firstTime) {
            Intent tutorialIntent = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(tutorialIntent);
            getPreferences(MODE_PRIVATE).edit().putBoolean("Intro", false).apply();
        }

        // Automatically go to user home if already logged in (and email verified)
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Log.d("autologin", "Current user: " + user.getEmail());
        }
        if (user != null && (user.isEmailVerified() || user.isAnonymous())) {

            View view = findViewById(android.R.id.content);
            // enter app with animation
            Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            mLoadAnimation.setDuration(800);
            view.startAnimation(mLoadAnimation);
            enterApp();
        }

        anim = (AnimationDrawable) findViewById(R.id.log_in_layout).getBackground();
        anim.setEnterFadeDuration(6000);
        anim.setExitFadeDuration(2000);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        mEmailView.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasInternet())
                    attemptLogin();
            }
        });

        Button mCreateAccount = findViewById(R.id.create_account);
        mCreateAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), CreateAccountActivity.class);
                startActivity(i);
            }
        });

        Button forgotPasswordBtn = findViewById(R.id.forgot_password);
        forgotPasswordBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ForgotPasswordActivity.class);
                startActivity(i);
            }
        });

        //************* google login authentication **********//
        // Configure Google Sign In
        mGoogleBtn = findViewById(R.id.googleBtn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasInternet()) {
                    mGoogleBtn.setClickable(false);
                    signIn();
                }

            }
        });

        //************* facebook login authentication **********//
        mCallbackManager = CallbackManager.Factory.create();
        mFBBtn = findViewById(R.id.fb_button);
        mFBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasInternet())
                    fbSignIn();
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn){
            if (mAuth.getCurrentUser()== null)
                LoginManager.getInstance().logOut();
            else {
                enterApp();
            }
        }

        //************* anonymous login authentication **********//
        anonymBtn = findViewById(R.id.anonym_log_in);
        anonymBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasInternet()) {
                    anonymBtn.setClickable(false);
                    mAuth.signInAnonymously().addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            // Only sign in if user's email is verified
                                            if (user.isAnonymous()) {
                                                enterApp();
                                            } else {
                                                Toast.makeText(getApplication(), R.string.error_general, Toast.LENGTH_LONG).show();
                                                anonymBtn.setClickable(true);
                                            }
                                        }
                                    }
                                }
                            }
                    );
                }
            }
        });

    }

    private void enterApp(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
    private boolean hasInternet(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            Toast.makeText(LoginActivity.this, R.string.connecting, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void fbSignIn(){
        mFBBtn.setReadPermissions("email");
        mFBBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                if (!loginResult.getRecentlyGrantedPermissions().contains("email")) {
                    Toast.makeText(LoginActivity.this, "Please provide your email", Toast.LENGTH_SHORT).show();
                    LoginManager.getInstance().logOut();
                    return;
                }
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            final Intent i = new Intent(getApplicationContext(), MainActivity.class);

                            // get profile photo
                            Bundle params = new Bundle();
                            params.putString("fields", "id,email,gender,cover,picture.type(large)");
                            new GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            if (response != null) {
                                                try {
                                                    JSONObject data = response.getJSONObject();
                                                    if (data.has("picture")) {
                                                        String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                                        i.putExtra("photo",profilePicUrl);
//                                                        mAuth.getCurrentUser().updateEmail(String.valueOf(data.getJSONObject("email")));
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            startActivity(i);
                                            finish();
                                            Toast.makeText(getApplicationContext(), R.string.log_in_success, Toast.LENGTH_SHORT).show();

                                        }
                                    }).executeAsync();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
//                            updateUI(null);
                        }
                        mLoginProgressBar.setVisibility(View.GONE);
                        // ...
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (anim != null && !anim.isRunning())
            anim.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (anim != null && anim.isRunning())
            anim.stop();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed" + e.getMessage(), e);
                mGoogleBtn.setClickable(true);
            }
            mLoginProgressBar.setVisibility(View.GONE);
        }
        else {
            // Pass the activity result back to the Facebook SDK
            mLoginProgressBar.setVisibility(View.VISIBLE);
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        mLoginProgressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();

                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtra("photo",task.getResult().getUser().getPhotoUrl());
                            startActivity(i);
                            finish();
                            Toast.makeText(getApplicationContext(), R.string.log_in_success, Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            mLoginProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        mEmailSignInButton.setClickable(false);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

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
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.

            // avoid abusive loggin
            mEmailSignInButton.setClickable(true);
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user!= null){
                                    // Only sign in if user's email is verified
                                    if (user.isEmailVerified()) {
                                        enterApp();

                                    } else {
                                        // Re-send account validation email if not verified
                                        user.sendEmailVerification();
                                        Toast.makeText(getApplication(), R.string.error_email_not_validated,
                                                Toast.LENGTH_LONG).show();
                                        mEmailSignInButton.setClickable(true);
                                    }
                                }

                            } else { // Display error message if cannot login
                                int errorMessage = R.string.error_login;

                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (Exception e) {
                                    try {
                                        String errorCode = ((FirebaseAuthException) e).getErrorCode();
                                        Log.d("login_failed", "Exception: " + e.getMessage()
                                                + ", Error code: " + errorCode);

                                        if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                                            errorMessage = R.string.error_no_such_email;
                                        } else if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                                            errorMessage = R.string.error_invalid_email;
                                        }
                                    }catch (ClassCastException ignored){
                                    }
                                }

                                Toast.makeText(getApplication(), errorMessage, Toast.LENGTH_LONG).show();

                                mEmailSignInButton.setClickable(true);
                            }
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}