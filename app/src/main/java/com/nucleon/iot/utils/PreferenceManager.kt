package com.nucleon.iot.utils

import android.content.Context
import android.content.SharedPreferences
import com.nucleon.iot.R

object PreferenceManager {

    private const val PREF_NAME = "nucleon_preferences"
    private const val KEY_THEME = "theme"
    private const val KEY_LANGUAGE = "language"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getTheme(): Int {
        val isDarkMode = preferences.getBoolean(KEY_THEME, false)
        return if (isDarkMode) R.style.Theme_Nucleon_Dark else R.style.Theme_Nucleon
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_THEME, enabled).apply()
    }

    fun getLanguage(): String {
        return preferences.getString(KEY_LANGUAGE, "id") ?: "id"
    }

    fun setLanguage(language: String) {
        preferences.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun clearUserData() {
        preferences.edit().clear().apply()
    }
}
