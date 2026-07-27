package com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state

import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft
import java.io.File

data class TreatmentAnalysisUiState(
    val prescriptionId: Int = 0,
    val visitId: Int? = null,
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val draft: TreatmentAnalysisDraft? = null,
    val captureStage: CaptureStage = CaptureStage.BEFORE,
    val selectedImagePath: String? = null,
    val message: String? = null,
    val error: String? = null,
)

enum class CaptureStage {
    BEFORE,
    AFTER,
}

sealed class TreatmentAnalysisUiEvent {
    data class CameraPermissionResult(val granted: Boolean) : TreatmentAnalysisUiEvent()
    data class ImageCaptured(val file: File) : TreatmentAnalysisUiEvent()
    object Retake : TreatmentAnalysisUiEvent()
    object ConfirmImage : TreatmentAnalysisUiEvent()
    object RetryUpload : TreatmentAnalysisUiEvent()
    object DismissMessage : TreatmentAnalysisUiEvent()
    object NavigateBack : TreatmentAnalysisUiEvent()
}
