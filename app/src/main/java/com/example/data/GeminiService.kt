package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Gemini REST API Data Classes with Moshi annotations ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client ---

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Gemini Content Generation Helper ---

object GeminiHelper {
    suspend fun generateCoachResponse(
        userName: String,
        userGoal: String,
        conversationHistory: List<Content>,
        latestPrompt: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineMockResponse(userName, userGoal, latestPrompt)
        }

        // Build prompt contents
        val systemPrompt = "Sen LifeOS AI uygulamasının premium yaşam koçusun. " +
                "Kullanıcının adı $userName. Hedefi: $userGoal. " +
                "Kullanıcıya her zaman ismiyle ($userName) hitap etmelisin. " +
                "Kısa, son derece motive edici, premium ve ilham verici yanıtlar ver. " +
                "Apple, Nothing ve Notion felsefesinde net, öz ve etkileyici konuş."

        val finalContents = mutableListOf<Content>()
        // Add conversation history
        finalContents.addAll(conversationHistory)
        // Add current prompt
        finalContents.add(Content(parts = listOf(Part(text = latestPrompt))))

        val request = GenerateContentRequest(
            contents = finalContents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Cevap üretilemedi. Lütfen tekrar dener misin, $userName?"
        } catch (e: Exception) {
            e.printStackTrace()
            getOfflineMockResponse(userName, userGoal, latestPrompt)
        }
    }

    private fun getOfflineMockResponse(userName: String, userGoal: String, prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("selam") || query.contains("merhaba") -> {
                "Merhaba $userName! Ben senin kişisel yaşam koçunum. Bugün $userGoal hedefine odaklanmak ve gününü mükemmelleştirmek için harika bir gün. Sana nasıl yardımcı olabilirim?"
            }
            query.contains("spor") || query.contains("antrenman") || query.contains("kas") || query.contains("kilo") -> {
                "Harika bir spor vizyonu, $userName! $userGoal hedefin doğrultusunda bugünkü antrenmanını aksatmamalısın. Disiplin, motivasyonun tükendiği yerde başlar. Su içmeyi de unutma!"
            }
            query.contains("hedef") || query.contains("plan") || query.contains("yapacak") -> {
                "Planlama başarının yarısıdır, $userName. Bugün tamamlaman gereken görevlerini Tasks menüsünden kontrol et ve en yüksek öncelikliden başla. Küçük adımlar büyük zaferler getirir."
            }
            query.contains("finans") || query.contains("para") || query.contains("bütçe") || query.contains("tasarruf") -> {
                "Finansal özgürlük yolculuğu, $userName! Giderlerini her gün kaydetmek tasarruf kaslarını güçlendirir. Bugün bütçeni kontrol et ve gereksiz harcamalardan kaçın."
            }
            query.contains("motivasyon") || query.contains("mutsuz") || query.contains("yorgun") -> {
                "Bugün kendini yorgun hissetmen çok normal, $userName. Ancak unutma ki bugünkü disiplinin, yarınki karakterini inşa ediyor. Şimdi derin bir nefes al, bir bardak su iç bir görev tamamlayarak başla!"
            }
            else -> {
                "Harika bir soru, $userName! $userGoal yolundaki bu adımı çok önemsiyorum. Bugünü verimli kılmak için öncelikli işlerine odaklan ve her gün bir önceki günden %1 daha iyi olmaya çalış."
            }
        }
    }
}
