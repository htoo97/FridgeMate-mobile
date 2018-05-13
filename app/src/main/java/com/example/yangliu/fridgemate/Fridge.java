package com.example.yangliu.fridgemate;

import android.support.annotation.NonNull;

public class Fridge {

    private int fridgeid;

    private String fridgeName;

    private String memberList;

    public Fridge() {

    }

    // Getters and setters
    // NonNull: -> return value can never be null.
    public Fridge(@NonNull int fridgeid, String name, String memberList) {
        this.fridgeName = name;
        this.fridgeid = fridgeid;
        this.memberList = memberList;
    }

    public int getFridgeid() {
        return fridgeid;
    }

    public void setFridgeid(int itemId) {
        this.fridgeid = itemId;
    }

    public String getFridgeName() {
        return fridgeName;
    }

    public void setFridgeName(@NonNull String fridgeName) {
        this.fridgeName = fridgeName;
    }

    public String getMemberList() {
        return memberList;
    }

    public void setMemberList(String memberList) {
        this.memberList = memberList;
    }
}