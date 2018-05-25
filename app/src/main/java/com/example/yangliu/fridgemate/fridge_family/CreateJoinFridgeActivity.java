package com.example.yangliu.fridgemate.fridge_family;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yangliu.fridgemate.R;

public class CreateJoinFridgeActivity extends AppCompatActivity {

    private EditText id;
    private Button createBtn;
    private Button joinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_join_fridge);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setTitle("Create/Join a Fridge Family!");
        id = findViewById(R.id.editText);
        joinBtn = findViewById(R.id.invite_friend);
        createBtn = findViewById(R.id.create_fridge);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fridgId =  String.valueOf(id.getText());
                //TODO:: DATABASE let user to join fridge

                finish();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateJoinFridgeActivity.this, CreateFridgeActivity.class);
                startActivity(i);
                finish();
            }
        });

    }


    // Return to previous screen on back button
    @Override
    public boolean onSupportNavigateUp(){
        finish();

        return true;
    }
}
