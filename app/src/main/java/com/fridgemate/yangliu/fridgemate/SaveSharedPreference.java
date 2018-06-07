package com.fridgemate.yangliu.fridgemate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference
{
    static final String email= "email";
    private static final String currentFridge = "currentFridge";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(email, userName);
        editor.commit();
    }

    public static void setCurrentFridge(Context ctx, int num)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(currentFridge,num);
        editor.commit();
    }

    public static int getCurrentFridge(Context ctx)
    {
        return getSharedPreferences(ctx).getInt(currentFridge, 1);
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(email, "");
    }

    public static void clearUserName(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.commit();
    }
}