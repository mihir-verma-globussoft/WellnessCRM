package com.crm.enhance_wellness.feature.treatmentanalysis.presentation.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.crm.enhance_wellness.core.ui.WellnessCard
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisStatus
import com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state.CaptureStage
import com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state.TreatmentAnalysisUiEvent
import com.crm.enhance_wellness.feature.treatmentanalysis.presentation.state.TreatmentAnalysisUiState
import java.io.File
import java.util.concurrent.Executor

@Composable
fun TreatmentAnalysisScreen(
    state: TreatmentAnalysisUiState,
    onEvent: (TreatmentAnalysisUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { onEvent(TreatmentAnalysisUiEvent.CameraPermissionResult(it)) },
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        onEvent(TreatmentAnalysisUiEvent.CameraPermissionResult(granted))
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error
        if (!text.isNullOrBlank()) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            onEvent(TreatmentAnalysisUiEvent.DismissMessage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Before/After Scan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (state.visitId != null) {
                "Prescription #${state.prescriptionId} • Visit #${state.visitId}"
            } else {
                "Prescription #${state.prescriptionId}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StatusPanel(state)
        GuidancePanel()

        when {
            state.isLoading -> LoadingPanel()
            !state.hasCameraPermission -> PermissionPanel(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
            state.selectedImagePath != null -> CapturedPreview(
                imagePath = state.selectedImagePath,
                isUploading = state.isUploading,
                onRetake = { onEvent(TreatmentAnalysisUiEvent.Retake) },
                onConfirm = { onEvent(TreatmentAnalysisUiEvent.ConfirmImage) },
            )
            state.draft?.hasSubmittedAfter == true -> ReviewPendingPanel()
            else -> CameraCapturePanel(
                prescriptionId = state.prescriptionId,
                stage = state.captureStage,
                enabled = !state.isUploading,
                onCaptured = { onEvent(TreatmentAnalysisUiEvent.ImageCaptured(it)) },
                onError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
            )
        }

        RetryPanel(state = state, onRetry = { onEvent(TreatmentAnalysisUiEvent.RetryUpload) })
    }
}

@Composable
private fun StatusPanel(state: TreatmentAnalysisUiState) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = when {
                    state.draft?.hasSubmittedAfter == true -> "Images submitted for review."
                    state.draft?.hasUploadedBefore == true -> "Before image saved. Capture after image later."
                    state.draft?.status == TreatmentAnalysisStatus.BEFORE_CAPTURED -> "Before image captured. Upload is pending."
                    state.draft?.status == TreatmentAnalysisStatus.AFTER_CAPTURED -> "After image captured. Upload is pending."
                    else -> "Capture the before image first."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "You will explicitly confirm each upload. Medical photos are not uploaded in the background.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GuidancePanel() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Align the same treatment area.", style = MaterialTheme.typography.bodySmall)
        Text("Use good lighting and avoid glare.", style = MaterialTheme.typography.bodySmall)
        Text("Keep the camera steady before capture.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LoadingPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PermissionPanel(onRequestPermission: () -> Unit) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Camera permission is required to capture treatment images.")
            Button(onClick = onRequestPermission) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Allow camera")
            }
        }
    }
}

@Composable
private fun CapturedPreview(
    imagePath: String,
    isUploading: Boolean,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            contentScale = ContentScale.Crop,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onRetake,
                enabled = !isUploading,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retake")
            }
            Button(
                onClick = onConfirm,
                enabled = !isUploading,
                modifier = Modifier.weight(1f),
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text("Use photo")
            }
        }
    }
}

@Composable
private fun CameraCapturePanel(
    prescriptionId: Int,
    stage: CaptureStage,
    enabled: Boolean,
    onCaptured: (File) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val file = copyGalleryImageToCache(context, uri, prescriptionId, stage)
        if (file != null) {
            onCaptured(file)
        } else {
            onError("Unable to load selected image. Please try another photo.")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd,
        ) {
            CameraPreview(
                imageCapture = imageCapture,
                cameraSelector = cameraSelector,
                onBindError = { message ->
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    onError(message)
                },
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.75f),
                modifier = Modifier.padding(8.dp),
            ) {
                IconButton(
                    onClick = {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    },
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch camera",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Button(
            onClick = {
                captureImage(
                    context = context,
                    prescriptionId = prescriptionId,
                    stage = stage,
                    imageCapture = imageCapture,
                    executor = executor,
                    onCaptured = onCaptured,
                    onError = onError,
                )
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (stage == CaptureStage.BEFORE) "Capture before photo" else "Capture after photo")
        }

        OutlinedButton(
            onClick = { galleryLauncher.launch("image/*") },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Choose from gallery")
        }
    }
}

@Composable
private fun CameraPreview(
    imageCapture: ImageCapture,
    cameraSelector: CameraSelector,
    onBindError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, previewView, imageCapture, cameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                )
            }.onFailure { throwable ->
                val message = when (throwable) {
                    is IllegalArgumentException -> "Selected camera is not available on this device."
                    else -> throwable.message ?: "Unable to start camera preview."
                }
                onBindError(message)
            }
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clipToBounds(),
        )
    }
}

private fun captureImage(
    context: Context,
    prescriptionId: Int,
    stage: CaptureStage,
    imageCapture: ImageCapture,
    executor: Executor,
    onCaptured: (File) -> Unit,
    onError: (String) -> Unit,
) {
    val file = createCaptureFile(context, prescriptionId, stage)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptured(file)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception.message ?: "Unable to capture image.")
            }
        },
    )
}

private fun createCaptureFile(context: Context, prescriptionId: Int, stage: CaptureStage): File {
    val dir = File(context.cacheDir, "treatment_analysis_capture").apply { mkdirs() }
    val stageName = stage.name.lowercase()
    return File(dir, "prescription_${prescriptionId}_${stageName}_${System.currentTimeMillis()}.jpg")
}

private fun copyGalleryImageToCache(
    context: Context,
    uri: Uri,
    prescriptionId: Int,
    stage: CaptureStage,
): File? = runCatching {
    val file = createCaptureFile(context, prescriptionId, stage)
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    file.takeIf { it.length() > 0L }
}.getOrNull()

@Composable
private fun ReviewPendingPanel() {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Your before and after images are with the clinic for review.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun RetryPanel(
    state: TreatmentAnalysisUiState,
    onRetry: () -> Unit,
) {
    val canRetry = state.draft?.status == TreatmentAnalysisStatus.BEFORE_CAPTURED ||
        state.draft?.status == TreatmentAnalysisStatus.AFTER_CAPTURED
    if (!canRetry) return

    OutlinedButton(
        onClick = onRetry,
        enabled = !state.isUploading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.Upload, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Retry upload")
    }
}
