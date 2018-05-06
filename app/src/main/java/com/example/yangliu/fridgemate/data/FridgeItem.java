package com.example.yangliu.fridgemate.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "fridge")
public class FridgeItem {


    @ColumnInfo(name = "item_id")
    private int itemId;
    //TODO:: assigning ids

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "item_name")
    private String itemName;


    @ColumnInfo(name = "exp_date")
    private String expDate;


    @ColumnInfo(name = "image")
    private byte[] image;

    public FridgeItem() {

    }

    // Getters and setters
    // NonNull: -> return value can never be null.
    public FridgeItem(@NonNull String name, String expDate, byte[] image) {
        this.itemName = name;
        this.expDate = expDate;
        this.image = image;
    }

    public FridgeItem(@NonNull String name, String expDate) {
        this.itemName = name;
        this.expDate = expDate;
    }

    public FridgeItem(@NonNull String name) {
        this.itemName = name;
        this.expDate = "";
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public byte[] getImage() { return image; }

    public void setImage(byte[] imageByteArr) {
        this.image = imageByteArr;
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
}