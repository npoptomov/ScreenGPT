package com.screengpt.overlay.api

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAIApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {

    suspend fun askAboutScreen(
        apiKey: String,
        model: String,
        prompt: String,
        base64Image: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("OpenAI API key is missing. Please set your API key in ScreenGPT settings."))
        }

        try {
            val payload = buildRequestPayload(model, prompt, base64Image)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(CHAT_COMPLETIONS_URL)
                .header("Authorization", "Bearer ${apiKey.trim()}")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                
                if (!response.isSuccessful) {
                    val errorMessage = parseErrorMessage(response.code, responseBody)
                    return@withContext Result.failure(IOException(errorMessage))
                }

                val parsedAnswer = parseCompletionResponse(responseBody)
                Result.success(parsedAnswer)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildRequestPayload(model: String, prompt: String, base64Image: String): JsonObject {
        val root = JsonObject()
        root.addProperty("model", model)
        root.addProperty("max_tokens", 1200)

        val messages = JsonArray()

        // 1. System Prompt
        val systemMsg = JsonObject()
        systemMsg.addProperty("role", "system")
        systemMsg.addProperty(
            "content",
            "You are ScreenGPT, an intelligent mobile screen visual assistant. " +
            "The user took a screenshot of what they are looking at on their phone and asked a question. " +
            "Analyze the image carefully, read any visible text, diagrams, code, or UI elements, " +
            "and provide a concise, direct, helpful, and clear answer. Use bullet points and clean structure where applicable."
        )
        messages.add(systemMsg)

        // 2. User Multimodal Message
        val userMsg = JsonObject()
        userMsg.addProperty("role", "user")

        val userContent = JsonArray()

        // Text Part
        val textObj = JsonObject()
        textObj.addProperty("type", "text")
        val effectivePrompt = if (prompt.isBlank()) "Explain what is on this screen in detail and summarize the key information." else prompt
        textObj.addProperty("text", effectivePrompt)
        userContent.add(textObj)

        // Image Part (Base64 JPEG)
        val imageObj = JsonObject()
        imageObj.addProperty("type", "image_url")
        val imageUrl = JsonObject()
        imageUrl.addProperty("url", "data:image/jpeg;base64,$base64Image")
        imageUrl.addProperty("detail", "auto")
        imageObj.add("image_url", imageUrl)
        userContent.add(imageObj)

        userMsg.add("content", userContent)
        messages.add(userMsg)

        root.add("messages", messages)
        return root
    }

    private fun parseCompletionResponse(jsonString: String): String {
        return try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val choices = jsonObject.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                message.get("content").asString.trim()
            } else {
                "No answer returned by ChatGPT."
            }
        } catch (e: Exception) {
            "Failed to parse ChatGPT response: ${e.localizedMessage}"
        }
    }

    private fun parseErrorMessage(statusCode: Int, responseBody: String): String {
        return try {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            val errorObj = jsonObject.getAsJsonObject("error")
            val message = errorObj?.get("message")?.asString
            when (statusCode) {
                401 -> "Invalid OpenAI API Key (HTTP 401). Please check your key in ScreenGPT settings."
                429 -> "Rate limit or quota exceeded (HTTP 429). Please check your OpenAI account billing/credits."
                400 -> "Bad Request (HTTP 400): ${message ?: "Image format or parameters rejected"}"
                else -> message ?: "OpenAI error (HTTP $statusCode): $responseBody"
            }
        } catch (e: Exception) {
            "HTTP error $statusCode: $responseBody"
        }
    }

    companion object {
        private const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
    }
}
