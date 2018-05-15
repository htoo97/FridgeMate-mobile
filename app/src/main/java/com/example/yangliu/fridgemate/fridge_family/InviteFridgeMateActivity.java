package com.example.yangliu.fridgemate.fridge_family;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yangliu.fridgemate.R;

public class InviteFridgeMateActivity extends AppCompatActivity {

    private EditText id;
    private Button inviteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_fridge_mate);


        setTitle("Invite a Fridge Mate!");
        id = findViewById(R.id.editText);
        inviteBtn = findViewById(R.id.invite_friend);

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fridgId =  String.valueOf(id.getText());
                // TODO:: DATABASE:: add a fridge member

                finish();
            }
        });

    }
}
