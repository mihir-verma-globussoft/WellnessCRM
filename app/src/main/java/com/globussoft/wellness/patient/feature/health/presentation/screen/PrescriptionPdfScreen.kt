package com.globussoft.wellness.patient.feature.health.presentation.screen

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.globussoft.wellness.patient.feature.health.presentation.state.PrescriptionPdfUiEvent
import com.globussoft.wellness.patient.feature.health.presentation.state.PrescriptionPdfUiState
import java.io.File

@Composable
fun PrescriptionPdfScreen(
    state: PrescriptionPdfUiState,
    onEvent: (PrescriptionPdfUiEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.error != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Text(state.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.pdfBytes != null -> PdfViewer(pdfBytes = state.pdfBytes)
        }
    }
}

@Composable
private fun PdfViewer(pdfBytes: ByteArray) {
    val context = LocalContext.current
    val bitmaps = remember(pdfBytes) {
        val file = File.createTempFile("rx_", ".pdf", context.cacheDir)
        file.writeBytes(pdfBytes)
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        val pages = mutableListOf<android.graphics.Bitmap>()
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bmp = createBitmap(page.width * 2, page.height * 2)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pages.add(bmp)
        }
        renderer.close()
        fd.close()
        file.delete()
        pages
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        bitmaps.forEach { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Prescription page",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}
