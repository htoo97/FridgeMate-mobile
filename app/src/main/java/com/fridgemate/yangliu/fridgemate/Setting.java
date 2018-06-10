package com.fridgemate.yangliu.fridgemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Setting extends TitleWithButtonsActivity {
    private final boolean MIRROR = false;
    private final boolean SHAPE = true;

    private ImageView waterTheme;
    private ImageView shapeTheme;
    private Button saveBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SaveSharedPreference.getTheme(Setting.this) == MIRROR)
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppTheme2);

        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_setting);
        setBackArrow();
        setTitle("Themes");

        waterTheme = findViewById(R.id.Water);
        shapeTheme = findViewById(R.id.Shape);

        final Intent intent = getIntent();
        waterTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSharedPreference.setTheme(Setting.this, MIRROR);
                finish();
                startActivity(intent);
            }
        });

        shapeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSharedPreference.setTheme(Setting.this,SHAPE);
                finish();
                startActivity(intent);
            }
        });

        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
