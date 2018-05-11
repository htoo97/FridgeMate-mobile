package com.example.yangliu.fridgemate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView profileImg;
    private DrawerLayout mDrawLayout;
    private ActionBarDrawerToggle mToggle;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);


        // Navigation drawer set up
        profileImg = findViewById(R.id.profile_image);
        mDrawLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawLayout,R.string.open,R.string.close);
        mToggle.setDrawerIndicatorEnabled(true);
        mDrawLayout.addDrawerListener(mToggle);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToggle.syncState();

        // initialize the first tab pagg
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, new ScrollingFragment());
        fragmentTransaction.commit();

        // Navigation drawer option functionality
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile_settings:
                        //TODO::settings
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
                    case R.id.action_settings:
                        //TODO::settings
                        return true;
                    case R.id.menu_refresh:
                        //TODO: refresh here
                        //adapter.notifyDataSetChanged();
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

    // bottom view navigation option function
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.current_fridge:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_container, new ScrollingFragment());
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_dashboard:

                    return true;
                case R.id.shopping_list:

                    return true;
            }
            return false;
        }
    };

    // Open/close the navigation drawer by clicking the action button on the tool bar
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
    // Close the navigation drawer by pressing Back
    @Override
    public void onBackPressed() {
        if (this.mDrawLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
