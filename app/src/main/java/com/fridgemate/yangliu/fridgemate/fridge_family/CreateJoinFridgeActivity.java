package com.fridgemate.yangliu.fridgemate.fridge_family;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.TitleWithButtonsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;

import static com.fridgemate.yangliu.fridgemate.MainActivity.userDoc;

public class CreateJoinFridgeActivity extends TitleWithButtonsActivity {

    private EditText fridgeInput;
    private Button createBtn;
    private Button joinBtn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_create_join_fridge);
        setBackArrow();
        setTitle("New Fridge");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        fridgeInput = findViewById(R.id.editText);
        joinBtn = findViewById(R.id.invite_friend);
        createBtn = findViewById(R.id.create_fridge);

        assert user != null;

        joinBtn.setOnClickListener(new View.OnClickListener() {
            // Join the selected fridge ID
            @Override
            public void onClick(View v) {
                String fridge =  String.valueOf(fridgeInput.getText());

                // Cancel and display error if String is empty
                if(fridge == null || fridge.isEmpty()){
                    fridgeInput.setError(getString(R.string.error_field_required));
                    return;
                }

                joinBtn.setClickable(false);
                final DocumentReference newfridgeDoc = db.collection("Fridges").document(fridge);
                newfridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot fridgeData = task.getResult();
                        if (fridgeData.exists()) {
                            List members = (List)fridgeData.get("members");
                            if(members == null){
                                members = new ArrayList<>();
                            }

                            // Cancel if user is already member of fridge
                            if(members.contains(userDoc)){
                                Toast.makeText(CreateJoinFridgeActivity.this,
                                        R.string.join_fridge_duplicate, Toast.LENGTH_LONG).show();
                                joinBtn.setClickable(true);
                                return;
                            }
                            else {

                                newfridgeDoc.update("joinRequest",userDoc).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(CreateJoinFridgeActivity.this, R.string.sent_join, Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }
                                );
                            }
                        }
                        else {
                            Toast.makeText(CreateJoinFridgeActivity.this,
                                    R.string.join_fridge_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBtn.setClickable(false);
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
