package com.example.yangliu.fridgemate;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.example.yangliu.fridgemate.current_contents.ContentScrollingFragment;
import com.example.yangliu.fridgemate.fridge_family.FridgeFamilyFragment;
import com.example.yangliu.fridgemate.shop_list.ShopListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView profileImg;
    private TextView name;
    private DrawerLayout mDrawLayout;
    private ActionBarDrawerToggle mToggle;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDoc;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("FridgeMate");


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userDoc = setUpDatabase();

        // Return to login screen if cannot verify user identity
        if(userDoc == null){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        final String email = user.getEmail();

        // Create fridge if user has no fridges
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();

                    // Perform first-time fridge setup
                    if(userData.get("currentFridge") == null
                            && ( userData.get("fridges") == null || ((List)userData.get("fridges")).isEmpty() )) {
                        Toast.makeText(MainActivity.this, R.string.fridge_setup_message, Toast.LENGTH_SHORT).show();
                        Map<String, Object> fridgeData = new HashMap<>();
                        fridgeData.put("fridgeName", "My Fridge");
                        fridgeData.put("owner", userDoc);

                        db.collection("Fridges")
                            .add(fridgeData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                public void onSuccess(DocumentReference documentReference) {
                                    List<DocumentReference> fridges = new ArrayList<DocumentReference>();
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


        // Slide Menu set up
        mDrawLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawLayout,R.string.open,R.string.close);
        mToggle.setDrawerIndicatorEnabled(true);
        mDrawLayout.addDrawerListener(mToggle);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        profileImg = headerView.findViewById(R.id.profile_image);
        name = (TextView)headerView.findViewById(R.id.user_name);
        // TODO:: DATABASE:: get profile image and name from
        // profileImg.setImageBitmap();

        String displayName = user.getDisplayName();
        if(displayName == null || displayName.equals("")){
            displayName = user.getEmail();
        }

        name.setText(displayName);

        // initialize the first tab page
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, new ContentScrollingFragment());
        fragmentTransaction.commit();

        // slide menu options function
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.profile_settings:
                        //TODO::settings
                        Intent intent = new Intent(MainActivity.this, EditProfile.class);
                        startActivity(intent);
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

                        // TODO: store local account data?
                        // SaveSharedPreference.clearUserName(MainActivity.this);
                        return true;
                }
                return true;
            }
        });

        //bottom navigation
        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
            switch (item.getItemId()) {
                case R.id.current_fridge:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ContentScrollingFragment());
                    fragmentTransaction.commit();
                    mToolbar.setTitle("Current Contents");
                    return true;
                case R.id.navigation_dashboard:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new FridgeFamilyFragment());
                    fragmentTransaction.commit();
                    mToolbar.setTitle("Fridge Family");
                    return true;
                case R.id.shopping_list:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ShopListFragment());
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
        super.onActivityResult(requestCode, resultCode, data);
    }

}
