package com.example.yangliu.fridgemate;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddItemManual extends AppCompatActivity {

    public static final String NAME_KEY = "name";
    public static final String DATE_KEY = "date";
    public static final String IMAGE_KEY = "img";
    public static final String Other_KEY = "other";

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int NEW_ITEM_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST = 1888;

    private EditText mEditNameView;
    private EditText mEditDate;
    private ImageButton mCameraButton;
    private ImageView itemProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_manual);


        mCameraButton = (ImageButton) findViewById(R.id.cameraButton);
        itemProfile = (ImageView) findViewById(R.id.imageView);
        mEditDate = (EditText) findViewById(R.id.edit_date);
        mEditNameView = findViewById(R.id.edit_word);

        itemProfile.setDrawingCacheEnabled(true);

        // pull the available data (if it is modifying item)
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(intent.hasExtra(NAME_KEY)) {
            Log.d("extra test", extras.getString(NAME_KEY));
            mEditNameView.setText(extras.getString(NAME_KEY));
        }
        if(intent.hasExtra(DATE_KEY)) {
            mEditDate.setText(extras.getString(DATE_KEY));
        }
        if(intent.hasExtra(IMAGE_KEY)) {
            // store directly as bitmap or decode everytime
            byte[] imgByte = extras.getByteArray(IMAGE_KEY);
            if (imgByte != null) {
                Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                itemProfile.setImageBitmap(photo);
            }
        }

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

                    // TODO:: pass item data
                    Bundle extras = new Bundle();
                    extras.putString(NAME_KEY,mEditNameView.getText().toString());
                    Log.d("passing","passing "+mEditDate.getText().toString()+" to the bundle");
                    extras.putString(DATE_KEY,mEditDate.getText().toString());
                    // downcast the image
                    Bitmap image = Bitmap.createScaledBitmap(itemProfile.getDrawingCache(),
                            itemProfile.getWidth(),itemProfile.getHeight(), true);
                    extras.putByteArray(IMAGE_KEY, getBitmapAsByteArray(image));
                    itemProfile.setDrawingCacheEnabled(false);
                    replyIntent.putExtras(extras);
                    setResult(RESULT_OK, replyIntent);
                    //startActivityForResult(replyIntent,NEW_ITEM_ACTIVITY_REQUEST_CODE);
                }
                finish();
            }
        });

        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                mEditDate.setText(sdf.format(myCalendar.getTime()));
            }

        };
        mEditDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
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
            // rotate it due to some hardware issue
            Matrix matrix = new Matrix();
            matrix.postRotate(90); // clockwise by 90 degrees
            Bitmap rotatedBitmap = Bitmap.createBitmap(photo  , 0, 0, photo.getWidth(), photo  .getHeight(), matrix, true);
            itemProfile.setImageBitmap(rotatedBitmap);
            //Glide.with(AddItemManual.this).load(data.getExtras().get("data")).centerCrop().into(itemProfile);

        }
    }
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }


}