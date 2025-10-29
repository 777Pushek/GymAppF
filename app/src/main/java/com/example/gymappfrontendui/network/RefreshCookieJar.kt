package com.example.gymappfrontendui.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import androidx.core.content.edit

class RefreshCookieJar(context: Context) : CookieJar {
    companion object {
        private const val PREFS_NAME = "refresh_cookie_prefs"
        private const val REFRESH_COOKIE_KEY = "refresh_cookie"
    }
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private var cookieCache: List<Cookie> = emptyList()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieCache = cookies
        cookies.find { it.name == "refreshToken" }?.let { refreshCookie ->
            prefs.edit { putString(REFRESH_COOKIE_KEY, refreshCookie.toString()) }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieString = prefs.getString(REFRESH_COOKIE_KEY, null)
        return if (cookieString != null) {
            Cookie.parse(url, cookieString)?.let { listOf(it) } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit { clear() }
        cookieCache = emptyList()
    }
}