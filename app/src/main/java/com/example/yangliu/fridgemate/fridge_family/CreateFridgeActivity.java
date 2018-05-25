package com.example.yangliu.fridgemate.fridge_family;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yangliu.fridgemate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateFridgeActivity extends AppCompatActivity {

    private EditText fridgeName;
    private EditText fridgePassword;
    private Button createBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fridge);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        final String email = user.getEmail();
        final DocumentReference userDoc = db.collection("Users").document(email);


        fridgeName = findViewById(R.id.fridge_name);
        createBtn = findViewById(R.id.create_fridge_button);

        setTitle("Create Your Fridge Family");


        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new fridge
                Map<String, Object> fridgeData = new HashMap<>();

                final String name = fridgeName.getText().toString();

                final DocumentReference userDoc = db.collection("Users").document(email);
                userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    public void onComplete(Task<DocumentSnapshot> task) {
                        final DocumentSnapshot userData = task.getResult();

                        Map<String, Object> fridgeData = new HashMap<>();
                        fridgeData.put("fridgeName", name);
                        fridgeData.put("owner", userDoc);

                        db.collection("Fridges")
                                .add(fridgeData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    public void onSuccess(DocumentReference documentReference) {
                                        List<DocumentReference> fridges = new ArrayList<DocumentReference>();
                                        if (userData.get("fridges") != null) {
                                            fridges = (List) userData.get("fridges");
                                        }

                                        fridges.add(documentReference);

                                        userDoc.update(
                                                "currentFridge", documentReference,
                                                "fridges", fridges);
                                    }
                                });
                    }
                });

                finish();
            }
        });
    }
}
