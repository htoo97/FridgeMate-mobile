package com.example.yangliu.fridgemate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.example.yangliu.fridgemate.authentication.LoginActivity;

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
