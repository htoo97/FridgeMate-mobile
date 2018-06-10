package com.fridgemate.yangliu.fridgemate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.fridgemate.yangliu.fridgemate.authentication.LoginActivity;

public class SplashActivity extends Activity {

    //wrap LoginActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set theme
        if (!SaveSharedPreference.getTheme(this))
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppTheme2);
        //call the real onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // make it full screen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);


        final ImageView myImageView= findViewById(R.id.splash);
        final TextView myTextView = findViewById(R.id.COOL);
        final TextView myTextView2 = findViewById(R.id.COOL2);
        Animation a = new AlphaAnimation(0.00f,1.00f);
        a.setDuration(900);
        myImageView.startAnimation(a);
        myTextView.startAnimation(a);
        myTextView2.startAnimation(a);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 1200);

    }
}
