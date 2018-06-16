package com.fridgemate.yangliu.fridgemate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import static android.graphics.Color.*;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
//        addSlide(firstFragment);
//        addSlide(secondFragment);
//        addSlide(thirdFragment);
//        addSlide(fourthFragment);

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle("Welcome!");
        sliderPage1.setDescription("Here's a quick intro about FridgeMate.");
        sliderPage1.setImageDrawable(R.drawable.icon1);
        sliderPage1.setBgColor(Color.TRANSPARENT);
        addSlide(AppIntroFragment.newInstance(sliderPage1));

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle("Content List");
        sliderPage2.setDescription("Every thing in your fridge at a glance.");
        sliderPage2.setImageDrawable(R.drawable.intro1);
        sliderPage2.setBgColor(Color.TRANSPARENT);
        addSlide(AppIntroFragment.newInstance(sliderPage2));

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle("Put stuff in your fridge");
        sliderPage3.setDescription("Simply by clicking the + Button.\n Try Quick Add, it scans! \n");
        sliderPage3.setImageDrawable(R.drawable.intro2);
        sliderPage3.setBgColor(Color.TRANSPARENT);
        addSlide(AppIntroFragment.newInstance(sliderPage3));

        SliderPage sliderPage4 = new SliderPage();
        sliderPage4.setTitle("Need cooking advice? \nSure!");
        sliderPage4.setDescription("FridgeMate recommends you recipes based on what you have in your fridge");
        sliderPage4.setImageDrawable(R.drawable.intro3);
        sliderPage4.setBgColor(Color.TRANSPARENT);
        addSlide(AppIntroFragment.newInstance(sliderPage4));

        SliderPage sliderPage5 = new SliderPage();
        sliderPage5.setTitleTypeface("casual");
        sliderPage5.setTitle("Fridge? Friends?");
        sliderPage5.setDescription("Invite friends to your \"fridge family\". \n Connect with people and their foods.");
        sliderPage5.setImageDrawable(R.drawable.intro4);
        sliderPage5.setBgColor(Color.TRANSPARENT);
        addSlide(AppIntroFragment.newInstance(sliderPage5));

        SliderPage sliderPage6 = new SliderPage();
        sliderPage6.setTitleTypeface("casual");
        sliderPage6.setTitle("Need a reminder about what to buy?");
        sliderPage6.setDescription("Of course, here's your shopping list.");
        sliderPage6.setImageDrawable(R.drawable.intro5);
        sliderPage6.setBgColor(Color.TRANSPARENT);
        addSlide(AppIntroFragment.newInstance(sliderPage6));

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance("Welcome to FridgeMate.", "Enjoy! - C.O.O.L", R.drawable.icon1, R.drawable.animation_gradient));
        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(parseColor("#8c9497"));
        setSeparatorColor(parseColor("#f3f4f2"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

//        // Turn vibration on and set intensity.
//        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
//        setVibrate(true);
//        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}

//https://github.com/apl-devs/AppIntro