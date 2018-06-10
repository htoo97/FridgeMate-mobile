package com.fridgemate.yangliu.fridgemate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cocosw.bottomsheet.BottomSheet;
import com.fridgemate.yangliu.fridgemate.authentication.LoginActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends TitleWithButtonsActivity {


    private EditText name;
    private CircleImageView profilePhoto;
    private EditText status;
    private Button saveBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private DocumentReference userDoc;

    private boolean photoChanged = false;
    private String oldProfileUri;
    ContentValues cv;
    Uri imageUri;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // set theme
        if (SaveSharedPreference.getTheme(this) == false)
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppTheme2);
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_edit_profile);
        setBackArrow();
        setTitle("Edit Profile");

        // make keyboard push the page up
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        userDoc = MainActivity.userDoc;

        if (userDoc == null) {
            Toast.makeText(this, "User documents loading errors", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Return to login screen if cannot verify user identity
        if(user == null){
            Toast.makeText(getApplication(), R.string.error_load_data,
                    Toast.LENGTH_LONG).show();
            mAuth.signOut();

            Intent i = new Intent(EditProfile.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

//        ConstraintLayout mEditFormView = findViewById(R.id.edit_profile_form);
//        ProgressBar mProgressView = findViewById(R.id.progress);

        name = findViewById(R.id.user_name);
        status = findViewById(R.id.status);
        profilePhoto = findViewById(R.id.profile_image);
        profilePhoto.setDrawingCacheEnabled(true);

        byte[] profileBytes = getIntent().getByteArrayExtra("photo");
        if (profileBytes != null && profileBytes.length > 1){
            profilePhoto.setImageBitmap(BitmapFactory.decodeByteArray(profileBytes, 0, profileBytes.length));
        }

        TextView email = findViewById(R.id.email);
        name.setInputType(InputType.TYPE_CLASS_TEXT);

        // populate local data from the firebase
        name.setText(user.getDisplayName());

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot userData = task.getResult();
                    status.setText((CharSequence) userData.get("status"));
                    oldProfileUri = String.valueOf(userData.get("profilePhoto"));
                    if (oldProfileUri  != null && !oldProfileUri.equals("null"))
                        Glide.with(EditProfile.this).load(Uri.parse(oldProfileUri)).centerCrop()
                                .into(profilePhoto);
                }
            }
        });

        // Uri i = user.getPhotoUrl();
        // profilePhoto.setImageBitmap();
        email.setText(user.getEmail());
        saveBtn = findViewById(R.id.save_user_profile);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final Intent replyIntent = getIntent();

                saveBtn.setClickable(false);
                saveBtn.setText(R.string.button_saving);

                if (photoChanged) {
                    // add new photo to user's profile
                    Bitmap profileImg = null;
                    try {
                        profileImg = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final Uri[] newProfile = new Uri[1];
                    byte[] imgToUpload = getBitmapAsByteArray(profileImg);
                    final StorageReference ref = storage.getReference().child(Objects.requireNonNull(user.getEmail()));
                    UploadTask uploadTask = ref.putBytes(imgToUpload);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }
                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                newProfile[0] = task.getResult();
                                saveBtn.setText(R.string.button_saving);
                                // update the name, status, profile
                                String nameStr = String.valueOf(name.getText());
                                userDoc.update("status",String.valueOf(status.getText()),"name",nameStr,"profilePhoto", String.valueOf(newProfile[0]))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // delete the old photo // NO need for this online firebase handles it

                                        // update this basic stuff that will be depreciated soon
                                        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                                        builder.setDisplayName(String.valueOf(name.getText()));
                                        // Update username
                                        user.updateProfile(builder.build())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getApplication(),R.string.profile_updated,
                                                                    Toast.LENGTH_LONG).show();
                                                            profilePhoto.setDrawingCacheEnabled(false);

                                                            setResult(RESULT_OK,replyIntent);
                                                            finish();
                                                            supportFinishAfterTransition();
                                                        }
                                                    }
                                                });
                                    }
                                });


//
                            }
                        }
                    });
                }
                else {
                    // no photo updates
                    UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                    builder.setDisplayName(String.valueOf(name.getText()));
                    // Update user status, name
                    user.updateProfile(builder.build())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplication(), R.string.profile_updated,
                                                Toast.LENGTH_LONG).show();
                                        profilePhoto.setDrawingCacheEnabled(false);
                                        String statusStr = String.valueOf(status.getText());
                                        // update the status, name
                                        userDoc.update("status",  statusStr ,"name",String.valueOf(name.getText()))
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                setResult(RESULT_OK, replyIntent);
                                                finish();
                                                supportFinishAfterTransition();
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }
    public void selectPhoto(View view) {
        new BottomSheet.Builder(this).title("Options").sheet(R.menu.menu_profile_photo).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(EditProfile.this,android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfile.this,new String[]
                            {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
                }
                switch (which) {
                    case R.id.local://从相册里面取照片
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, LOAD_IMAGE_REQUEST);
                        break;
                    case R.id.camera://调用相机拍照
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                            } else if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_CAMERA_PERMISSION_CODE);
                            } else {
                                takeAPhoto();
                            }
                        }
                }
            }
        }).show();
    }

    public void takeAPhoto(){
        cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "My Picture");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.delete_account:
                // DATABASE: deactivate user account

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder.setTitle("Please enter your password again:");

                // Set up the input
                final EditText input = new EditText(EditProfile.this);
                input.setText(R.string.no_password_needed);
                LinearLayout linearLayout = new LinearLayout(EditProfile.this);
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(layoutParams);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                final String[] pw = new String[1];
                //layoutParams.gravity = Gravity.CENTER;
                linearLayout.addView(input);
                linearLayout.setPadding(40, 0, 40, 0);
                builder.setView(linearLayout);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pw[0] = input.getText().toString();

                        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()),pw[0]);
                        // it means it's a google account
                        if (credential == null)
                            credential = GoogleAuthProvider.getCredential
                                    (String.valueOf(user.getIdToken(true)), String.valueOf(mAuth.getAccessToken(true)));
                        else {
                            finish();
                            Toast.makeText(EditProfile.this, "Deactivate Failed", Toast.LENGTH_SHORT).show();
                        }
                        // Get auth credentials from the user for re-authentication. The example below shows
                        // email and password credentials but there are multiple possible providers,
                        // such as GoogleAuthProvider or FacebookAuthProvider.
                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot userData = task.getResult();

                                                // delete user's profile image
                                                if (userData.get("profilePhoto") != null) {
                                                    String profilelUri = (String) Objects.requireNonNull(userData.get("profilePhoto"));
                                                    if (profilelUri != null && !profilelUri.equals("") && !profilelUri.equals("null"))
                                                        storage.getReferenceFromUrl(profilelUri).delete();
                                                }

                                                // delete (or update) user's fridges
                                                List<DocumentReference> fridges = (List) userData.get("fridges");
                                                assert fridges != null;
                                                for(final DocumentReference ref : fridges){
                                                    ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        public void onComplete(Task<DocumentSnapshot> task) {
                                                            if(task.isSuccessful()) {
                                                                DocumentSnapshot fridgeData = task.getResult();
                                                                List<DocumentReference> membersList = (List<DocumentReference>) fridgeData.get("members");
                                                                assert membersList != null;
                                                                for (DocumentReference member : membersList){
                                                                    if (member == userDoc)
                                                                        membersList.remove(member);
                                                                }

                                                                //delete the fridge if the user is the last one
                                                                if (membersList.size() == 0){
                                                                    // delete items' images in Storage
                                                                    ref.collection("FridgeItems").get().addOnCompleteListener(
                                                                            new OnCompleteListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                    List<DocumentSnapshot> itemSnapshots= task.getResult().getDocuments();
                                                                                    for (DocumentSnapshot each: itemSnapshots){
                                                                                        String uri = (String) each.get("imageID");
                                                                                        if (uri != null && !uri.equals("") && !uri.equals("null")){
                                                                                            storage.getReferenceFromUrl(uri).delete();
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                    );
                                                                    // delete all info in this fridge
                                                                    ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Toast.makeText(EditProfile.this, "ALl data have been deleted", Toast.LENGTH_LONG).show();
                                                                            Intent intent = new Intent(EditProfile.this,LoginActivity.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    });
                                                                }
                                                                else{
                                                                    ref.update("members",membersList);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }

                                            }
                                        });
//                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    Toast.makeText(EditProfile.this, "ALl data have been deleted", Toast.LENGTH_LONG).show();
//                                                    Intent intent = new Intent(EditProfile.this,LoginActivity.class);
//                                                    startActivity(intent);
//                                                }
//                                            }
//                                        });

                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
                builder.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Bitmap head;
    private static final int CAMERA_REQUEST = 1888;
    private static final int LOAD_IMAGE_REQUEST = 1889;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        photoChanged = true;
        switch  (requestCode) {
            //从相册里面取相片的返回结果
            case LOAD_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    imageUri = data.getData();
                    Glide.with(this).load(imageUri).centerCrop()
                            .into(profilePhoto);
                }
                break;

            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    Glide.with(this).load(imageUri).centerCrop()
                            .into(profilePhoto);
                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "You have not selected and image", Toast.LENGTH_SHORT).show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public byte[] getBitmapAsByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        Log.d("Img size: " ,String.valueOf(outputStream.size()/1024) + "kb");
        return outputStream.toByteArray();
    }

    // Return to previous screen on back button
    @Override
    public boolean onSupportNavigateUp(){
        finish();

        return true;
    }
}