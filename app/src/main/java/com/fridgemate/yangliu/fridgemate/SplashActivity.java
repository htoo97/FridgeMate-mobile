package com.fridgemate.yangliu.fridgemate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.fridgemate.yangliu.fridgemate.authentication.LoginActivity;

public class SplashActivity extends Activity {

    //wrap LoginActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //call the real onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // make it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        final ImageView myImageView= findViewById(R.id.splash);
        final TextView myTextView = findViewById(R.id.COOL);
        final TextView myTextView2 = findViewById(R.id.COOL2);
        Animation a = new AlphaAnimation(0.00f,1.00f);
        a.setDuration(1100);
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
        }, 1500);

    }
}
