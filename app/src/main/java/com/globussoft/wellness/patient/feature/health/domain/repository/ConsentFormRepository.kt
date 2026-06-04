package com.globussoft.wellness.patient.feature.health.domain.repository

import com.globussoft.wellness.patient.feature.health.domain.model.ConsentForm

interface ConsentFormRepository {
    suspend fun getConsentForms(): List<ConsentForm>
    suspend fun getConsentFormPdf(consentId: Int): ByteArray
}
