package com.globus.crm.feature.auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.globus.crm.R
import com.globus.crm.feature.auth.presentation.state.SplashUiState

// Logo background color — matches app_logo.jpg exactly so the image blends into the screen.
private val LogoBgColor = Color(0xFF133F3E)

@Composable
fun SplashScreen(
    state: SplashUiState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LogoBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "App logo",
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White)
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
