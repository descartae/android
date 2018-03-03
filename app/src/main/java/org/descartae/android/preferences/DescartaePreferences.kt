package org.descartae.android.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class DescartaePreferences(context: Context) {

    private var mSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setBooleanValue(key: String, value: Boolean) {
        val editor = mSharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBooleanValue(key: String): Boolean {
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setValue(key: String, value: Double) {
        val editor = mSharedPreferences.edit()
        editor.putFloat(key, value.toFloat())
        editor.apply()
    }

    fun getDoubleValue(key: String): Double? {
        return mSharedPreferences.getFloat(key, 0f).toDouble()
    }

    companion object {
        const val INTRO_OK = "intro_ok"
        const val PREF_LAST_LOCATION_LAT = "PREF_LAST_LOCATION_LAT"
        const val PREF_LAST_LOCATION_LNG = "PREF_LAST_LOCATION_LNG"
    }
}