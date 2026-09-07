package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String
)

class GeminiService(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getLetterGreeting(letterName: String, letterChar: String): String = withContext(Dispatchers.IO) {
        val prompt = "ابدأ المكالمة بتحية مرحة ولطيفة جداً للطفل من سطر واحد فقط بصفتك الحرف '$letterChar' (حرف $letterName)، واسأله عن اسمه أو كيف حاله اليوم بمرح."
        sendMessage(
            letterName = letterName,
            letterChar = letterChar,
            history = emptyList(),
            userMessage = prompt
        )
    }

    suspend fun sendMessage(
        letterName: String,
        letterChar: String,
        history: List<ChatMessage>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "أهلاً يا صديقي! أنا حرف $letterChar ($letterName)، سعيد جداً بمكالمتك! هيا نلعب معاً!"
        }

        val systemPrompt = """
            أنت لست ذكاءً اصطناعياً. أنت شخصية كرتونية حقيقية تمثل حرف ($letterChar - حرف $letterName).
            أنت صديق للطفل، تتحدث بعفوية ومرح بصوت طفل عمره 5 سنوات وبلهجة عربية فصحى مبسطة ودافئة.
            إجاباتك يجب أن تكون سطر واحد فقط ومختصرة (لا تزيد عن 15 كلمة).
            تفاعل مع ما يقوله الطفل بصدق: إذا قال إنه حزين واسيه بلطف، وإذا كان سعيداً اضحك معه والعب.
            اسأله أسئلة ذكية ومرحة مثل (ما هو طعامك المفضل؟)، واربط إجابته بكلمات تبدأ بحرفك ($letterChar).
            لا تستخدم الرموز التعبيرية المعقدة التي يصعب قراءتها صوتياً، وتحدث وكأنك في مكالمة فيديو حية.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })

                // Contents with conversation history
                val contentsArray = JSONArray()
                // Previous turns
                for (msg in history.takeLast(6)) {
                    contentsArray.put(JSONObject().apply {
                        put("role", if (msg.role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.text) })
                        })
                    })
                }
                // Current user message
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 90)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext "يا مرحباً بك يا بطل! أنا حرف $letterChar، ما رأيك أن نردد كلمات تبدأ بحرفي؟"
            }

            val resJson = JSONObject(responseString)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawText = parts.getJSONObject(0).optString("text", "").trim()
                    // Strip asterisks or formatting
                    val cleaned = rawText.replace("*", "").trim()
                    if (cleaned.isNotBlank()) {
                        return@withContext cleaned
                    }
                }
            }
            "أهلاً يا صديقي! أنا سعيد بمكالمتك، ما هو الشيء المفضل لديك بحرف $letterChar؟"
        } catch (e: Exception) {
            "أهلاً يا بطل! أنا أسمعك جيداً! هيا أخبرني كلمة تبدأ بحرف $letterChar!"
        }
    }
}
