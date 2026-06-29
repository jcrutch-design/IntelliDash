package com.example.intellidash

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ModelConfig
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.GenerationConfig
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client for multimodal media indexing using local Gemini Nano via AICore (ML Kit GenAI).
 */
class MultimodalIndexerClient {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    companion object {
        private val SYSTEM_INSTRUCTIONS = """
            TASK: Analyze the provided image and generate a concise summary, a category, and a list of relevant tags.
            
            JSON TEMPLATE:
            {
              "summary": "A brief description of what is in the image.",
              "category": "The primary category of the image (e.g., Nature, People, Technology, Document, Screenshot).",
              "collectionName": "Suggest a category (e.g., 'Receipts', 'Travel', 'Work', 'Food', 'Social') that best groups this image.",
              "tags": ["tag1", "tag2", "tag3"]
            }
            
            INSTRUCTIONS:
            1. Return ONLY the raw JSON object.
            2. The summary should be one sentence.
            3. Tags should be specific and descriptive.
        """.trimIndent()
    }

    /**
     * Creates a [GenerativeModel] with the specified [ModelPreference].
     */
    private fun getModel(preference: Int): GenerativeModel {
        val modelConfig = ModelConfig.builder()
            .apply { releaseStage = ModelReleaseStage.PREVIEW }
            .apply { this.preference = preference }
            .build()

        val config = GenerationConfig.builder()
            .apply { this.modelConfig = modelConfig }
            .build()

        return Generation.getClient(config)
    }

    /**
     * Indexes an image by performing multimodal inference using AICore.
     * Implements fallback strategy (FULL -> FAST) and handles BUSY errors.
     * 
     * @param bitmap The image to index.
     * @param preferredCollections A list of user-defined collection names to prefer.
     * @return A parsed [IndexerResponse] or null if inference fails.
     */
    suspend fun indexImage(bitmap: Bitmap, preferredCollections: List<String> = emptyList()): IndexerResponse? = withContext(Dispatchers.IO) {
        val collectionsContext = if (preferredCollections.isNotEmpty()) {
            "\nCRITICAL: You MUST choose one of the following categories if it is at all relevant: ${preferredCollections.joinToString(", ")}. Only suggest a new one if none of the provided options fit."
        } else ""

        val promptText = """
            $SYSTEM_INSTRUCTIONS
            $collectionsContext
            
            INDEX TARGET:
            Analyze the attached image.
        """.trimIndent()

        // Fallback strategy: Try FULL (NPU) first, then FAST (CPU/GPU) if it fails.
        val preferences = listOf(ModelPreference.FULL, ModelPreference.FAST)
        var lastException: Throwable? = null

        for (preference in preferences) {
            val prefName = if (preference == ModelPreference.FULL) "FULL" else "FAST"
            Log.d("MultimodalIndexer", "Attempting index with ModelPreference.$prefName")

            try {
                val model = getModel(preference)
                
                // Build multimodal request: Text + Image
                val request = GenerateContentRequest.builder(
                    ImagePart(bitmap),
                    TextPart(promptText),
                ).build()

                // Perform inference
                val result = model.generateContent(request)
                val responseText = result.candidates.firstOrNull()?.text ?: ""
                
                if (responseText.isBlank()) {
                    Log.w("MultimodalIndexer", "Empty response from model with $prefName")
                    continue
                }

                val cleanedJson = extractJson(responseText)
                val response = json.decodeFromString<IndexerResponse>(cleanedJson)
                
                Log.d("MultimodalIndexer", "Indexing successful with ModelPreference.$prefName")
                return@withContext response
            } catch (e: Exception) {
                lastException = e
                Log.w("MultimodalIndexer", "ModelPreference.$prefName failed: ${e.message}")
                
                // Specifically check for BUSY or UNKNOWN errors if possible
                if ((e.message?.contains("BUSY", ignoreCase = true) == true) || 
                    (e.message?.contains("UNKNOWN", ignoreCase = true) == true)) {
                    Log.i("MultimodalIndexer", "AICore reported BUSY/UNKNOWN, attempting fallback...")
                }
            }
        }

        Log.e("MultimodalIndexer", "All indexing attempts failed: ${lastException?.message}")
        null
    }

    private fun extractJson(input: String): String {
        val start = input.indexOf('{')
        val end = input.lastIndexOf('}')
        val jsonContent = if (start != -1 && end != -1 && start < end) {
            input.substring(start, end + 1)
        } else {
            input.trim()
        }
        return repairJson(jsonContent)
    }

    private fun repairJson(input: String): String {
        return input
            .replace(Regex("""(?<=[}\]])\s+(?=[{\[])"""), ", ")
            .replace(Regex("""(?<=")\s+(?=")"""), ", ")
    }

    @Serializable
    data class IndexerResponse(
        val summary: String,
        val category: String,
        val collectionName: String? = null,
        val tags: List<String>
    )
}
