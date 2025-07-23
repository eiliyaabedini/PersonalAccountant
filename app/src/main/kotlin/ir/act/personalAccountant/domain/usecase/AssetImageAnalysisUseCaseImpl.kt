package ir.act.personalAccountant.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.ai.data.remote.OpenAIClient
import ir.act.personalAccountant.ai.data.repository.AIRepository
import ir.act.personalAccountant.ai.domain.model.AssetAnalysisRequest
import ir.act.personalAccountant.ai.domain.usecase.ImageAnalyzer
import ir.act.personalAccountant.domain.model.AssetAnalysisResult
import ir.act.personalAccountant.domain.repository.AssetRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetImageAnalysisUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAIClient: OpenAIClient,
    private val aiRepository: AIRepository,
    private val imageAnalyzer: ImageAnalyzer,
    private val assetRepository: AssetRepository
) : AssetImageAnalysisUseCase {

    override suspend fun analyzeAssetImage(imageUri: Uri): Result<AssetAnalysisResult> {
        return try {
            val apiKey = aiRepository.apiKey.first()
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("OpenAI API key not configured"))
            }

            // Convert image to base64 using the existing ImageAnalyzer
            val base64Image = imageAnalyzer.convertImageToBase64(imageUri) { uri ->
                context.contentResolver.openInputStream(uri)
            } ?: return Result.failure(Exception("Failed to convert image to base64"))

            // Get available asset types  
            val availableAssetTypes = assetRepository.getAllAssetTypes().first()

            // Create request
            val request = AssetAnalysisRequest(
                imageBase64 = base64Image,
                availableAssetTypes = availableAssetTypes
            )

            // Call AI service
            val response = openAIClient.analyzeAsset(request, apiKey)

            if (response.success) {
                val analysisResult = AssetAnalysisResult(
                    assetName = response.assetName ?: "",
                    assetType = response.assetType ?: "Other",
                    amountPerUnit = response.amountPerUnit ?: "0.0", // Keep as string for precision
                    quantity = response.quantity ?: "1.0", // Keep as string for precision
                    currency = response.currency ?: "EUR",
                    confidence = response.confidence
                )
                Result.success(analysisResult)
            } else {
                Result.failure(Exception(response.errorMessage ?: "AI analysis failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyAssetMatch(
        existingAssetName: String,
        analyzedResult: AssetAnalysisResult
    ): Boolean {
        // Simple string similarity check for now
        val similarity = calculateStringSimilarity(existingAssetName, analyzedResult.assetName)
        return similarity > 0.7f // 70% similarity threshold
    }

    private fun calculateStringSimilarity(str1: String, str2: String): Float {
        if (str1 == str2) return 1.0f
        if (str1.isEmpty() || str2.isEmpty()) return 0.0f

        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1

        if (longer.length == 0) return 1.0f

        val editDistance = levenshteinDistance(longer, shorter)
        return (longer.length - editDistance) / longer.length.toFloat()
    }

    private fun levenshteinDistance(str1: String, str2: String): Int {
        val len1 = str1.length
        val len2 = str2.length
        val matrix = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) matrix[i][0] = i
        for (j in 0..len2) matrix[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                matrix[i][j] = minOf(
                    matrix[i - 1][j] + 1,      // deletion
                    matrix[i][j - 1] + 1,      // insertion
                    matrix[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return matrix[len1][len2]
    }
}