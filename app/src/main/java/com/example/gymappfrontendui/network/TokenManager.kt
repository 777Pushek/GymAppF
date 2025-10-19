package com.example.gymappfrontendui.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class TokenManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val TOKEN_KEY = "auth_token"
        private const val TEMP_TOKEN_KEY = "temp_token"
    }
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit { putString(TOKEN_KEY, token) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        sharedPreferences.edit { remove(TOKEN_KEY) }
    }

    fun saveTemporaryToken(token: String) {
        sharedPreferences.edit { putString(TEMP_TOKEN_KEY, token) }
    }

    fun getTemporaryToken(): String? {
        return sharedPreferences.getString(TEMP_TOKEN_KEY, null)
    }

    fun clearTemporaryToken() {
        sharedPreferences.edit { remove(TEMP_TOKEN_KEY) }
    }


}