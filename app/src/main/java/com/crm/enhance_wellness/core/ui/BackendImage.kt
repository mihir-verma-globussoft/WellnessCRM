package com.crm.enhance_wellness.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.SubcomposeAsyncImage
import com.crm.enhance_wellness.core.util.BackendImageUrlResolver

@Composable
fun BackendImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable BoxScope.() -> Unit,
) {
    val resolvedUrl = BackendImageUrlResolver.resolve(imageUrl)
    if (resolvedUrl == null) {
        ImageFallback(
            modifier = modifier.imageDescription(contentDescription),
            shape = shape,
            fallback = fallback,
        )
        return
    }

    SubcomposeAsyncImage(
        model = resolvedUrl,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
            .clip(shape)
            .imageDescription(contentDescription),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        },
        error = {
            ImageFallback(modifier = Modifier.fillMaxSize(), shape = shape, fallback = fallback)
        },
    )
}

@Composable
fun BackendImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable BoxScope.() -> Unit,
) {
    val resolvedModel = if (model is String) BackendImageUrlResolver.resolve(model) else model
    if (resolvedModel == null) {
        ImageFallback(
            modifier = modifier.imageDescription(contentDescription),
            shape = shape,
            fallback = fallback,
        )
        return
    }

    SubcomposeAsyncImage(
        model = resolvedModel,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
            .clip(shape)
            .imageDescription(contentDescription),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        },
        error = {
            ImageFallback(modifier = Modifier.fillMaxSize(), shape = shape, fallback = fallback)
        },
    )
}

private fun Modifier.imageDescription(contentDescription: String?): Modifier =
    if (contentDescription.isNullOrBlank()) {
        this
    } else {
        semantics { this.contentDescription = contentDescription }
    }

@Composable
private fun ImageFallback(
    modifier: Modifier,
    shape: Shape,
    fallback: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
        content = fallback,
    )
}
