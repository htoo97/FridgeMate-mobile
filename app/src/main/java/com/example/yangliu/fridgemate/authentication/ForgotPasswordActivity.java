package com.example.yangliu.fridgemate.authentication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPasswordActivity extends TitleWithButtonsActivity {


    private EditText email;
    private FirebaseAuth mAuth;
    private Button forgotBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_forgot_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_back_material);

        email = findViewById(R.id.email_address_find);


        forgotBtn = findViewById(R.id.forgotBtn);
        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_address = String.valueOf(email.getText());
                if (isEmailValid(email_address))
                mAuth.getInstance().sendPasswordResetEmail(email_address)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                else{
                    Toast.makeText(ForgotPasswordActivity.this, "Please input a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }


    // Return to previous screen on back button
    @Override
    public boolean onSupportNavigateUp(){
        finish();

        return true;
    }
}
