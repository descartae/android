package org.descartae.android.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class DescartaePreferences {

    public static final String TAG = DescartaePreferences.class.getSimpleName();

    private static SharedPreferences mSharedPreferences;
    private static DescartaePreferences instance;

    public Gson gson;

    public final static String INTRO_OK = "intro_ok";
    public static final String PREF_LAST_LOCATION_LAT = "PREF_LAST_LOCATION_LAT";
    public static final String PREF_LAST_LOCATION_LNG = "PREF_LAST_LOCATION_LNG";

    public DescartaePreferences(Activity context) {
        mSharedPreferences = context.getPreferences(Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static DescartaePreferences getInstance(Activity context) {
        if (instance == null) {
            instance = new DescartaePreferences(context);
        }
        return instance;
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setValue(String key, double value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(key, (float) value);
        editor.commit();
        editor.apply();
    }

    public Double getDoubleValue(String key) {
        return (double) mSharedPreferences.getFloat(key, 0F);
    }
}