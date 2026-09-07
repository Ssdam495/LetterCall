package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

object AppPreferences {
    private const val PREFS_NAME = "letter_call_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_SERVER_IP = "server_ip"
    private const val KEY_IS_CONFIGURED = "is_configured"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiKey(context: Context): String {
        val saved = getPrefs(context).getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (saved.isNotBlank()) return saved
        // Fallback to BuildConfig if provided at build time
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getServerIp(context: Context): String {
        return getPrefs(context).getString(KEY_SERVER_IP, "") ?: ""
    }

    fun isConfigured(context: Context): Boolean {
        val hasExplicitConfig = getPrefs(context).getBoolean(KEY_IS_CONFIGURED, false)
        if (hasExplicitConfig) return true
        // If an API key is available through BuildConfig, auto-mark configured
        return getApiKey(context).isNotBlank()
    }

    fun saveSettings(context: Context, apiKey: String, serverIp: String = "") {
        getPrefs(context).edit()
            .putString(KEY_GEMINI_API_KEY, apiKey.trim())
            .putString(KEY_SERVER_IP, serverIp.trim())
            .putBoolean(KEY_IS_CONFIGURED, true)
            .apply()
    }
}
