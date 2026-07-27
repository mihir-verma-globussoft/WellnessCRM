package com.crm.enhance_wellness.feature.treatmentanalysis.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class TreatmentImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createCaptureFile(prescriptionId: Int, stage: String): File {
        val dir = File(context.cacheDir, "treatment_analysis_capture").apply { mkdirs() }
        return File(dir, "prescription_${prescriptionId}_${stage}_${System.currentTimeMillis()}.jpg")
    }

    fun compressToPrivateFile(source: File, prescriptionId: Int, stage: String): File {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeFile(source.absolutePath, decodeOptions)
            ?: throw IllegalArgumentException("Unable to decode captured image")

        val dir = File(context.filesDir, "treatment_analysis").apply { mkdirs() }
        val output = File(dir, "prescription_${prescriptionId}_${stage}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(output).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        }
        bitmap.recycle()
        return output
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        val largest = max(width, height)
        var sampleSize = 1
        while (largest / sampleSize > MAX_DIMENSION) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private companion object {
        const val MAX_DIMENSION = 1600
        const val JPEG_QUALITY = 80
    }
}
