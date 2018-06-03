package com.example.yangliu.fridgemate;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FridgeItem  implements Comparable<FridgeItem>{
    private int itemId;

    private String itemName;

    private String expDate;

    private String docRef;

    private Uri image;

    public FridgeItem() {
    }

    // Getters and setters
    // NonNull: -> return value can never be null.
    public FridgeItem(@NonNull String name, String expDate, Uri image, String ref){
        this.itemName = name;
        this.expDate = expDate;
        this.image = image;
        docRef = ref;
    }

    public FridgeItem(@NonNull String name, String expDate) {
        this.itemName = name;
        this.expDate = expDate;
    }

    public FridgeItem(@NonNull String name) {
        this.itemName = name;
        this.expDate = "";
    }

    public FridgeItem(@NonNull FridgeItem item) {
        this.itemName = item.getItemName();
        this.expDate = item.getExpDate();
        this.image = item.getImage();
        this.itemId= item.getItemId();
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Uri getImage() { return image; }

    public void setImage(Uri imageUri) {
        this.image = imageUri;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    @Override
    public int compareTo(@NonNull FridgeItem o) {

        if (expDate.length() !=0 && o.expDate.length() != 0 ){
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
            Date thisDate = null, dateCompareTo = null;
            try {
                thisDate = sdf.parse(expDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                dateCompareTo = sdf.parse(o.expDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (thisDate.after(dateCompareTo))
                return 1;
            if (dateCompareTo.after(thisDate))
                return -1;
            else
                return 0;
        }
        else{
            if (expDate == "" && o.expDate == "")
                return 0;
            if (expDate == "")
                return 1;
            else
                return -1;

        }
    }



    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }
}