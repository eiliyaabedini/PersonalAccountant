package ir.act.personalAccountant.ai.data.remote

import com.google.gson.Gson
import ir.act.personalAccountant.ai.domain.model.AIAnalysisResult
import ir.act.personalAccountant.ai.domain.model.CurrencyExchangeResponse
import ir.act.personalAccountant.ai.domain.model.ExchangeRateResult
import ir.act.personalAccountant.ai.domain.model.ReceiptAnalysisRequest
import ir.act.personalAccountant.ai.domain.model.ReceiptAnalysisResponse
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

    suspend fun sendRequest(
        request: OpenAIRequest,
        apiKey: String
    ): OpenAIResponse = withContext(Dispatchers.IO) {
        // Check if this is an o3-mini model that needs the Responses API
        if (request.model.startsWith("o3-")) {
            return@withContext handleO3MiniRequest(request, apiKey)
        }

        // For traditional models, ensure temperature is set if not provided
        val requestWithTemp = if (request.temperature == null) {
            request.copy(temperature = 0.2)
        } else {
            request
        }

        val response = apiService.createChatCompletion(
            authorization = "Bearer $apiKey",
            request = requestWithTemp
        )

        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response from API")
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = gson.fromJson(errorBody, OpenAIResponse::class.java)
                errorResponse.error?.message ?: "HTTP ${response.code()}: ${response.message()}"
            } catch (e: Exception) {
                "HTTP ${response.code()}: ${response.message()}"
            }
            throw Exception("API Error: $errorMessage")
        }
    }

    private suspend fun handleO3MiniRequest(
        request: OpenAIRequest,
        apiKey: String
    ): OpenAIResponse = withContext(Dispatchers.IO) {
        // Convert to Responses API format
        val responsesRequest = OpenAIRequest(
            model = request.model,
            input = request.messages, // Use messages as input for Responses API
            max_output_tokens = request.max_tokens
                ?: 4000, // Use max_output_tokens for Responses API
            reasoning = OpenAIReasoning(effort = "medium", summary = "auto")
        )

        val response = apiService.createResponse(
            authorization = "Bearer $apiKey",
            request = responsesRequest
        )

        if (response.isSuccessful) {
            val responsesBody = response.body() ?: throw Exception("Empty response from API")

            // Convert OpenAIResponsesResponse to OpenAIResponse format
            OpenAIResponse(
                id = responsesBody.id,
                `object` = responsesBody.`object`,
                created = responsesBody.created,
                model = responsesBody.model,
                choices = listOf(
                    OpenAIChoice(
                        index = 0,
                        message = OpenAIResponseMessage(
                            role = "assistant",
                            content = responsesBody.output_text ?: ""
                        ),
                        finish_reason = "stop"
                    )
                ),
                usage = responsesBody.usage,
                error = responsesBody.error
            )
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = gson.fromJson(errorBody, OpenAIResponsesResponse::class.java)
                errorResponse.error?.message ?: "HTTP ${response.code()}: ${response.message()}"
            } catch (e: Exception) {
                "HTTP ${response.code()}: ${response.message()}"
            }
            throw Exception("API Error: $errorMessage")
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

    suspend fun getCurrencyExchangeRate(
        fromCurrency: String,
        toCurrency: String,
        apiKey: String
    ): CurrencyExchangeResponse = withContext(Dispatchers.IO) {
        try {
            val request = createExchangeRateRequest(fromCurrency, toCurrency)
            val response = apiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody?.let { body ->
                    if (body.error != null) {
                        return@withContext CurrencyExchangeResponse(
                            success = false,
                            errorMessage = body.error.message
                        )
                    }

                    val content = body.choices.firstOrNull()?.message?.content
                    if (content != null) {
                        parseExchangeRateResponse(content, fromCurrency, toCurrency)
                    } else {
                        CurrencyExchangeResponse(
                            success = false,
                            errorMessage = "No response content received"
                        )
                    }
                } ?: CurrencyExchangeResponse(
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

                CurrencyExchangeResponse(
                    success = false,
                    errorMessage = errorMessage
                )
            }
        } catch (e: Exception) {
            CurrencyExchangeResponse(
                success = false,
                errorMessage = "Network error: ${e.message}"
            )
        }
    }

    private fun createExchangeRateRequest(fromCurrency: String, toCurrency: String): OpenAIRequest {
        val systemPrompt = """
            You are a currency exchange rate assistant. Search for the current live exchange rate from $fromCurrency to $toCurrency. 
            IMPORTANT: Find the exact timestamp when this exchange rate was published/updated by the source.
            
            Respond ONLY with valid JSON in this exact format:
            {
                "rate": 0.0000,
                "from_currency": "$fromCurrency",
                "to_currency": "$toCurrency",
                "timestamp_utc": "YYYY-MM-DD HH:MM:SS UTC",
                "source": "source_name",
                "confidence": 0.95
            }
            
            Rules:
            - rate must be a number representing how much 1 unit of $fromCurrency equals in $toCurrency
            - timestamp_utc should be the actual time when the exchange rate was last updated according to the source, not the current time
            - source should be the website/service where you found the rate
            - confidence should be between 0.0 and 1.0 based on source reliability
            - respond in only 4 precision like 4.2436 not more than that and round it up
        """.trimIndent()

        return OpenAIRequest(
            model = "gpt-4o-search-preview",
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
                            text = "Please search for and provide the current live exchange rate from $fromCurrency to $toCurrency, including the exact timestamp when this rate was last updated by the source."
                        )
                    )
                )
            ),
            max_tokens = 300,
            web_search_options = OpenAIWebSearchOptions(search_context_size = "medium")
        )
    }

    private fun parseExchangeRateResponse(
        content: String,
        fromCurrency: String,
        toCurrency: String
    ): CurrencyExchangeResponse {
        return try {
            val cleanContent = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val result = gson.fromJson(cleanContent, ExchangeRateResult::class.java)

            // Parse timestamp from UTC string format to milliseconds
            val timestamp = try {
                result.timestamp_utc?.let { timestampStr ->
                    // Try to parse the "YYYY-MM-DD HH:MM:SS UTC" format
                    val format =
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", java.util.Locale.US)
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    format.parse(timestampStr)?.time ?: System.currentTimeMillis()
                } ?: System.currentTimeMillis()
            } catch (e: Exception) {
                // If parsing fails, use current time
                System.currentTimeMillis()
            }

            CurrencyExchangeResponse(
                success = true,
                exchangeRate = result.rate,
                fromCurrency = result.from_currency,
                toCurrency = result.to_currency,
                timestamp = timestamp,
                source = result.source
            )
        } catch (e: Exception) {
            CurrencyExchangeResponse(
                success = false,
                errorMessage = "Failed to parse exchange rate response: ${e.message}"
            )
        }
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