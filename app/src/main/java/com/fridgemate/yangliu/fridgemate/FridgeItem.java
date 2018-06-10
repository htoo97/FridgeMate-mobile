package com.fridgemate.yangliu.fridgemate;

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
        this.docRef = ref;
    }


    public FridgeItem(@NonNull FridgeItem item) {
        this.itemName = item.getItemName();
        this.expDate = item.getExpDate();
        this.image = item.getImage();
        this.itemId= item.getItemId();
        this.docRef = item.getDocRef();
    }

    public int getItemId() {
        return itemId;
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

    public String getDocRef() {
        return docRef;
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
            else {
                // order alphabetically if dates are the same
                int comparison = itemName.compareTo(o.itemName);
                if (comparison > 0)
                    return 1;
                else if (comparison < 0)
                    return -1;
                return 0;
            }
        }
        else{
            if (expDate == "" && o.expDate == "") {
                int comparison = itemName.compareTo(o.itemName);
                if (comparison > 0)
                    return 1;
                else if (comparison < 0)
                    return -1;
                return 0;
            }
            if (expDate == "")
                return 1;
            else
                return -1;
        }
    }



}