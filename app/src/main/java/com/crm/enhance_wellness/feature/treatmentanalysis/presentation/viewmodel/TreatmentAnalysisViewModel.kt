package com.crm.enhance_wellness.feature.treatmentanalysis.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.treatmentanalysis.data.image.ImageQualityChecker
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisStatus
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.repository.TreatmentAnalysisRepository
import com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state.CaptureStage
import com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state.TreatmentAnalysisUiEvent
import com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state.TreatmentAnalysisUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class TreatmentAnalysisNavEvent {
    object Back : TreatmentAnalysisNavEvent()
}

@HiltViewModel
class TreatmentAnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TreatmentAnalysisRepository,
    private val qualityChecker: ImageQualityChecker,
) : ViewModel() {

    private val prescriptionId: Int = checkNotNull(savedStateHandle["prescriptionId"])
    private val visitId: Int? = savedStateHandle.get<Int>("visitId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(TreatmentAnalysisUiState(prescriptionId = prescriptionId, visitId = visitId))
    val uiState: StateFlow<TreatmentAnalysisUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<TreatmentAnalysisNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        observeDraft()
    }

    fun onEvent(event: TreatmentAnalysisUiEvent) {
        when (event) {
            is TreatmentAnalysisUiEvent.CameraPermissionResult ->
                _uiState.value = _uiState.value.copy(hasCameraPermission = event.granted)
            is TreatmentAnalysisUiEvent.ImageCaptured -> handleImageCaptured(event.file)
            TreatmentAnalysisUiEvent.Retake ->
                _uiState.value = _uiState.value.copy(selectedImagePath = null, error = null)
            TreatmentAnalysisUiEvent.ConfirmImage -> confirmImage()
            TreatmentAnalysisUiEvent.RetryUpload -> retryUpload()
            TreatmentAnalysisUiEvent.DismissMessage ->
                _uiState.value = _uiState.value.copy(message = null, error = null)
            TreatmentAnalysisUiEvent.NavigateBack -> viewModelScope.launch {
                _navEvent.send(TreatmentAnalysisNavEvent.Back)
            }
        }
    }

    private fun observeDraft() {
        viewModelScope.launch {
            repository.observeDraft(prescriptionId).collect { draft ->
                val stage = when {
                    draft?.hasSubmittedAfter == true -> CaptureStage.AFTER
                    draft?.hasUploadedBefore == true -> CaptureStage.AFTER
                    else -> CaptureStage.BEFORE
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    draft = draft,
                    captureStage = stage,
                )
            }
        }
    }

    private fun handleImageCaptured(file: File) {
        viewModelScope.launch {
            val quality = qualityChecker.check(file)
            if (!quality.isAcceptable) {
                _uiState.value = _uiState.value.copy(error = quality.message, selectedImagePath = null)
                return@launch
            }
            _uiState.value = _uiState.value.copy(selectedImagePath = file.absolutePath, error = null)
        }
    }

    private fun confirmImage() {
        val imagePath = _uiState.value.selectedImagePath ?: return
        val stage = _uiState.value.captureStage
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null, message = null)
            val saveResult = when (stage) {
                CaptureStage.BEFORE -> repository.saveBeforeCapture(prescriptionId, File(imagePath))
                CaptureStage.AFTER -> repository.saveAfterCapture(prescriptionId, File(imagePath))
            }
            if (saveResult is Result.Error) {
                _uiState.value = _uiState.value.copy(isUploading = false, error = saveResult.message)
                return@launch
            }
            val uploadResult = when (stage) {
                CaptureStage.BEFORE -> uploadBefore()
                CaptureStage.AFTER -> uploadAfter()
            }
            _uiState.value = when (uploadResult) {
                is Result.Success -> _uiState.value.copy(
                    isUploading = false,
                    selectedImagePath = null,
                    message = if (stage == CaptureStage.BEFORE) {
                        "Before image saved. Capture after image later."
                    } else {
                        "Images submitted for review."
                    },
                )
                is Result.Error -> _uiState.value.copy(
                    isUploading = false,
                    selectedImagePath = null,
                    error = uploadResult.message,
                )
                Result.Loading -> _uiState.value
            }
        }
    }

    private fun retryUpload() {
        val draft = _uiState.value.draft ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null, message = null)
            val result = when (draft.status) {
                TreatmentAnalysisStatus.BEFORE_CAPTURED -> uploadBefore()
                TreatmentAnalysisStatus.AFTER_CAPTURED -> uploadAfter()
                else -> Result.Success(draft)
            }
            _uiState.value = when (result) {
                is Result.Success -> _uiState.value.copy(
                    isUploading = false,
                    message = when (draft.status) {
                        TreatmentAnalysisStatus.AFTER_CAPTURED -> "Images submitted for review."
                        else -> "Before image saved. Capture after image later."
                    },
                )
                is Result.Error -> _uiState.value.copy(isUploading = false, error = result.message)
                Result.Loading -> _uiState.value
            }
        }
    }

    private suspend fun uploadBefore(): Result<com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft> {
        val id = visitId ?: return Result.Error(
            code = "missing_visit_id",
            message = "This prescription does not have visit data, so photos cannot be uploaded.",
        )
        return repository.uploadBefore(prescriptionId, id)
    }

    private suspend fun uploadAfter(): Result<com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft> {
        val id = visitId ?: return Result.Error(
            code = "missing_visit_id",
            message = "This prescription does not have visit data, so photos cannot be uploaded.",
        )
        return repository.uploadAfter(prescriptionId, id)
    }
}
