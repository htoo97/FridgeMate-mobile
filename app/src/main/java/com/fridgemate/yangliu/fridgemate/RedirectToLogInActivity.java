package com.fridgemate.yangliu.fridgemate;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RedirectToLogInActivity extends AppCompatActivity {

    private AnimationDrawable anim;

    final int REQUEST_NEW_ACCOUNT = 233;
    final int CLOSE_ALL = 23333;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEW_ACCOUNT){
            if (resultCode == CLOSE_ALL){
                setResult(CLOSE_ALL);
                finish();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect_to_log_in);

        // set theme
        if (!SaveSharedPreference.getTheme(this))
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppTheme2);

        // background animation
        anim = (AnimationDrawable) findViewById(R.id.bg_layout).getBackground();
        anim.setEnterFadeDuration(6000);
        anim.setExitFadeDuration(2000);


        Button toLogIn = findViewById(R.id.to_log_in);
        toLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(CLOSE_ALL);
                finish();
            }
        });

        ImageButton toIntro = findViewById(R.id.introBtn);
        toIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RedirectToLogInActivity.this, IntroActivity.class);
                startActivityForResult(i,REQUEST_NEW_ACCOUNT);
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

}
