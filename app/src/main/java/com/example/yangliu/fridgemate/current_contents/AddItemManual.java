package com.example.yangliu.fridgemate.current_contents;

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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yangliu.fridgemate.FridgeItem;
import com.example.yangliu.fridgemate.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.example.yangliu.fridgemate.TitleWithButtonsActivity;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddItemManual extends TitleWithButtonsActivity {

    public static final String NAME_KEY = "name";
    public static final String DATE_KEY = "date";
    public static final String IMAGE_KEY = "img";
    public static final String ITEM_ID = "item_id";
    public static final String Other_KEY = "other";

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST = 1888;

    private EditText mEditNameView;
    private EditText mEditDate;
    private ImageButton mCameraButton;
    private ImageView itemProfile;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_add_item_manual);
        setBackArrow();
        setTitle("Add Item");

        mCameraButton = (ImageButton) findViewById(R.id.cameraButton);
        itemProfile = (ImageView) findViewById(R.id.imageView);
        mEditDate = (EditText) findViewById(R.id.edit_date);
        mEditNameView = findViewById(R.id.edit_word);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        final Calendar myCalendar = Calendar.getInstance();

        itemProfile.setDrawingCacheEnabled(true);

        // TODO:: DATABASE receive item info by item id
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String itemId = null;
        if (extras != null) {
            itemId = getIntent().getExtras().getString(ITEM_ID);
        }

        final String email = user.getEmail();
        final DocumentReference userDoc = db.collection("Users").document(email);

        // TODO:: DATABASE receive item info by item id
//        if(item has a name) {
//            mEditNameView.setText(item's name);
//        }
//        if(item has an exp date) {
//            mEditDate.setText(exp date);
//        }
//        if(item has an image)) {
//            // I stored images as byte arrays, but it depends on your choice of type
//            byte[] imgByte = extras.getByteArray(IMAGE_KEY);
//            if (imgByte != null) {
//                Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
//                itemProfile.setImageBitmap(photo);
//            }
//        }

        // set up camera button
        mCameraButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.CAMERA},
                                        MY_CAMERA_PERMISSION_CODE);
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
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mEditNameView.getText())) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {

                    Bundle extras = new Bundle();
                    extras.putString(NAME_KEY,mEditNameView.getText().toString());
                    Log.d("passing","passing "+mEditDate.getText().toString()+" to the bundle");
                    extras.putString(DATE_KEY,mEditDate.getText().toString());

                    // downcast the image
                    final Bitmap image = Bitmap.createScaledBitmap(itemProfile.getDrawingCache(),
                            itemProfile.getWidth(),itemProfile.getHeight(), true);
                    extras.putByteArray(IMAGE_KEY, getBitmapAsByteArray(image));

                    itemProfile.setDrawingCacheEnabled(false);
                    replyIntent.putExtras(extras);
                    setResult(RESULT_OK, replyIntent);
                    //startActivityForResult(replyIntent,NEW_ITEM_ACTIVITY_REQUEST_CODE);

                    userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                final DocumentSnapshot userData = task.getResult();

                                byte[] imgToUpload = getBitmapAsByteArray(image);
                                final StorageReference ref = storage.getReference().child("testUpload.jpg");
                                UploadTask uploadTask = ref.putBytes(imgToUpload);
                                final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return ref.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();

                                            Map<String, Object> itemData = new HashMap<>();
                                            itemData.put("itemName", mEditNameView.getText().toString());
                                            itemData.put("expirationDate", mEditDate.getText().toString());
                                            SimpleDateFormat mdyFormat = new SimpleDateFormat("MM/dd/yyyy");
                                            itemData.put("lastModifiedDate", mdyFormat.format(myCalendar.getTime()).toString());
                                            itemData.put("purchaseDate", mdyFormat.format(myCalendar.getTime()).toString());
                                            itemData.put("lastModifiedBy", userDoc);
                                            itemData.put("imageID", downloadUri.toString());
                                            itemData.put("fridge", userData.get("currentFridge"));

                                            db.collection("FridgeItems")
                                                    .add(itemData)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            // TODO: update the list of items
                                                        }
                                                    });
                                        } else {
                                            // Handle failures
                                            // ...
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
                finish();
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Expiration date Auto-generated method
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                mEditDate.setText(sdf.format(myCalendar.getTime()));
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

    // camera functions
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // TODO:: may need to be deleted
            Matrix matrix = new Matrix();
            // clockwise by 90 degrees becaues some weird issue on emulator
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(photo  , 0, 0, photo.getWidth(), photo  .getHeight(), matrix, true);
            itemProfile.setImageBitmap(rotatedBitmap);
            //Glide.with(AddItemManual.this).load(data.getExtras().get("data")).centerCrop().into(itemProfile);

        }
    }

    // TODO:: DATABASE: this is the image compression method you can make the return value the image type you chose
    public byte[] getBitmapAsByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream);
        Log.d("Img size: " ,String.valueOf(outputStream.size()/1024) + "kb");

        return outputStream.toByteArray();
    }

}