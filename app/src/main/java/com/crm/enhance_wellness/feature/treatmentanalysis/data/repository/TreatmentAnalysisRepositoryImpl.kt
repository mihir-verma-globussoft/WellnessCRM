package com.crm.enhance_wellness.feature.treatmentanalysis.data.repository

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.treatmentanalysis.data.image.TreatmentImageProcessor
import com.crm.enhance_wellness.feature.treatmentanalysis.data.local.dao.TreatmentAnalysisDraftDao
import com.crm.enhance_wellness.feature.treatmentanalysis.data.mapper.toDomain
import com.crm.enhance_wellness.feature.treatmentanalysis.data.mapper.toEntity
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisStatus
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.repository.TreatmentAnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentAnalysisRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val dao: TreatmentAnalysisDraftDao,
    private val imageProcessor: TreatmentImageProcessor,
) : TreatmentAnalysisRepository {

    override fun observeDraft(prescriptionId: Int): Flow<TreatmentAnalysisDraft?> =
        dao.observe(prescriptionId).map { it?.toDomain() }

    override suspend fun saveBeforeCapture(prescriptionId: Int, imageFile: File): Result<TreatmentAnalysisDraft> =
        saveCapture(
            prescriptionId = prescriptionId,
            imageFile = imageFile,
            stage = "before",
            status = TreatmentAnalysisStatus.BEFORE_CAPTURED,
        )

    override suspend fun saveAfterCapture(prescriptionId: Int, imageFile: File): Result<TreatmentAnalysisDraft> =
        saveCapture(
            prescriptionId = prescriptionId,
            imageFile = imageFile,
            stage = "after",
            status = TreatmentAnalysisStatus.AFTER_CAPTURED,
        )

    override suspend fun uploadBefore(prescriptionId: Int, visitId: Int): Result<TreatmentAnalysisDraft> = runCatching {
        val existing = dao.get(prescriptionId)?.toDomain()
        val localPath = existing?.beforeLocalPath
            ?: return Result.Error("missing_before", "Capture a before image first.")
        val response = api.uploadVisitTreatmentPhoto(
            visitId = visitId,
            photos = File(localPath).toMultipart(),
            kind = "before".toRequestBody("text/plain".toMediaType()),
        )
        if (!response.isSuccessful) throw HttpException(response)
        val updated = existing.copy(
            beforeRemoteUrl = existing.beforeRemoteUrl ?: "",
            status = TreatmentAnalysisStatus.BEFORE_UPLOADED,
            updatedAt = System.currentTimeMillis(),
        )
        dao.upsert(updated.toEntity())
        Result.Success(updated)
    }.getOrElse { Result.Error("upload_before_failed", it.message ?: "Before image upload failed.") }

    override suspend fun uploadAfter(prescriptionId: Int, visitId: Int): Result<TreatmentAnalysisDraft> = runCatching {
        val existing = dao.get(prescriptionId)?.toDomain()
            ?: return Result.Error("missing_draft", "Capture a before image first.")
        val localPath = existing.afterLocalPath
            ?: return Result.Error("missing_after", "Capture an after image first.")
        val response = api.uploadVisitTreatmentPhoto(
            visitId = visitId,
            photos = File(localPath).toMultipart(),
            kind = "after".toRequestBody("text/plain".toMediaType()),
        )
        if (!response.isSuccessful) throw HttpException(response)
        val updated = existing.copy(
            afterRemoteUrl = existing.afterRemoteUrl ?: "",
            status = TreatmentAnalysisStatus.SUBMITTED_FOR_REVIEW,
            updatedAt = System.currentTimeMillis(),
        )
        dao.upsert(updated.toEntity())
        Result.Success(updated)
    }.getOrElse { Result.Error("upload_after_failed", it.message ?: "After image upload failed.") }

    private suspend fun saveCapture(
        prescriptionId: Int,
        imageFile: File,
        stage: String,
        status: TreatmentAnalysisStatus,
    ): Result<TreatmentAnalysisDraft> = runCatching {
        val saved = imageProcessor.compressToPrivateFile(imageFile, prescriptionId, stage)
        val now = System.currentTimeMillis()
        val existing = dao.get(prescriptionId)?.toDomain()
        val draft = if (stage == "before") {
            TreatmentAnalysisDraft(
                prescriptionId = prescriptionId,
                analysisId = existing?.analysisId,
                beforeLocalPath = saved.absolutePath,
                beforeRemoteUrl = existing?.beforeRemoteUrl,
                beforeCapturedAt = now,
                afterLocalPath = null,
                afterRemoteUrl = null,
                afterCapturedAt = null,
                status = status,
                updatedAt = now,
            )
        } else {
            (existing ?: TreatmentAnalysisDraft(
                prescriptionId = prescriptionId,
                analysisId = null,
                beforeLocalPath = null,
                beforeRemoteUrl = null,
                beforeCapturedAt = null,
                afterLocalPath = null,
                afterRemoteUrl = null,
                afterCapturedAt = null,
                status = TreatmentAnalysisStatus.DRAFT,
                updatedAt = now,
            )).copy(
                afterLocalPath = saved.absolutePath,
                afterCapturedAt = now,
                status = status,
                updatedAt = now,
            )
        }
        dao.upsert(draft.toEntity())
        Result.Success(draft)
    }.getOrElse { Result.Error("capture_save_failed", it.message ?: "Unable to save image.") }

    private fun File.toMultipart(): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            name = "photos",
            filename = name,
            body = asRequestBody("image/jpeg".toMediaType()),
        )
}
