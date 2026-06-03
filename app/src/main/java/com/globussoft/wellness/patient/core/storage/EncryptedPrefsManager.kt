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
        private const val KEY_PATIENT_ID = "patient_id"
        private const val KEY_PATIENT_NAME = "patient_name"
        private const val KEY_PATIENT_PHONE = "patient_phone"
    }

    fun savePatientInfo(id: Int, name: String, phone: String) {
        prefs.edit()
            .putInt(KEY_PATIENT_ID, id)
            .putString(KEY_PATIENT_NAME, name)
            .putString(KEY_PATIENT_PHONE, phone)
            .apply()
    }

    fun getPatientId(): Int = prefs.getInt(KEY_PATIENT_ID, -1)

    fun getPatientName(): String? = prefs.getString(KEY_PATIENT_NAME, null)

    fun getPatientPhone(): String? = prefs.getString(KEY_PATIENT_PHONE, null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
