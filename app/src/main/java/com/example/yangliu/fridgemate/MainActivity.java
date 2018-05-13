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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.example.yangliu.fridgemate.current_contents.ContentScrollingFragment;
import com.example.yangliu.fridgemate.fridge_family.FridgeFamilyFragment;
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
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Slide Menu set up
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
                    case R.id.action_settings:
                        //TODO::settings
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


    // Search button set up
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(MainActivity.this.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
//        searchView.setSubmitButtonEnabled(true);
        return true;
    }
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String keyword = intent.getStringExtra(SearchManager.QUERY);
            // TODO:: use this key word filtered list
        }
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
