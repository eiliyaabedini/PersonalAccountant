package ir.act.personalAccountant.ai.data.remote

import com.google.gson.Gson
import ir.act.personalAccountant.ai.domain.model.AIAnalysisResult
import ir.act.personalAccountant.ai.domain.model.AssetAIAnalysisResult
import ir.act.personalAccountant.ai.domain.model.AssetAnalysisRequest
import ir.act.personalAccountant.ai.domain.model.AssetAnalysisResponse
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

    suspend fun analyzeAsset(
        request: AssetAnalysisRequest,
        apiKey: String
    ): AssetAnalysisResponse = withContext(Dispatchers.IO) {
        try {
            val openAIRequest = createAssetAnalysisRequest(request)
            val response = apiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = openAIRequest
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody?.let { body ->
                    if (body.error != null) {
                        return@withContext AssetAnalysisResponse(
                            success = false,
                            errorMessage = body.error.message
                        )
                    }

                    val content = body.choices.firstOrNull()?.message?.content
                    if (content != null) {
                        parseAssetAIResponse(content)
                    } else {
                        AssetAnalysisResponse(
                            success = false,
                            errorMessage = "No response content received"
                        )
                    }
                } ?: AssetAnalysisResponse(
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

                AssetAnalysisResponse(
                    success = false,
                    errorMessage = errorMessage
                )
            }
        } catch (e: Exception) {
            AssetAnalysisResponse(
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
            3. Currency detected from the receipt (look for currency symbols, currency codes, or country context)
            
            Available categories: ${request.availableCategories.joinToString(", ")}
            
            Respond ONLY with valid JSON in this exact format:
            {
                "total_amount": 0.00,
                "category": "category_name",
                "confidence": 0.95,
                "currency_detected": "USD"
            }
            
            Rules:
            - total_amount must be a number (decimal)
            - category must be one of the available categories
            - confidence should be between 0.0 and 1.0 based on how certain you are about the amount and category
            - currency_detected should be the 3-letter ISO currency code (e.g., USD, EUR, GBP, JPY) detected from the receipt
            - Look for currency symbols ($, €, £, ¥), currency codes (USD, EUR), or country/language context to determine currency
            - If you can't determine the currency from the receipt, use "${
            request.currencySymbol.let { symbol ->
                when (symbol) {
                    "$" -> "USD"
                    "€" -> "EUR"
                    "£" -> "GBP"
                    "¥" -> "JPY"
                    else -> "USD"
                }
            }
        }" as default
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

    private fun createAssetAnalysisRequest(request: AssetAnalysisRequest): OpenAIRequest {
        val systemPrompt = """
            You are an asset analysis assistant. Analyze the provided trading app screenshot and extract:
            1. Asset name (e.g., "VUAA", "Top 500 US Stocks", "Bitcoin")
            2. Asset type from available types or suggest a new one if needed
            3. Amount per unit (price per share/unit/coin)
            4. Quantity (how many units are owned)
            5. Currency (EUR, USD, GBP, etc.)
            
            Available asset types: ${request.availableAssetTypes.joinToString(", ")}
            
            Respond ONLY with valid JSON in this exact format:
            {
                "assetName": "extracted_asset_name",
                "assetType": "type_from_available_or_new",
                "amountPerUnit": "0.00000000",
                "quantity": "0.00000000",
                "currency": "EUR",
                "confidence": 0.95
            }
            
            CRITICAL PRECISION RULES:
            - amountPerUnit and quantity MUST be strings with FULL decimal precision as shown in the screenshot
            - For crypto assets: preserve ALL decimal places (e.g., "0.83052318", "1.23456789", "0.00000001")
            - For stocks: preserve at least 4 decimal places (e.g., "123.4567")
            - NEVER round numbers or use scientific notation
            - NEVER truncate decimal places - extract the EXACT number shown
            - If you see "0.83052318", output exactly "0.83052318", not "0.83" or "0.8305"
            - assetName should be the exact name from the screenshot
            - assetType must be one of the available types or suggest a new appropriate type
            - currency should be the 3-letter ISO currency code detected
            - confidence should be between 0.0 and 1.0 based on how certain you are
            - Focus on extracting exact values shown in the screenshot with maximum precision
            - If any value is unclear, set confidence lower but still preserve all visible decimal places
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
                            text = "Please analyze this trading app screenshot and extract the asset information."
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

    private fun parseAssetAIResponse(content: String): AssetAnalysisResponse {
        return try {
            val cleanContent = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val result = gson.fromJson(cleanContent, AssetAIAnalysisResult::class.java)

            // Validate that the string values can be converted to valid numbers
            val validatedAmountPerUnit = try {
                result.amountPerUnit.toBigDecimal()
                result.amountPerUnit
            } catch (e: Exception) {
                return AssetAnalysisResponse(
                    success = false,
                    errorMessage = "Invalid amountPerUnit format: ${result.amountPerUnit}"
                )
            }

            val validatedQuantity = try {
                result.quantity.toBigDecimal()
                result.quantity
            } catch (e: Exception) {
                return AssetAnalysisResponse(
                    success = false,
                    errorMessage = "Invalid quantity format: ${result.quantity}"
                )
            }

            AssetAnalysisResponse(
                success = true,
                assetName = result.assetName,
                assetType = result.assetType,
                amountPerUnit = validatedAmountPerUnit,
                quantity = validatedQuantity,
                currency = result.currency,
                confidence = result.confidence
            )
        } catch (e: Exception) {
            AssetAnalysisResponse(
                success = false,
                errorMessage = "Failed to parse AI response: ${e.message}"
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
                confidence = result.confidence,
                detectedCurrency = result.currency_detected
            )
        } catch (e: Exception) {
            ReceiptAnalysisResponse(
                success = false,
                errorMessage = "Failed to parse AI response: ${e.message}"
            )
        }
    }
}