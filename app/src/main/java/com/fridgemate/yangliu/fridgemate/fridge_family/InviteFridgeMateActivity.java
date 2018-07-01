package com.fridgemate.yangliu.fridgemate.fridge_family;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fridgemate.yangliu.fridgemate.MainActivity;
import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.TitleWithButtonsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fridgemate.yangliu.fridgemate.MainActivity.fridgeDoc;
import static com.fridgemate.yangliu.fridgemate.MainActivity.memberListAdapter;

public class InviteFridgeMateActivity extends TitleWithButtonsActivity {

    private EditText id;

    private FirebaseFirestore db;

    private static final String members = "members";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_invite_fridge_mate);
        setBackArrow();

        setTitle("A New Fridge Mate");
        id = findViewById(R.id.editText);
        Button inviteBtn = findViewById(R.id.invite_friend);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        final String email = user.getEmail();
        db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = MainActivity.userDoc;

//        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            public void onComplete(Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    final DocumentSnapshot userData = task.getResult();
//                    fridgeDoc= userData.getDocumentReference("currentFridge");
//                }
//            }
//        });
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String newcomerId = String.valueOf(id.getText());

                // check if user is yourself
                if (newcomerId.equals(email)){
                    Toast.makeText(InviteFridgeMateActivity.this, "Opps, you can't add yourself again", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // check if user is already in the fridge
                fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        List<DocumentReference> memberRefList = (List<DocumentReference>) task.getResult().get("members");
                        for(DocumentReference memberRef : memberRefList){
                            if (memberRef.getId().equals(newcomerId)){
                                Toast.makeText(InviteFridgeMateActivity.this, "Opps, you can't add this person again", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }
                        // Sanitize newcomerId. must be a valid User email
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
        });
    }

    private void addUser(final DocumentReference newOne){
        if (newOne == null) {
            Toast.makeText(InviteFridgeMateActivity.this, "No such user exist", Toast.LENGTH_SHORT).show();
            return;
        }
//        // populate member locally
//        memberListAdapter.names.add(newOne);
//        memberListAdapter.notifyDataSetChanged();

        // add user to the member list on firebase
        fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentReference> membersList = new ArrayList<>();
                    final DocumentSnapshot fridgeData = task.getResult();
                    if (fridgeData.get(members) != null) {
                        membersList.addAll(((List) Objects.requireNonNull(fridgeData.get(members))));
                    }
                        membersList.add(newOne);
                        fridgeDoc.update(members, membersList);
                }
            }
        });

        // add fridge to the member's fridge list on firebase
        newOne.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                List<DocumentReference> fridges = (List<DocumentReference>) task.getResult().get("fridges");
                assert fridges != null;
                fridges.add(fridgeDoc);
                newOne.update("fridges",fridges);
            }
        });
        Toast.makeText(getApplicationContext(), "Invited!", Toast.LENGTH_SHORT).show();
    }
}
