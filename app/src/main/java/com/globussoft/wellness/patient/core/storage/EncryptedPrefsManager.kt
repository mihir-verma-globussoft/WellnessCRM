package com.globussoft.wellness.patient.core.storage

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class EncryptedPrefsManager @Inject constructor(
    @Named("encrypted") private val prefs: SharedPreferences,
) {
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveUserInfo(userId: Int, name: String, email: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
