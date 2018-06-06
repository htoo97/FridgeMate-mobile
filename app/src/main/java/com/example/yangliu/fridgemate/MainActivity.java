package com.example.yangliu.fridgemate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
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
import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.example.yangliu.fridgemate.current_contents.ContentListAdapter;
import com.example.yangliu.fridgemate.current_contents.ContentScrollingFragment;
import com.example.yangliu.fridgemate.fridge_family.FridgeFamilyFragment;
import com.example.yangliu.fridgemate.fridge_family.FridgeListAdapter;
import com.example.yangliu.fridgemate.fridge_family.MemberListAdapter;
import com.example.yangliu.fridgemate.shop_list.ShopListAdapter;
import com.example.yangliu.fridgemate.shop_list.ShopListFragment;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView profileImg;
    private TextView name;
    private DrawerLayout mDrawLayout;
    private ActionBarDrawerToggle mToggle;
    public ProgressBar loadProgress;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;

    public static FirebaseAuth mAuth;
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore db;
    public static DocumentReference userDoc;
    public static FirebaseStorage storage;
    public static FirebaseUser user;
    public static DocumentReference fridgeDoc;

    public static final int PROFILE_EDIT_REQUEST_CODE = 4;

    public static ContentListAdapter contentListAdapter = null;
    public static MemberListAdapter memberListAdapter;
    public static FridgeListAdapter fridgeListAdapter;
    public static ShopListAdapter shopListAdapter;

    public static boolean contentSync;
    public static boolean familySync;
    public static boolean shopListSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle("FridgeMate");

        // progress spinning bar set up
        loadProgress = findViewById(R.id.spin_progress);
        showProgress(true);
        // allow first time sync when user enters the app
        familySync = shopListSync = contentSync = true;

        // set up data base
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userDoc = setUpDatabase();
        storage = FirebaseStorage.getInstance();

        // set up slide menu user profiles
        mDrawLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawLayout,R.string.open,R.string.close);
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
        profileImg.setOnClickListener(new View.OnClickListener() {
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
        if(displayName == null || displayName.equals("")) displayName = user.getEmail();
        name.setText(displayName);

        //bottom navigation
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        // check and sync with user's documents
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    // initialize fridgeDoc
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                    // finish database connection set up:

                    // entrace
                    View view = findViewById(R.id.main_container);
                    Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                    mLoadAnimation.setDuration(800);
                    view.startAnimation(mLoadAnimation);
                    // initialize the first tab page
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ContentScrollingFragment());
                    fragmentTransaction.commit();
                    showProgress(false);

                    // load user profile image
                    String profileUri = (String) task.getResult().get("profilePhoto");
                    if (profileUri != null && !profileUri.equals("null")){
                        Glide.with(getApplicationContext()).load(Uri.parse(profileUri)).centerCrop().into(profileImg);
                    }

                    // only allow user to change tab after syncing
                    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

                    // create the first fridge if necessary
                    // Create fridge if user has no fridges
                    // Perform first-time fridge setup
                    if(userData.get("currentFridge") == null
                            && ( userData.get("fridges") == null || ((List)userData.get("fridges")).isEmpty() )) {
                        Toast.makeText(MainActivity.this, R.string.fridge_setup_message, Toast.LENGTH_SHORT).show();
                        Map<String, Object> fridgeData = new HashMap<>();
                        fridgeData.put("fridgeName", "My Fridge");
                        fridgeData.put("owner", userDoc);
                        List<DocumentReference> members = new ArrayList<DocumentReference>();
                        members.add(userDoc);
                        fridgeData.put("members", members);

                        db.collection("Fridges")
                                .add(fridgeData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    public void onSuccess(DocumentReference documentReference) {
                                        List fridges = new ArrayList<DocumentReference>();
                                        if(userData.get("fridges") != null){
                                            fridges = (List)userData.get("fridges");
                                        }

                                        fridges.add(documentReference);

                                        userDoc.update(
                                                "currentFridge", documentReference,
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
                }
            }
        });

        // Return to login screen if cannot verify user identity
        if(userDoc == null){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        contentListAdapter = new ContentListAdapter(this);
        fridgeListAdapter = new FridgeListAdapter(this);
        memberListAdapter = new MemberListAdapter(this);
        shopListAdapter = new ShopListAdapter(this);


        final String email = user.getEmail();

        // slide menu options function set up
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.profile_settings:
                        Intent intent = new Intent(MainActivity.this, EditProfile.class);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, findViewById(R.id.profile_image), "profile_img");
                        startActivityForResult(intent, PROFILE_EDIT_REQUEST_CODE, options.toBundle());
                        return true;
                    case R.id.log_out:
                        // Sign out user from database and go back to signin screen
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Logout")
                                .setMessage("Are you sure you want to log out?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        mAuth.signOut();
                                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(i);
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

        final String email = user.getEmail();

        assert email != null;
        DocumentReference documentReference = db.collection("Users").document(email);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Create document for user if doesn't already exist
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);

                        db.collection("Users").document(email)
                                .set(userData);
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
            mLoadAnimation.setDuration(800);
            view.startAnimation(mLoadAnimation);
            switch (item.getItemId()) {
                case R.id.current_fridge:
                    // check if it's already in current_fridge
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ContentScrollingFragment(),"content");
                    fragmentTransaction.commit();
                    mToolbar.setTitle("Current Contents");

                    return true;
                case R.id.navigation_dashboard:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new FridgeFamilyFragment(),"family");
                    fragmentTransaction.commit();
                    mToolbar.setTitle("Fridge Family");
                    return true;
                case R.id.shopping_list:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ShopListFragment(),"wishlist");
                    fragmentTransaction.commit();
                    mToolbar.setTitle("Shopping List");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loadProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            loadProgress.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
