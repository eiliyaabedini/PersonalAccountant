package ir.act.personalAccountant.ai.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApiService {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): Response<OpenAIResponse>

    @POST("v1/responses")
    suspend fun createResponse(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): Response<OpenAIResponsesResponse>
}

data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>? = null,
    val input: Any? = null, // Can be string or list of messages for o3-mini
    val max_tokens: Int? = null, // For traditional models
    val max_completion_tokens: Int? = null, // For o3-mini reasoning models (Chat Completions API)
    val max_output_tokens: Int? = null, // For o3-mini reasoning models (Responses API)
    val temperature: Double? = null, // Optional - not supported by o3-mini models
    val reasoning: OpenAIReasoning? = null, // For o3-mini reasoning
    val web_search_options: OpenAIWebSearchOptions? = null // For web search functionality
)

data class OpenAIMessage(
    val role: String,
    val content: List<OpenAIContent>
)

data class OpenAIContent(
    val type: String,
    val text: String? = null,
    val image_url: OpenAIImageUrl? = null
)

data class OpenAIImageUrl(
    val url: String
)

data class OpenAIResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<OpenAIChoice>,
    val usage: OpenAIUsage?,
    val error: OpenAIError?
)

data class OpenAIChoice(
    val index: Int,
    val message: OpenAIResponseMessage,
    val finish_reason: String?
)

data class OpenAIResponseMessage(
    val role: String,
    val content: String
)

data class OpenAIUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class OpenAIError(
    val message: String,
    val type: String,
    val param: String?,
    val code: String?
)

// For o3-mini reasoning models
data class OpenAIReasoning(
    val effort: String = "medium", // "low", "medium", or "high"
    val summary: String = "auto"   // "auto", "detailed", or "none"
)

// Response structure for Responses API (o3-mini)
data class OpenAIResponsesResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val output: List<OpenAIOutputItem>,
    val output_text: String?,
    val usage: OpenAIUsage?,
    val error: OpenAIError?
)

data class OpenAIOutputItem(
    val type: String, // "message", "reasoning", etc.
    val role: String?,
    val content: String?
)

// For web search options
data class OpenAIWebSearchOptions(
    val search_context_size: String = "medium" // "low", "medium", "high"
)