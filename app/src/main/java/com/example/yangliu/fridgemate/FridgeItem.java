package com.example.yangliu.fridgemate;

import android.support.annotation.NonNull;

public class FridgeItem {
    private int itemId;

    private String itemName;

    private String expDate;

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