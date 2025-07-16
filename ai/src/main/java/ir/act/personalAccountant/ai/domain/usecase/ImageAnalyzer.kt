package ir.act.personalAccountant.ai.domain.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageAnalyzer @Inject constructor() {

    suspend fun convertImageToBase64(
        imageUri: Uri,
        inputStreamProvider: (Uri) -> InputStream?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = inputStreamProvider(imageUri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return@withContext null

            // Compress and resize if needed
            val optimizedBitmap = optimizeBitmap(bitmap)

            val outputStream = ByteArrayOutputStream()
            optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun optimizeBitmap(originalBitmap: Bitmap): Bitmap {
        val maxSize = 1024 // Maximum width or height

        val width = originalBitmap.width
        val height = originalBitmap.height

        if (width <= maxSize && height <= maxSize) {
            return originalBitmap
        }

        val scaleFactor = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }

        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    }
}