package com.fridgemate.yangliu.fridgemate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.fridgemate.yangliu.fridgemate.authentication.LoginActivity;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
//import static android.graphics.Color.*;

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
//            setDoneText("Log In");
//            setSkipText("Skip");

            addSlide(AppIntroFragment.newInstance("Welcome"
                    ,"Here's a quick intro about FridgeMate.", R.drawable.icon1, Color.BLACK));

            addSlide(AppIntroFragment.newInstance("Content List"
                    ,"Every thing in your fridge at a glance.", R.drawable.intro1, Color.BLACK));

            addSlide(AppIntroFragment.newInstance("Put stuff in your fridge"
                    ,"Simply by clicking the + Button.\n Try Quick Add, it scans! \n", R.drawable.intro2, Color.BLACK));



            addSlide(AppIntroFragment.newInstance("Need cooking advice? \nSure!"
                    ,"FridgeMate recommends you recipes based on what you have in your fridge", R.drawable.intro3, Color.BLACK));


            addSlide(AppIntroFragment.newInstance("Fridge? Friends?"
                    ,"Invite friends to your \"fridge family\". \n Connect with people and their foods.", R.drawable.intro4, Color.BLACK));


            addSlide(AppIntroFragment.newInstance("Need a reminder about what to buy?"
                    , "Of course, here's your shopping list.", R.drawable.intro5, Color.BLACK));

            // Instead of fragments, you can also use our default slide
            // Just set a title, description, background and image. AppIntro will do the rest.
            addSlide(AppIntroFragment.newInstance("Welcome to FridgeMate.", "Enjoy! - C.O.O.L", R.drawable.icon1, Color.BLACK));
            // OPTIONAL METHODS
            // Override bar/separator color.
//        setBarColor(parseColor("#8c9497"));
//        setSeparatorColor(parseColor("#f3f4f2"));

            // Hide Skip/Done button.-
            showSkipButton(true);
            setProgressButtonEnabled(true);
        }
        catch(OutOfMemoryError e){
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            setResult(CLOSE_ALL);
            finish();
        }

//        // Turn vibration on and set intensity.
//        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
//        setVibrate(true);
//        setVibrateIntensity(30);
    }

    final int CLOSE_ALL = 23333;

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        setResult(CLOSE_ALL);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        setResult(CLOSE_ALL);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}

//https://github.com/apl-devs/AppIntro