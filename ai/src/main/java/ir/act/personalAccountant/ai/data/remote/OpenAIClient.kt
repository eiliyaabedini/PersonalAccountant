package ir.act.personalAccountant.ai.data.remote

import com.google.gson.Gson
import ir.act.personalAccountant.ai.domain.model.ReceiptAnalysisRequest
import ir.act.personalAccountant.ai.domain.model.ReceiptAnalysisResponse
import ir.act.personalAccountant.ai.domain.model.AIAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAIClient @Inject constructor(
    private val apiService: OpenAIApiService,
    private val gson: Gson
) {
    suspend fun analyzeReceipt(
        request: ReceiptAnalysisRequest,
        apiKey: String
    ): ReceiptAnalysisResponse = withContext(Dispatchers.IO) {
        try {
            val openAIRequest = createOpenAIRequest(request)
            val response = apiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = openAIRequest
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody?.let { body ->
                    if (body.error != null) {
                        return@withContext ReceiptAnalysisResponse(
                            success = false,
                            errorMessage = body.error.message
                        )
                    }

                    val content = body.choices.firstOrNull()?.message?.content
                    if (content != null) {
                        parseAIResponse(content)
                    } else {
                        ReceiptAnalysisResponse(
                            success = false,
                            errorMessage = "No response content received"
                        )
                    }
                } ?: ReceiptAnalysisResponse(
                    success = false,
                    errorMessage = "Empty response from API"
                )
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val errorResponse = gson.fromJson(errorBody, OpenAIResponse::class.java)
                    errorResponse.error?.message ?: "HTTP ${response.code()}: ${response.message()}"
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }

                ReceiptAnalysisResponse(
                    success = false,
                    errorMessage = errorMessage
                )
            }
        } catch (e: Exception) {
            ReceiptAnalysisResponse(
                success = false,
                errorMessage = "Network error: ${e.message}"
            )
        }
    }

    suspend fun testConnection(apiKey: String): ReceiptAnalysisResponse =
        withContext(Dispatchers.IO) {
            try {
                val testRequest = OpenAIRequest(
                    model = "gpt-4o-mini-2024-07-18",
                    messages = listOf(
                        OpenAIMessage(
                            role = "user",
                            content = listOf(
                                OpenAIContent(
                                    type = "text",
                                    text = "Hi"
                                )
                            )
                        )
                    ),
                    max_tokens = 50,
                    temperature = 0.1
                )

                val response = apiService.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = testRequest
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let { body ->
                        if (body.error != null) {
                            return@withContext ReceiptAnalysisResponse(
                                success = false,
                                errorMessage = "API Error: ${body.error.message}"
                            )
                        }

                        val content = body.choices.firstOrNull()?.message?.content
                        if (content != null) {
                            ReceiptAnalysisResponse(
                                success = true,
                                errorMessage = "Connection successful! AI responded: \"$content\""
                            )
                        } else {
                            ReceiptAnalysisResponse(
                                success = false,
                                errorMessage = "No response content received"
                            )
                        }
                    } ?: ReceiptAnalysisResponse(
                        success = false,
                        errorMessage = "Empty response from API"
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = gson.fromJson(errorBody, OpenAIResponse::class.java)
                        errorResponse.error?.message
                            ?: "HTTP ${response.code()}: ${response.message()}"
                    } catch (e: Exception) {
                        "HTTP ${response.code()}: ${response.message()}"
                    }

                    ReceiptAnalysisResponse(
                        success = false,
                        errorMessage = "Connection failed: $errorMessage"
                    )
                }
            } catch (e: Exception) {
                ReceiptAnalysisResponse(
                    success = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }

    private fun createOpenAIRequest(request: ReceiptAnalysisRequest): OpenAIRequest {
        val systemPrompt = """
            You are a receipt analysis assistant. Analyze the provided receipt image and extract:
            1. Total amount (as a decimal number)
            2. Most appropriate category from the available options
            
            Available categories: ${request.availableCategories.joinToString(", ")}
            
            Respond ONLY with valid JSON in this exact format:
            {
                "total_amount": 0.00,
                "category": "category_name",
                "confidence": 0.95,
                "currency_detected": "${request.currencySymbol}"
            }
            
            Rules:
            - total_amount must be a number (decimal)
            - category must be one of the available categories
            - confidence should be between 0.0 and 1.0
            - If you can't determine the amount or category, use confidence < 0.5
        """.trimIndent()

        return OpenAIRequest(
            model = "gpt-4o-mini-2024-07-18",
            messages = listOf(
                OpenAIMessage(
                    role = "system",
                    content = listOf(
                        OpenAIContent(
                            type = "text",
                            text = systemPrompt
                        )
                    )
                ),
                OpenAIMessage(
                    role = "user",
                    content = listOf(
                        OpenAIContent(
                            type = "text",
                            text = "Please analyze this receipt image and provide the total amount and category."
                        ),
                        OpenAIContent(
                            type = "image_url",
                            image_url = OpenAIImageUrl(
                                url = "data:image/jpeg;base64,${request.imageBase64}"
                            )
                        )
                    )
                )
            ),
            max_tokens = 300,
            temperature = 0.2
        )
    }

    private fun parseAIResponse(content: String): ReceiptAnalysisResponse {
        return try {
            val cleanContent = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val result = gson.fromJson(cleanContent, AIAnalysisResult::class.java)

            ReceiptAnalysisResponse(
                success = true,
                totalAmount = result.total_amount,
                category = result.category,
                confidence = result.confidence
            )
        } catch (e: Exception) {
            ReceiptAnalysisResponse(
                success = false,
                errorMessage = "Failed to parse AI response: ${e.message}"
            )
        }
    }
}