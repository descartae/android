package org.descartae.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class DescartaePreferences {

    public static final String TAG = DescartaePreferences.class.getSimpleName();

    private static SharedPreferences mSharedPreferences;
    private static DescartaePreferences instance;

    public Gson gson;

    public final static String INTRO_OK = "intro_ok";

    public DescartaePreferences(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();

    }

    public static DescartaePreferences getInstance(Context context) {
        if (instance == null) {
            instance = new DescartaePreferences(context);
        }
        return instance;
    }

    public void setStringValue(String key, String value) {
        mSharedPreferences.edit()
                .putString(key, value)
                .apply();
    }

    public String getStringValue(String key) {
        return mSharedPreferences.getString(key, null);
    }

    public void setBooleanValue(String key, boolean value) {
        mSharedPreferences.edit()
                .putBoolean(key, value)
                .apply();
    }

    public boolean getBooleanValue(String key) {
        return mSharedPreferences.getBoolean(key, false);
    }
}