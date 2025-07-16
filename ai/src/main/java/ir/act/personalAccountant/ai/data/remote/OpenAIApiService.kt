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
}

data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val max_tokens: Int = 300,
    val temperature: Double = 0.2
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