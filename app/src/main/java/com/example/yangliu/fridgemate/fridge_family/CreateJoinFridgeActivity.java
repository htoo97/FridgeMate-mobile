package com.example.yangliu.fridgemate.fridge_family;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class CreateJoinFridgeActivity extends TitleWithButtonsActivity {

    private EditText fridgeInput;
    private Button createBtn;
    private Button joinBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_create_join_fridge);
        setBackArrow();
        setTitle("Create/Join a Fridge Family!");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        fridgeInput = findViewById(R.id.editText);
        joinBtn = findViewById(R.id.invite_friend);
        createBtn = findViewById(R.id.create_fridge);

        email = user.getEmail();
        final DocumentReference userDoc = db.collection("Users").document(email);

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


                final DocumentReference fridgeDoc = db.collection("Fridges").document(fridge);
                fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot fridgeData = task.getResult();
                        if (fridgeData.exists()) {
                            List<DocumentReference> members = (List)fridgeData.get("members");
                            if(members == null){
                                members = new ArrayList<>();
                            }

                            // Cancel if user is already member of fridge
                            if(members.contains(userDoc)){
                                Toast.makeText(CreateJoinFridgeActivity.this,
                                        R.string.join_fridge_duplicate, Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Add user to fridge's members
                            members.add(userDoc);
                            fridgeDoc.update("members", members);

                            // Add fridge to user's list of fridges
                            addToFridges(fridgeDoc);

                            String fridgeName = fridgeData.getString("fridgeName");
                            Toast.makeText(CreateJoinFridgeActivity.this,
                                    getResources().getString(R.string.join_fridge_success, fridgeName),
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(CreateJoinFridgeActivity.this,
                                    R.string.join_fridge_error, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                });
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

    // Add fridge to user's list of fridges
    private void addToFridges(final DocumentReference fridge){
        final DocumentReference userDoc = db.collection("Users").document(email);

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                final DocumentSnapshot userData = task.getResult();

                List<DocumentReference> fridges = (List)userData.get("fridges");
                fridges.add(fridge);

                userDoc.update("fridges", fridges)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    });
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
