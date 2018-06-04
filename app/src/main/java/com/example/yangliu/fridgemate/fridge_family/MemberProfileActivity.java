package com.example.yangliu.fridgemate.fridge_family;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yangliu.fridgemate.R;
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberProfileActivity extends TitleWithButtonsActivity {

    private ConstraintLayout mEditFormView;

    private TextView name;
    private TextView status;
    private CircleImageView profilePhoto;
    private TextView email;
    private TextView currentFridge;


    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_member_profile);
        setBackArrow();
        setTitle("Profile");

        db = FirebaseFirestore.getInstance();

        String memberId = getIntent().getStringExtra("memberId");

        email = findViewById(R.id.email);
        profilePhoto = findViewById(R.id.profile_image);
        profilePhoto.setClickable(false);
        status = findViewById(R.id.status);
        name = findViewById(R.id.user_name);
        currentFridge = findViewById(R.id.current_fridge);
        currentFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Friend's Fridge ID", currentFridge.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MemberProfileActivity.this, name.getText() + "'s current Fridge ID copied.", Toast.LENGTH_SHORT).show();
            }
        });

        DocumentReference memberDoc = db.collection("Users").document(memberId);

        memberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot memberData = task.getResult();
                String emailStr = String.valueOf(memberData.get("email"));
                email.setText(emailStr);
                String imgUri = String.valueOf(memberData.get("profilePhoto"));
                if (imgUri != null && !imgUri.equals("null") && !imgUri.equals(""))
                    Glide.with(MemberProfileActivity.this).load(Uri.parse(imgUri)).centerCrop().into(profilePhoto);
                String statusMessage = String.valueOf(memberData.get("status"));
                if (!statusMessage.equals("null") && !statusMessage.equals(""))
                    status.setText(statusMessage);
                String userName = String.valueOf(memberData.get("name"));
                if (!userName.equals("null") && !userName.equals(""))
                    name.setText(userName);
                else{
                    name.setText(emailStr.substring(0,emailStr.indexOf("@")));
                }
                DocumentReference d = (DocumentReference) memberData.get("currentFridge");
                if (d != null) {
                    String currentFridgeID = String.valueOf(d.getId());
                    currentFridge.setText(currentFridgeID);
                }

            }
        });


    }

}
