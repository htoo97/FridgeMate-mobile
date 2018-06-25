package com.fridgemate.yangliu.fridgemate.current_contents;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fridgemate.yangliu.fridgemate.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.fridgemate.yangliu.fridgemate.TitleWithButtonsActivity;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddItemManual extends TitleWithButtonsActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;

    private EditText mEditNameView;
    private EditText mEditDate;
    private ImageView itemProfile;
    private ProgressBar progressBar;
    private EditText amount;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private DocumentReference  userDoc;
    private DocumentReference fridgeDoc;
    private ImageButton mRotateImg;

    private Bitmap image; // indicator of a new image added
    private String oldImageUri; // indicator of there exist an old image

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_add_item_manual);
        setBackArrow();
        setTitle("Edit Item Info");

        // amount attribute
        ImageButton incQuantity = findViewById(R.id.ibn_add1);
        ImageButton decQuantity = findViewById(R.id.ibn_del1);
        amount = findViewById(R.id.et_content2);
        incQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(amount.getText().toString());
                temp += 1;
                if (temp>99)
                    amount.setText("99");
                else
                    amount.setText(String.valueOf(temp));
            }
        });
        decQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(amount.getText().toString());
                if (temp!=1) {
                    temp -= 1;
                }
                else {
                    temp = 1;
                }
                amount.setText(String.valueOf(temp));
            }
        });

        itemProfile = findViewById(R.id.item_image);
        mEditDate = findViewById(R.id.edit_date);
        mEditNameView = findViewById(R.id.edit_word);
        mEditNameView.requestFocus();
        progressBar = findViewById(R.id.item_progress_bar);
        mRotateImg = findViewById(R.id.rotateImg);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        image = null;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userDoc = db.collection("Users").document(user.getEmail());
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot userData = task.getResult();
                    fridgeDoc = userData.getDocumentReference("currentFridge");
                }
            }
        });

        itemProfile.setDrawingCacheEnabled(true);

        // DATABASE receive item info by item id
        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        String expDate = null;
        int num;
        if (extras != null) {
            mEditNameView.setText(extras.getString("name"));
            expDate = extras.getString("expDate");
            if (expDate != null && expDate.length() == 10) {
                mEditDate.setText(expDate);
                updateProgressBar(expDate);

            }
            num = Integer.parseInt(extras.getString("amount"));
            if (num > 0)
                amount.setText(String.valueOf(num));

            oldImageUri = extras.getString("image");
            if (oldImageUri != null && !oldImageUri.equals("") && !oldImageUri.equals("null")) {
                Glide.with(this).load(Uri.parse(oldImageUri)).centerCrop().into(itemProfile);
            }
        }

        // date picker
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        final Calendar myCalendar = Calendar.getInstance();
        if (expDate != null && expDate.length() == 10){
            try {
                myCalendar.setTime(sdf.parse(expDate));
            } catch (ParseException e) {
                e.printStackTrace();
                myCalendar.setTime(Calendar.getInstance().getTime());
            }
        }


        // rotate image button
        mRotateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((oldImageUri != null && !oldImageUri.equals("") && !oldImageUri.equals("null")) || image != null) {
                    RotateAnimation rotate = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotate.setDuration(300);
                    rotate.setInterpolator(new LinearInterpolator());
                    itemProfile.startAnimation(rotate);
                    // image changed

                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            itemProfile.setRotation(itemProfile.getRotation()+ 90);
                            image = itemProfile.getDrawingCache();
                        }
                    }, 320);

                }
            }
        });

        // set up camera button
        itemProfile.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.CAMERA},
                                        MY_CAMERA_PERMISSION_CODE);
                                if((checkSelfPermission(Manifest.permission.CAMERA)
                                        == PackageManager.PERMISSION_GRANTED)){
                                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                                }
                            } else {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            }
                        }
                    }
                }

        );

        // set up save button
        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent replyIntent = getIntent();
                // only pressing button once
                button.setClickable(false);
                if (TextUtils.isEmpty(mEditNameView.getText())) {
                    Toast.makeText(AddItemManual.this, R.string.item_unnamed, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED, replyIntent);
                    finish();
                    // animation
                    supportFinishAfterTransition();
                }
                else {
                    button.setText(R.string.button_saving);
                    setResult(RESULT_OK, replyIntent);
                    userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
//                                final DocumentSnapshot userData = task.getResult();

                                final Map<String, Object> itemData = new HashMap<>();
                                String name = mEditNameView.getText().toString();
                                // capitalize name
                                itemData.put("itemName", name.substring(0,1).toUpperCase() + name.substring(1));
                                itemData.put("expirationDate", mEditDate.getText().toString());
                                SimpleDateFormat mdyFormat = new SimpleDateFormat("MM/dd/yyyy");
                                itemData.put("lastModifiedDate", mdyFormat.format(myCalendar.getTime()));
                                itemData.put("purchaseDate", mdyFormat.format(myCalendar.getTime()));
                                itemData.put("lastModifiedBy", userDoc);
                                itemData.put("amount", String.valueOf(amount.getText()));

                                // ************** if it has new profile image **********************
                                if (image != null) {

                                    // if image rotatedr
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(itemProfile.getRotation());
                                    Bitmap toRotateBitmap = itemProfile.getDrawingCache();
                                    byte[] imgToUpload = getBitmapAsByteArray(Bitmap.createBitmap(
                                            toRotateBitmap, 0, 0, toRotateBitmap.getWidth(), toRotateBitmap.getHeight(), matrix, true));
                                    // TODO:: this is may lose some consistency since fridgeItem collection doesn't exist
                                    String imageName = db.collection("fridgeItems").document().getId();
                                    final StorageReference ref = storage.getReference().child(imageName);
                                    UploadTask uploadTask = ref.putBytes(imgToUpload);

                                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if (!task.isSuccessful()) {
                                                throw new Error();
                                            }

                                            // Continue with the task to get the download URL
                                            return ref.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                Uri downloadUri = task.getResult();
                                                button.setText(R.string.button_saving);
                                                itemData.put("imageID", downloadUri.toString());
                                                // ************** if we are editing an item **********************
                                                if (extras != null){
                                                    // delete the old image on database if there's any
                                                    if (oldImageUri != null && !oldImageUri.equals("") && !oldImageUri.equals("null")) {
                                                        storage.getReferenceFromUrl(oldImageUri).delete();
                                                        oldImageUri = String.valueOf(downloadUri);
                                                    }
                                                    // update the fields
                                                    String docRef = extras.getString("docRef");
                                                    if (docRef != null && !docRef.equals("")){
                                                        DocumentReference itemDoc = fridgeDoc.collection("FridgeItems").document(docRef);
                                                        itemDoc.update(itemData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                finish();
                                                                supportFinishAfterTransition();
                                                            }
                                                        });
                                                    }
                                                }
                                                // ************** if we are adding an item **********************
                                                else {
                                                    fridgeDoc.collection("FridgeItems")
                                                            .add(itemData).addOnCompleteListener(
                                                            new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    finish();
                                                                }
                                                            }
                                                    );
                                                }

                                            }
                                        }
                                    });
                                }
                                // ************** if it doesn't have a new profile image **********************
                                else {
                                    // ************** if we are editing an item **********************
                                    if (extras != null) {
                                        itemData.put("imageID", oldImageUri);
                                        String docRef = extras.getString("docRef");
                                        if (docRef != null && !docRef.equals("")) {
                                            DocumentReference itemDoc = fridgeDoc.collection("FridgeItems").document(docRef);
                                            itemDoc.set(itemData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    finish();
                                                    supportFinishAfterTransition();
                                                }
                                            });
                                        }
                                    }
                                    // ************** if we are adding an item **********************
                                    else {
                                        itemData.put("imageID", "");
                                        fridgeDoc.collection("FridgeItems")
                                                .add(itemData)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });

                }
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String expDateStr = sdf.format(myCalendar.getTime());
                mEditDate.setText(expDateStr);
                updateProgressBar(expDateStr);
            }

        };
        mEditDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddItemManual.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateProgressBar(String expDate){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date strDate = null;
        if (expDate.length() != 0) {
            try {
                strDate = sdf.parse(expDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (strDate != null) {
            int dayDiff = (int) (strDate.getTime() / (86400000)) - (int) (System.currentTimeMillis() / (86400000));
            if (dayDiff < 0)
                progressBar.setProgress(0);
            else
                progressBar.setProgress((int)(dayDiff*7.2));
        }
    }

    // camera functions
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bundle photoCache = data.getExtras();
            if (photoCache != null){
            Bitmap photo = (Bitmap)photoCache.get("data");
            byte[] ba = getBitmapAsByteArray(photo);
            photo = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            //itemProfile.setImageBitmap(photo);
            Glide.with(AddItemManual.this).load(ba).asBitmap().centerCrop().into(itemProfile);
            image = photo;
            mRotateImg.setVisibility(View.VISIBLE);
            }

        }
    }

    // this is the image compression method
    public byte[] getBitmapAsByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        Log.d("Img size: " ,String.valueOf(outputStream.size()/1024) + "kb");
        return outputStream.toByteArray();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

}