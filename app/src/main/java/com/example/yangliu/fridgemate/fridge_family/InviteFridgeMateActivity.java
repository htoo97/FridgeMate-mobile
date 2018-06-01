package com.example.yangliu.fridgemate.fridge_family;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class InviteFridgeMateActivity extends TitleWithButtonsActivity {

    private EditText id;
    private Button inviteBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DocumentReference userDoc;
    private DocumentReference fridgeDoc;

    private static final String members = "members";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_invite_fridge_mate);
        setBackArrow();

        setTitle("Invite a Fridge Mate!");
        id = findViewById(R.id.editText);
        inviteBtn = findViewById(R.id.invite_friend);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userDoc = db.collection("Users").document(user.getEmail());

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc= userData.getDocumentReference("currentFridge");
                }
            }
        });
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String newcomerId = String.valueOf(id.getText());
                // TODO: Sanitize newcomerId. must be a valid User email
                db.collection("Users").whereEqualTo("email",newcomerId)
                        .limit(1).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty())
                                        addUser(null);
                                    else {
                                        addUser(db.collection("Users").document(newcomerId));
                                     finish();
                                    }
                                }
                            }
                        });
            }
        });
    }

    private void addUser(final DocumentReference newOne){
        if (newOne == null) {
            Toast.makeText(InviteFridgeMateActivity.this, "No such user exist", Toast.LENGTH_SHORT).show();
            return;
        }
        // add user to the member list
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentReference> membersList = new ArrayList<>();
                    final DocumentSnapshot fridgeData = task.getResult();
                    if (fridgeData.get(members) != null) {
                        membersList.addAll(((List) fridgeData.get(members)));
                    }
                        membersList.add(newOne);
                        fridgeDoc.update(members, membersList);
                }
            }
        });

        newOne.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                List<DocumentReference> fridges = (List<DocumentReference>) task.getResult().get("fridges");
                fridges.add(fridgeDoc);
                newOne.update("fridges",fridges);
            }
        });
        Toast.makeText(getApplicationContext(), "Invited!", Toast.LENGTH_SHORT).show();
        return;
    }
}
