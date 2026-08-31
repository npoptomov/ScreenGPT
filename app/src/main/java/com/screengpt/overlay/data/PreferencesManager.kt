package com.screengpt.overlay.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for devices/emulators with custom keystore implementations
            context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        }
    }

    var appMode: String
        get() = prefs.getString(KEY_APP_MODE, MODE_QUICK_BRIDGE) ?: MODE_QUICK_BRIDGE
        set(value) = prefs.edit().putString(KEY_APP_MODE, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var selectedModel: String
        get() = prefs.getString(KEY_MODEL, MODEL_GPT_4O_MINI) ?: MODEL_GPT_4O_MINI
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var extractTextWithOcr: Boolean
        get() = prefs.getBoolean(KEY_OCR_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_OCR_ENABLED, value).apply()

    fun isQuickBridgeMode(): Boolean = appMode == MODE_QUICK_BRIDGE

    fun hasValidApiKey(): Boolean = apiKey.isNotBlank() && apiKey.startsWith("sk-")

    companion object {
        private const val PREFS_FILENAME = "screen_gpt_secure_prefs"
        private const val KEY_APP_MODE = "app_mode"
        private const val KEY_API_KEY = "openai_api_key"
        private const val KEY_MODEL = "selected_ai_model"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_OCR_ENABLED = "ocr_enabled"

        const val MODE_QUICK_BRIDGE = "quick_bridge"
        const val MODE_OVERLAY_API = "overlay_api"

        const val MODEL_GPT_4O_MINI = "gpt-4o-mini"
        const val MODEL_GPT_4O = "gpt-4o"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
