package ir.act.personalAccountant.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import ir.act.personalAccountant.domain.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressionUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MAX_WIDTH = 1080    // Reduced for receipt optimization
        private const val MAX_HEIGHT = 1080   // Reduced for receipt optimization
        private const val QUALITY = 60        // Lower quality for text documents
        private const val TARGET_SIZE_KB = 200 // Target file size in KB
        private const val MIN_QUALITY = 30    // Minimum quality threshold
    }

    suspend fun compressImage(imageUri: String): SyncResult<File> {
        return withContext(Dispatchers.IO) {
            try {
                val originalFile =
                    File(Uri.parse(imageUri).path ?: throw IllegalArgumentException("Invalid URI"))

                if (!originalFile.exists()) {
                    return@withContext SyncResult.Error("File does not exist: ${originalFile.path}")
                }

                // Step 1: Initial compression with optimized settings
                var compressedFile = Compressor.compress(context, originalFile) {
                    resolution(MAX_WIDTH, MAX_HEIGHT)
                    quality(QUALITY)
                    format(Bitmap.CompressFormat.JPEG)
                }

                // Step 2: Iterative compression if file is still too large
                var currentQuality = QUALITY
                val targetSizeBytes = TARGET_SIZE_KB * 1024L

                while (compressedFile.length() > targetSizeBytes && currentQuality > MIN_QUALITY) {
                    currentQuality -= 10
                    compressedFile = Compressor.compress(context, originalFile) {
                        resolution(MAX_WIDTH, MAX_HEIGHT)
                        quality(currentQuality)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                }

                // If still too large, try further resolution reduction
                if (compressedFile.length() > targetSizeBytes) {
                    val reducedWidth = MAX_WIDTH * 2 / 3  // 720px
                    val reducedHeight = MAX_HEIGHT * 2 / 3  // 720px
                    currentQuality = 50

                    compressedFile = Compressor.compress(context, originalFile) {
                        resolution(reducedWidth, reducedHeight)
                        quality(currentQuality)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                }

                SyncResult.Success(compressedFile)
            } catch (e: Exception) {
                SyncResult.Error("Failed to compress image: ${e.message}", e)
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}