package com.fridgemate.yangliu.fridgemate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference
{
    static final String email= "email";
    private static final String currentFridge = "currentFridge";
    private static final String theme = "theme";

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


    private final boolean WATER = false;
    private final boolean SHAPE = true;
    public static void setTheme(Context ctx, boolean thm) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(theme,thm);
        editor.commit();
    }
    public static boolean getTheme(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(theme,false);
    }


    public static int getCurrentFridge(Context ctx)
    {
        return getSharedPreferences(ctx).getInt(currentFridge, 1);
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(email, "");
    }

    public static void clearAll(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.commit();
    }
}