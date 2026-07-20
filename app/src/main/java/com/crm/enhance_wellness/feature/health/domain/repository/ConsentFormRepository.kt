package com.crm.enhance_wellness.feature.health.domain.repository

import com.crm.enhance_wellness.feature.health.domain.model.ConsentForm

interface ConsentFormRepository {
    suspend fun getConsentForms(): List<ConsentForm>
    suspend fun getConsentFormPdf(consentId: Int): ByteArray
}
