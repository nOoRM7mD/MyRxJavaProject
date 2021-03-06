package com.example.lenovo.myrxjavaproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class stores and retrieves the API Key
 * that needs to be sent in every HTTP call as Authorization header field.
 */

public class PrefUtil {
    public PrefUtil() {
    }

    /**
     * Storing API Key in shared preferences to
     * add it in header part of every retrofit request
     */

    private String apiKey;

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
    }

    public static void storeApiKey(Context context, String apiKey) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("API_KEY", apiKey);
        editor.commit();
    }

    public static String getApiKey(Context context) {
        return getSharedPreferences(context).getString("API_KEY", null);
    }
}
