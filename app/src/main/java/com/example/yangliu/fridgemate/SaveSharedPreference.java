package com.example.yangliu.fridgemate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference
{
    static final String email= "email";
    private static final String SHOP_LIST_TEXT_KEY = "shoplisttext";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(email, userName);
        editor.commit();
    }

    public static void setShopList(Context ctx, String shopList)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(SHOP_LIST_TEXT_KEY, shopList);
        editor.commit();
    }

    public static String getShopList(Context ctx)
    {
        return getSharedPreferences(ctx).getString(SHOP_LIST_TEXT_KEY, "");
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