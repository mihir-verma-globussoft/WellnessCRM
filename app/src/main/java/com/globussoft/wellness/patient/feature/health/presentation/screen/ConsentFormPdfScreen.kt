package com.globussoft.wellness.patient.feature.health.presentation.screen

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.globussoft.wellness.patient.feature.health.presentation.state.ConsentFormPdfUiEvent
import com.globussoft.wellness.patient.feature.health.presentation.state.ConsentFormPdfUiState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentFormPdfScreen(
    state: ConsentFormPdfUiState,
    onEvent: (ConsentFormPdfUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consent Form") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ConsentFormPdfUiEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(state.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.pdfBytes != null -> ConsentPdfViewer(pdfBytes = state.pdfBytes)
            }
        }
    }
}

@Composable
private fun ConsentPdfViewer(pdfBytes: ByteArray) {
    val context = LocalContext.current
    val bitmaps = remember(pdfBytes) {
        val file = File.createTempFile("consent_", ".pdf", context.cacheDir)
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
                contentDescription = "Consent form page",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}
