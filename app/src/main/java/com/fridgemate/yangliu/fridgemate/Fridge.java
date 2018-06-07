package com.fridgemate.yangliu.fridgemate;

import android.support.annotation.NonNull;

public class Fridge {

    private String fridgeid;

    private String fridgeName;

    public Fridge() {

    }

    // Getters and setters
    // NonNull: -> return value can never be null.
    public Fridge(@NonNull String fridgeid, String name) {
        this.fridgeid = fridgeid;
        this.fridgeName = name;
    }

    public String getFridgeid() {
        return fridgeid;
    }

    public String getFridgeName() {
        return fridgeName;
    }
}