package com.fridgemate.yangliu.fridgemate.fridge_family;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fridgemate.yangliu.fridgemate.Fridge;
import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.SaveSharedPreference;
import com.fridgemate.yangliu.fridgemate.TitleWithButtonsActivity;
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

import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeListAdapter;
import static com.fridgemate.yangliu.fridgemate.MainActivity.memberListAdapter;

public class CreateFridgeActivity extends TitleWithButtonsActivity {

    private EditText fridgeName;
//    private EditText fridgePassword;
    private Button createBtn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_create_fridge);

        setBackArrow();
        setTitle("Create Fridge Family");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        assert user != null;
        final String email = user.getEmail();
        assert email != null;
        final DocumentReference userDoc = db.collection("Users").document(email);


        fridgeName = findViewById(R.id.fridge_name);
        createBtn = findViewById(R.id.create_fridge_button);

        setTitle("Create Your Fridge Family");

        // Create new fridge
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBtn.setClickable(false);
                // Create new fridge
                Map<String, Object> fridgeData = new HashMap<>();

                final String name = fridgeName.getText().toString();

                // Error if name is blank
                if(name.equals("")){
                    fridgeName.setError(getString(R.string.error_field_required));
                    fridgeName.requestFocus();
                    createBtn.setClickable(true);
                    return;
                }

                // Create fridge document in database
                final DocumentReference userDoc = db.collection("Users").document(email);
                userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    public void onComplete(Task<DocumentSnapshot> task) {
                        final DocumentSnapshot userData = task.getResult();

                        Map<String, Object> fridgeData = new HashMap<>();
                        fridgeData.put("fridgeName", name);
                        fridgeData.put("owner", userDoc);
                        final List<DocumentReference> members = new ArrayList<DocumentReference>();
                        members.add(userDoc);
                        fridgeData.put("members", members);

                        db.collection("Fridges")
                            .add(fridgeData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                public void onSuccess(DocumentReference documentReference) {
                                    // Add newly-created fridge to user's list of fridges
                                    List fridges = new ArrayList<DocumentReference>();
                                    if (userData.get("fridges") != null) {
                                        fridges = (List) userData.get("fridges");
                                    }
                                    // update adapters locally
                                    fridgeListAdapter.mFridges.add(new Fridge(documentReference.getId(),name));
                                    fridgeListAdapter.selectedItemPos = fridgeListAdapter.mFridges.size()-1;
                                    SaveSharedPreference.setCurrentFridge(getApplicationContext(),fridgeListAdapter.selectedItemPos);
                                    fridgeListAdapter.notifyDataSetChanged();
                                    memberListAdapter.names.clear();
                                    memberListAdapter.names.add(userDoc);
                                    memberListAdapter.notifyDataSetChanged();

                                    // update firebase
                                    assert fridges != null;
                                    fridges.add(documentReference);
                                    // Set as current fridge
                                    userDoc.update(
                                        "currentFridge", documentReference,
                                        "fridges", fridges)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                MainActivity.memberListAdapter.syncMemberList();
                                                MainActivity.contentSync = MainActivity.shopListSync= true;
                                                finish();

                                            }
                                        });
                                }
                            });
                    }
                });
            }
        });
    }

    // Return to previous screen on back button
    @Override
    public boolean onSupportNavigateUp(){
        Intent i = new Intent(CreateFridgeActivity.this, CreateJoinFridgeActivity.class);
        startActivity(i);
        finish();

        return true;
    }
}
