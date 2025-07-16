package ir.act.personalAccountant.ai

import android.net.Uri
import ir.act.personalAccountant.ai.domain.model.ReceiptAnalysisRequest
import ir.act.personalAccountant.ai.domain.model.ReceiptAnalysisResponse
import ir.act.personalAccountant.ai.domain.usecase.ImageAnalyzer
import ir.act.personalAccountant.ai.data.remote.OpenAIClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIEngine @Inject constructor(
    private val openAIClient: OpenAIClient,
    private val imageAnalyzer: ImageAnalyzer
) {

    suspend fun analyzeReceiptImage(
        imageUri: Uri,
        availableCategories: List<String>,
        apiKey: String,
        currencySymbol: String = "$",
        inputStreamProvider: (Uri) -> InputStream?
    ): Flow<ReceiptAnalysisResponse> = flow {
        try {
            // Convert image to base64
            val base64Image = imageAnalyzer.convertImageToBase64(imageUri, inputStreamProvider)

            if (base64Image == null) {
                emit(
                    ReceiptAnalysisResponse(
                        success = false,
                        errorMessage = "Failed to process image"
                    )
                )
                return@flow
            }

            // Create request
            val request = ReceiptAnalysisRequest(
                imageBase64 = base64Image,
                availableCategories = availableCategories,
                currencySymbol = currencySymbol
            )

            // Call OpenAI API
            val response = openAIClient.analyzeReceipt(request, apiKey)
            emit(response)

        } catch (e: Exception) {
            emit(
                ReceiptAnalysisResponse(
                    success = false,
                    errorMessage = "Analysis failed: ${e.message}"
                )
            )
        }
    }

    fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotBlank() && apiKey.startsWith("sk-")
    }

    suspend fun testConnection(apiKey: String): ReceiptAnalysisResponse {
        return try {
            // Simple test with text completion
            val testResponse = openAIClient.testConnection(apiKey)
            testResponse
        } catch (e: Exception) {
            ReceiptAnalysisResponse(
                success = false,
                errorMessage = "Connection test failed: ${e.message}"
            )
        }
    }
}