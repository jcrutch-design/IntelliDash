package com.example.intellidash

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client for multimodal media indexing using MediaPipe LlmInference with vision support.
 */
class MultimodalIndexerClient(private val context: Context) {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private var llmInference: LlmInference? = null

    private fun getInference(): LlmInference {
        return llmInference ?: synchronized(this) {
            if (llmInference != null) return@synchronized llmInference!!
            
            val optionsBuilder = LlmInferenceOptions.builder()
            optionsBuilder.setModelPath("/data/local/tmp/models/gemma-3n.bin")
            optionsBuilder.setMaxNumImages(1)
            
            val options = optionsBuilder.build()
            LlmInference.createFromOptions(context, options).also { llmInference = it }
        }
    }

    /**
     * Indexes an image by performing multimodal inference.
     * 
     * @param bitmap The image to index, downscaled to 512x512 for efficiency.
     * @return A parsed [IndexerResponse] or null if inference fails.
     */
    suspend fun indexImage(bitmap: Bitmap): IndexerResponse? = withContext(Dispatchers.IO) {
        try {
            val inference = getInference()
            val mpImage = BitmapImageBuilder(bitmap).build()

            // Use LlmInferenceSession to manage multimodal inputs (Image + Text).
            val session = LlmInferenceSession.createFromOptions(inference, LlmInferenceSession.LlmInferenceSessionOptions.builder().build())
            
            // 1. Add the visual modality
            session.addImage(mpImage)

            val prompt = """
                TASK: Analyze the provided image and generate a concise summary, a category, and a list of relevant tags.
                
                JSON TEMPLATE:
                {
                  "summary": "A brief description of what is in the image.",
                  "category": "The primary category of the image (e.g., Nature, People, Technology, Document, Screenshot).",
                  "tags": ["tag1", "tag2", "tag3"]
                }
                
                INSTRUCTIONS:
                1. Return ONLY the raw JSON object.
                2. The summary should be one sentence.
                3. Tags should be specific and descriptive.
            """.trimIndent()

            // 2. Add the text modality
            session.addQueryChunk(prompt)

            // 3. Generate response
            val responseText = session.generateResponse()
            
            val cleanedJson = extractJson(responseText)
            json.decodeFromString<IndexerResponse>(cleanedJson)
        } catch (e: Exception) {
            Log.e("MultimodalIndexer", "Failed to index image", e)
            null
        }
    }

    private fun extractJson(input: String): String {
        val start = input.indexOf('{')
        val end = input.lastIndexOf('}')
        return if (start != -1 && end != -1 && start < end) {
            input.substring(start, end + 1)
        } else {
            input.trim()
        }
    }

    @Serializable
    data class IndexerResponse(
        val summary: String,
        val category: String,
        val tags: List<String>
    )
}
