package com.example.if3210_2024_android_ppl.api

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context

class KeystoreHelper(context: Context) {
    private val sharedPreferencesFile = "EncryptedPreferences"
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        sharedPreferencesFile,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        encryptedSharedPreferences.edit().putString("ACCESS_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return encryptedSharedPreferences.getString("ACCESS_TOKEN", null)
    }
}
