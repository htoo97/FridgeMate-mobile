package com.example.yangliu.fridgemate.fridge_family;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.example.yangliu.fridgemate.SaveSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
                fridgeDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> membersList = new ArrayList<>();
                            final DocumentSnapshot fridgeData = task.getResult();
                            if (fridgeData.get(members) != null) {
                                membersList.addAll(((List) fridgeData.get(members)));
                            }
                            membersList.add(newcomerId);
                            fridgeDoc.update(members, membersList);
                            Toast.makeText(v.getContext(), "Invited " + newcomerId + " to your fridge!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }

}
