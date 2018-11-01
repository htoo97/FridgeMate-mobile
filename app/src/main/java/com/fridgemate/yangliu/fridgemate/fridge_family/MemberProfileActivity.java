package com.fridgemate.yangliu.fridgemate.fridge_family;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fridgemate.yangliu.fridgemate.BlurTransformation;
import com.fridgemate.yangliu.fridgemate.R;
import com.fridgemate.yangliu.fridgemate.TitleWithButtonsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberProfileActivity extends TitleWithButtonsActivity {

    private TextView name;
    private TextView status;
    private CircleImageView profilePhoto;
    private TextView email;
    private TextView currentFridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_member_profile);
        setBackArrow();
        setTitle("Profile");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String memberId = getIntent().getStringExtra("memberId");
        email = findViewById(R.id.email);

        // set up profile photo by cache
        profilePhoto = findViewById(R.id.profile_image);
        final byte[] profileBytes =  getIntent().getByteArrayExtra("photo");
        if (profileBytes != null && profileBytes.length > 1){
            profilePhoto.setImageBitmap(BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length));
        }else {
            profilePhoto.setImageDrawable(getResources().getDrawable(R.drawable.profile));
        }
        profilePhoto.setClickable(false);
        status = findViewById(R.id.status);
        name = findViewById(R.id.user_name);
        currentFridge = findViewById(R.id.current_fridge);
        currentFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Friend's Fridge ID", currentFridge.getText());
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MemberProfileActivity.this, "Fridge ID copied.", Toast.LENGTH_SHORT).show();
            }
        });

        DocumentReference memberDoc = db.collection("Users").document(memberId);

        final ProgressBar mImgLoadProgress = findViewById(R.id.progressBar2);

        // image background
        final ImageView memberProfileBackGround = findViewById(R.id.background);

        memberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot memberData = task.getResult();
                String emailStr = String.valueOf(memberData.get("email"));
                if (emailStr.equals("null")) {
                    Toast.makeText(MemberProfileActivity.this, "No such user exist", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mImgLoadProgress.setVisibility(View.VISIBLE);
                email.setText(emailStr);
                final String imgUri = String.valueOf(memberData.get("profilePhoto"));
                profilePhoto.setDrawingCacheEnabled(true);
                if (imgUri != null && !imgUri.equals("null") && !imgUri.equals("")) {
                    Glide.with(MemberProfileActivity.this).load(Uri.parse(imgUri)).listener(
                            new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    mImgLoadProgress.setVisibility(View.GONE);
                                    // set up a resonating background
                                    Glide.with(MemberProfileActivity.this)
                                            .load(Uri.parse(imgUri))
                                            .transform(new BlurTransformation(getApplicationContext()))
                                            .into(memberProfileBackGround);
                                    // add another color layer with dominant color
                                    Bitmap newBitmap = Bitmap.createScaledBitmap(profilePhoto.getDrawingCache(), 1, 1, true);
                                    final int color = newBitmap.getPixel(newBitmap.getWidth()/2, newBitmap.getWidth()/2);
                                    newBitmap.recycle();
                                    ImageView colorLayer = findViewById(R.id.color_layer);
                                    colorLayer.setBackgroundColor(color);
                                    return false;
                                }
                            }
                    ).centerCrop().into(profilePhoto);


                }
                else{
                    // default image background
                    memberProfileBackGround.setImageResource(R.drawable.down2);
                    mImgLoadProgress.setVisibility(View.GONE);
                }
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
