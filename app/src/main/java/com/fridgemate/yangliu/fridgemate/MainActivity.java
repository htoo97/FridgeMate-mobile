package com.fridgemate.yangliu.fridgemate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.fridgemate.yangliu.fridgemate.authentication.LoginActivity;
import com.fridgemate.yangliu.fridgemate.current_contents.ContentListAdapter;
import com.fridgemate.yangliu.fridgemate.current_contents.ContentScrollingFragment;
import com.fridgemate.yangliu.fridgemate.fridge_family.FridgeFamilyFragment;
import com.fridgemate.yangliu.fridgemate.fridge_family.FridgeListAdapter;
import com.fridgemate.yangliu.fridgemate.fridge_family.MemberListAdapter;
import com.fridgemate.yangliu.fridgemate.shop_list.ShopListAdapter;
import com.fridgemate.yangliu.fridgemate.shop_list.ShopListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static long shortAnimTime;
    private Toolbar mToolbar;
    private ConstraintLayout profileImgLayout;
    private CircleImageView profileImg;
    private TextView name;
    private DrawerLayout mDrawLayout;
    public static ProgressBar loadProgress;
    FragmentTransaction fragmentTransaction;

    public static FirebaseAuth mAuth;
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore db;
    public static DocumentReference userDoc;
    public static FirebaseStorage storage;
    public static FirebaseUser user;
    public static DocumentReference fridgeDoc;

    public static final int PROFILE_EDIT_REQUEST_CODE = 4;
    public static final int THEME_CHANGE_REQUEST_CODE = 3;

    public static ContentListAdapter contentListAdapter = null;
    public static MemberListAdapter memberListAdapter;
    public static FridgeListAdapter fridgeListAdapter;
    public static ShopListAdapter shopListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set theme
        if (!SaveSharedPreference.getTheme(this))
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppTheme2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.contents);

        // progress spinning bar set up
        loadProgress = findViewById(R.id.spin_progress);
        shortAnimTime = MainActivity.this.getResources().getInteger(android.R.integer.config_shortAnimTime);
        showProgress(true);

        // set up data base
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userDoc = setUpDatabase();
        storage = FirebaseStorage.getInstance();

        // set up slide menu user profiles
        mDrawLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(this, mDrawLayout, R.string.open, R.string.close);
        mToggle.setDrawerIndicatorEnabled(true);
        mDrawLayout.addDrawerListener(mToggle);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToggle.syncState();
        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView =  navigationView.getHeaderView(0);
        profileImg = headerView.findViewById(R.id.profile_image);
        profileImg.setDrawingCacheEnabled(true);
        profileImgLayout = headerView.findViewById(R.id.profile_layout);
        // allow user to click the profile only after a connection to the database
        profileImgLayout.setClickable(false);
        profileImgLayout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditProfile.class);
                Bitmap b = profileImg.getDrawingCache();
                if (b != null && !b.hasAlpha()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageInByte = baos.toByteArray();
                    intent.putExtra("photo",imageInByte);
                }
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, findViewById(R.id.profile_image), "profile_img");
                startActivityForResult(intent, PROFILE_EDIT_REQUEST_CODE, options.toBundle());
            }
        });
        name = headerView.findViewById(R.id.user_name);
        String displayName = user.getDisplayName();
        if(displayName == null || displayName.equals(""))
            displayName = user.getEmail();
        name.setText(displayName);
        if (user.isAnonymous()){
            name.setText(R.string.sample_name);
        }

        //bottom navigation
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        //list adapters
        contentListAdapter = new ContentListAdapter(this);
        fridgeListAdapter = new FridgeListAdapter(this);
        memberListAdapter = new MemberListAdapter(this);
        shopListAdapter = new ShopListAdapter(this);


        // check and sync with user's documents
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    // initialize fridgeDoc
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                    // finish database connection set up:

                    // entrance animation
                    View view = findViewById(R.id.main_container);
                    Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                    mLoadAnimation.setDuration(800);
                    view.startAnimation(mLoadAnimation);

                    try {
                        // initialize the first tab page
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.main_container, new ContentScrollingFragment());
                        fragmentTransaction.commit();

                        // load user profile image
                        String profileUri = (String) task.getResult().get("profilePhoto");
                        if (profileUri != null && !profileUri.equals("null"))
                            Glide.with(getApplicationContext()).load(Uri.parse(profileUri)).centerCrop().into(profileImg);
                        else {
                            // load from google/faceboook account

                            profileUri = String.valueOf(getIntent().getExtras().get("photo"));
                            if (profileUri != null && !profileUri.equals("") && !profileUri.equals("null")) {
                                Glide.with(getApplicationContext()).load(Uri.parse(profileUri)).centerCrop().into(profileImg);
                                userDoc.update("profilePhoto", profileUri);
                            } else
                                profileImg.setImageDrawable(getResources().getDrawable(R.drawable.profile));


                        }
                    }
                    catch (IllegalStateException e){
                        // when cannot perform getextras() or getintent() on saveInstanceState
                        Log.d("exception: ", e.getMessage());
                        Toast.makeText(MainActivity.this, "Loading failed, please try again. ", Toast.LENGTH_SHORT).show();
                    }

                    // only allow user to change tab after syncing
                    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                    // cancel progressbar
                    showProgress(false);

                    // Create fridge if user has no fridges, Perform first-time fridge setup
                    if (userData.get("currentFridge") == null && (userData.get("fridges") == null
                            || ((List) userData.get("fridges")).isEmpty())) {
                        Toast.makeText(MainActivity.this, R.string.fridge_setup_message, Toast.LENGTH_SHORT).show();
                        // create the first fridge contents
                        Map<String, Object> fridgeData = new HashMap<>();
                        fridgeData.put("fridgeName", "My Fridge");
                        fridgeData.put("owner", userDoc);
                        List<DocumentReference> members = new ArrayList<DocumentReference>();
                        members.add(userDoc);
                        fridgeData.put("members", members);

                        db.collection("Fridges")
                                .add(fridgeData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    public void onSuccess(DocumentReference fridgeDocRef) {
                                        List fridges = new ArrayList<DocumentReference>();
                                        if (userData.get("fridges") != null) {
                                            fridges = (List) userData.get("fridges");
                                        }

                                        final Map<String, Object> itemData = new HashMap<>();
                                        // capitalize name
                                        itemData.put("itemName", "Carrot");
                                        itemData.put("amount", "7");
                                        itemData.put("expirationDate", "07/30/2019");
                                        itemData.put("imageID", "https://diabetesmealplans.com/wp-content/uploads/2015/11/carrots.jpg");
                                        List<String> shopList = new LinkedList<>();
                                        shopList.add("Apple#10");
                                        shopList.add("Banana#1");
                                        fridgeDocRef.update("shoppingList", shopList);
                                        fridges.add(fridgeDocRef);
                                        fridgeDocRef.collection("FridgeItems").add(itemData);

                                        userDoc.update(
                                                "currentFridge", fridgeDocRef,
                                                "fridges", fridges)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(MainActivity.this,
                                                                R.string.fridge_setup_complete, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                });
                    }
                    // the user has fridges
                    else {
                        // sync lists when entering the app for better transitions
                        memberListAdapter.syncMemberList();
                        FridgeFamilyFragment.syncFridgeList();
                    }
                }
            }
        });

        // Return to login screen if cannot verify user identity
        if(userDoc == null){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        // slide menu options function set up
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.profile_settings:
                        intent = new Intent(MainActivity.this, EditProfile.class);
                        startActivityForResult(intent, PROFILE_EDIT_REQUEST_CODE);
                        return true;
                    case R.id.setting:
                        intent = new Intent(MainActivity.this, themeActivity.class);
                        startActivityForResult(intent,THEME_CHANGE_REQUEST_CODE);
                        return true;
                    case R.id.feedback:
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"fridgematehelp@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "Hi, about FridgeMate... ");
                        i.putExtra(Intent.EXTRA_TEXT   , "");
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this, R.string.cantemail, Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.log_out:
                        // Sign out user from database and go back to signin screen
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Logout")
                                .setMessage("Are you sure you want to log out?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        mAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        if (user.isAnonymous()){
                                            user.delete();
                                        }
                                        finish();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();

                        return true;
                }
                return true;
            }
        });
    }


    // Set up database functions when app opened
    private DocumentReference setUpDatabase(){

        if (user == null){
            Toast.makeText(getApplication(), R.string.error_load_data,
                    Toast.LENGTH_LONG).show();
            mAuth.signOut();
            return null;
        }


        // make anonymous account access fridgematehelp@gmail.com
        if (user.isAnonymous()){
            return db.collection("Users").document("fridgematehelp@gmail.com");
        }


        final String email = user.getEmail();

        assert email != null;
        if (db == null ||db.collection("Users") == null){
            Toast.makeText(this, R.string.connecting, Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        DocumentReference documentReference = db.collection("Users").document(email);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Create document for user if doesn't already exist
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        String profileURI = String.valueOf(getIntent().getExtras().get("photo"));
                        if (profileURI != null && !profileURI.equals(""))
                            userData.put("profilePhoto", profileURI);


                        db.collection("Users").document(email)
                                .set(userData);
                        profileImgLayout.setClickable(true);
                    }
                } else {
                    Log.d("set_up_database", "get failed with ", task.getException());
                }
            }
        });

        return documentReference;
    }

    // bottom view navigation option function
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            View view = findViewById(R.id.main_container);
            Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            mLoadAnimation.setDuration(1000);
            view.startAnimation(mLoadAnimation);
            showProgress(true);
            // check internet connection
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() == null)
                Toast.makeText(MainActivity.this, R.string.connecting, Toast.LENGTH_SHORT).show();

            switch (item.getItemId()) {
                case R.id.current_fridge:
                    mToolbar.setElevation(4);
                    // check if it's already in current_fridge
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ContentScrollingFragment(),"content");
                    fragmentTransaction.commit();
                    mToolbar.setTitle(R.string.contents_title);
                    return true;
                case R.id.navigation_dashboard:
                    mToolbar.setElevation(0);
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new FridgeFamilyFragment(),"family");
                    fragmentTransaction.commit();
                    mToolbar.setTitle(R.string.family);
                    return true;
                case R.id.shopping_list:
                    mToolbar.setElevation(0);
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ShopListFragment(),"wishlist");
                    fragmentTransaction.commit();
                    mToolbar.setTitle(R.string.shopping_list);
                    return true;
            }
            return false;
        }
    };

    // Open/close the slide menu by clicking action button onthe tool bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (this.mDrawLayout.isDrawerOpen(GravityCompat.START)) {
                    this.mDrawLayout.closeDrawer(GravityCompat.START);
                }
                else
                    this.mDrawLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return false;
    }
    // Close the slide menu by pressing Back
    @Override
    public void onBackPressed() {
        if (this.mDrawLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    final int CLOSE_ALL = 23333;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // close all temp pages that anonymous account used
        if (resultCode == CLOSE_ALL) {
            user.delete().addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
            );
        }

        // update the image when edited the profile
        if (requestCode == PROFILE_EDIT_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                // use had made changes to the profile and successfully saved

                userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String profileUri = (String) task.getResult().get("profilePhoto");
                        if (profileUri != null && !profileUri.equals("null")){
                            Glide.with(getApplicationContext()).load(Uri.parse(profileUri)).centerCrop().into(profileImg);
                        }
                    }
                });
                name.setText(user.getDisplayName());
            }
        }else if (requestCode == THEME_CHANGE_REQUEST_CODE){
                recreate();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        loadProgress.setVisibility(show ? View.GONE : View.VISIBLE);
        loadProgress.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadProgress.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        loadProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        loadProgress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
