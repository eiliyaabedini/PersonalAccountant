package ir.act.personalAccountant.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageFileManager @Inject constructor() {
    
    companion object {
        private const val IMAGES_DIRECTORY = "images"
        private const val IMAGE_PREFIX = "expense_"
        private const val IMAGE_EXTENSION = ".jpg"
        const val FILE_PROVIDER_AUTHORITY = "ir.act.personalAccountant.provider"
    }
    
    fun createImageFile(context: Context): File {
        val timestamp = System.currentTimeMillis()
        val imageDir = getImagesDirectory(context)
        return File(imageDir, "$IMAGE_PREFIX$timestamp$IMAGE_EXTENSION")
    }
    
    fun createTempImageFile(context: Context): File {
        val timestamp = System.currentTimeMillis()
        val imageDir = getImagesDirectory(context)
        return File(imageDir, "${IMAGE_PREFIX}temp_$timestamp$IMAGE_EXTENSION")
    }
    
    fun getFileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            file
        )
    }
    
    suspend fun copyImageFromUri(context: Context, sourceUri: Uri, destinationFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(destinationFile)
                
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
    
    suspend fun deleteImage(imagePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    file.delete()
                } else {
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    fun imageExists(imagePath: String?): Boolean {
        return if (imagePath.isNullOrEmpty()) {
            false
        } else {
            File(imagePath).exists()
        }
    }
    
    fun getImageFile(imagePath: String): File? {
        return if (imagePath.isNotEmpty()) {
            val file = File(imagePath)
            if (file.exists()) file else null
        } else {
            null
        }
    }
    
    private fun getImagesDirectory(context: Context): File {
        val imageDir = File(context.filesDir, IMAGES_DIRECTORY)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        return imageDir
    }
    
    suspend fun cleanupOrphanedImages(context: Context, validImagePaths: List<String>) {
        withContext(Dispatchers.IO) {
            try {
                val imageDir = getImagesDirectory(context)
                val validFileNames = validImagePaths.map { File(it).name }.toSet()
                
                imageDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.startsWith(IMAGE_PREFIX) && 
                        file.name !in validFileNames) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}