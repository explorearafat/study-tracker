package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(
    @field:Json(name = "text") val text: String
)

data class GeminiContent(
    @field:Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiCandidate(
    @field:Json(name = "content") val content: GeminiContent?
)

data class GeminiResponse(
    @field:Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

data class GeminiRequest(
    @field:Json(name = "contents") val contents: List<GeminiContent>
)

data class MotivationalQuote(
    val quote: String,
    val author: String,
    val isAiGenerated: Boolean = true
)

interface GeminiApiInterface {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class GeminiQuoteRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GeminiApiInterface by lazy {
        retrofit.create(GeminiApiInterface::class.java)
    }

    private val fallbackQuotes = listOf(
        MotivationalQuote("The secret of getting ahead is getting started.", "Mark Twain", false),
        MotivationalQuote("Focus is a muscle. The more you practice, the stronger your concentration becomes.", "Academic Wisdom", false),
        MotivationalQuote("Small daily improvements over time lead to stunning results.", "Robin Sharma", false),
        MotivationalQuote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier", false),
        MotivationalQuote("Believe you can and you're halfway there.", "Theodore Roosevelt", false)
    )

    suspend fun fetchDailyMotivationalQuote(subjectTopic: String? = null): MotivationalQuote = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null") {
            return@withContext fallbackQuotes.random()
        }

        val promptText = if (!subjectTopic.isNullOrBlank()) {
            "Generate a short, inspiring academic motivational quote for a student studying $subjectTopic. Output JSON only with keys 'quote' and 'author'. Keep quote under 25 words."
        } else {
            "Generate a short, inspiring daily motivational quote for a university student focused on studying, focus, and academic success. Output JSON only with keys 'quote' and 'author'. Keep quote under 25 words."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = promptText))
                )
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext fallbackQuotes.random()

            val cleanedText = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanedText)
            val quote = json.optString("quote", rawText)
            val author = json.optString("author", "Academic Wisdom")

            MotivationalQuote(quote = quote, author = author, isAiGenerated = true)
        } catch (e: Exception) {
            fallbackQuotes.random()
        }
    }
}
