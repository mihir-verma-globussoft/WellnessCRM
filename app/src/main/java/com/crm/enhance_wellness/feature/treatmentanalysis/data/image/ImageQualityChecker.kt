package com.crm.enhance_wellness.feature.treatmentanalysis.data.image

import android.graphics.BitmapFactory
import java.io.File
import javax.inject.Inject
import kotlin.math.abs

data class ImageQualityResult(
    val isAcceptable: Boolean,
    val message: String? = null,
)

class ImageQualityChecker @Inject constructor() {
    fun check(file: File): ImageQualityResult {
        if (!file.exists() || file.length() == 0L) {
            return ImageQualityResult(false, "Image could not be read. Please retake it.")
        }

        val options = BitmapFactory.Options().apply { inSampleSize = 8 }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ?: return ImageQualityResult(false, "Image could not be read. Please retake it.")

        var totalBrightness = 0.0
        var totalEdgeDiff = 0.0
        var samples = 0

        val step = 2
        var y = 0
        while (y < bitmap.height - step) {
            var x = 0
            while (x < bitmap.width - step) {
                val current = luminance(bitmap.getPixel(x, y))
                val right = luminance(bitmap.getPixel(x + step, y))
                val bottom = luminance(bitmap.getPixel(x, y + step))
                totalBrightness += current
                totalEdgeDiff += abs(current - right) + abs(current - bottom)
                samples++
                x += step
            }
            y += step
        }
        bitmap.recycle()

        if (samples == 0) return ImageQualityResult(false, "Image is too small. Please retake it.")

        val averageBrightness = totalBrightness / samples
        val averageEdgeDiff = totalEdgeDiff / (samples * 2)

        return when {
            averageBrightness < MIN_BRIGHTNESS ->
                ImageQualityResult(false, "Photo is too dark. Move to better lighting and retake it.")
            averageBrightness > MAX_BRIGHTNESS ->
                ImageQualityResult(false, "Photo is too bright. Reduce glare and retake it.")
            averageEdgeDiff < MIN_EDGE_DIFF ->
                ImageQualityResult(false, "Photo looks blurry. Hold the camera steady and retake it.")
            else -> ImageQualityResult(true)
        }
    }

    private fun luminance(color: Int): Int {
        val red = color shr 16 and 0xff
        val green = color shr 8 and 0xff
        val blue = color and 0xff
        return ((0.299 * red) + (0.587 * green) + (0.114 * blue)).toInt()
    }

    private companion object {
        const val MIN_BRIGHTNESS = 35
        const val MAX_BRIGHTNESS = 235
        const val MIN_EDGE_DIFF = 4.0
    }
}
