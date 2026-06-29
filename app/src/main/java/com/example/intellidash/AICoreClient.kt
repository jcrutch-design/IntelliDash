package com.example.intellidash

import android.util.Log
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ModelConfig
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.GenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client for interacting with local Gemini Nano inference via AICore.
 */
class AICoreClient {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    companion object {
        private val SYSTEM_INSTRUCTIONS = """
            TASK: Audit the provided input for security risks, complexity, and vulnerabilities.
            
            JSON TEMPLATE:
            {
              "cyclomaticComplexity": 5,
              "vulnerabilities": ["Vulnerability 1", "Vulnerability 2"],
              "refactoredSolution": "Refactored solution or recommendations here..."
            }
            
            INSTRUCTIONS:
            1. Return ONLY the raw JSON object. No Markdown code blocks (no ```json).
            2. "cyclomaticComplexity" MUST be a single integer (Risk Level 1-10 for system context, or actual cyclomatic complexity for code).
            3. "vulnerabilities" MUST be a JSON array of strings.
            4. "refactoredSolution" MUST contain the refactored code or security recommendations.
            5. Ensure all JSON keys and string values are enclosed in double quotes.
            6. Do not include trailing commas.
        """.trimIndent()
    }

    /**
     * Warms up the AICore model by performing a lightweight dummy inference.
     * This ensures the model is loaded into memory (NPU/GPU) for faster subsequent requests.
     */
    suspend fun warmup() = withContext(Dispatchers.IO) {
        Log.d("AICore", "Warmup started")
        try {
            // Prefer FULL (NPU) for warmup.
            val model = getModel(ModelPreference.FULL)
            model.generateContent("warmup")
            Log.d("AICore", "Warmup completed with ModelPreference.FULL")
        } catch (_: Exception) {
            // Fallback to FAST if FULL is not available.
            try {
                val model = getModel(ModelPreference.FAST)
                model.generateContent("warmup")
                Log.d("AICore", "Warmup completed with ModelPreference.FAST")
            } catch (_: Exception) {
                Log.e("AICore", "Warmup failed")
                // Ignore warmup errors.
            }
        }
    }

    /**
     * Creates a [GenerativeModel] with the specified [ModelPreference].
     */
    private fun getModel(preference: Int): GenerativeModel {
        val modelConfig = ModelConfig.builder()
            // Utilize the Preview release track for the latest local AI features.
            .apply { releaseStage = ModelReleaseStage.PREVIEW }
            // Apply specified preference (FULL for NPU, FAST for fallback CPU/GPU).
            .apply { this.preference = preference }
            .build()

        val config = GenerationConfig.builder()
            .apply { this.modelConfig = modelConfig }
            .build()

        return Generation.getClient(config)
    }

    /**
     * Audits a code snippet for complexity and vulnerabilities using local LLM inference.
     * Hardware acceleration via the device's NPU is automatically routed by AICore.
     * Memory optimization is handled by AICore during local inference.
     *
     * @param fileName The name of the file being audited.
     * @param rawCodeSnippet The source code to analyze.
     * @return A parsed [AuditResponse] object.
     */
    suspend fun auditCodeSnippet(fileName: String, rawCodeSnippet: String): AuditResponse = withContext(Dispatchers.IO) {
        val sanitizedSnippet = sanitizeInput(rawCodeSnippet)
        val prompt = """
            $SYSTEM_INSTRUCTIONS
            
            AUDIT TARGET (Code from file: $fileName):
            $sanitizedSnippet
        """.trimIndent()

        generateAndParse(prompt)
    }

    /**
     * Audits device security posture based on system configuration.
     *
     * @param systemContext String containing system information (e.g., kernel version, SELinux status).
     * @return A parsed [AuditResponse] object.
     */
    suspend fun auditSystemSecurity(systemContext: String): AuditResponse = withContext(Dispatchers.IO) {
        val sanitizedContext = sanitizeInput(systemContext)
        val prompt = """
            $SYSTEM_INSTRUCTIONS
            
            AUDIT TARGET (Device System Context):
            $sanitizedContext
        """.trimIndent()

        generateAndParse(prompt)
    }

    private suspend fun generateAndParse(prompt: String): AuditResponse {
        // Fallback strategy: Try FULL (NPU) first, then FAST (CPU/GPU) if it fails.
        val preferences = listOf(ModelPreference.FULL, ModelPreference.FAST)
        var lastException: Throwable? = null

        for (preference in preferences) {
            val prefName = if (preference == ModelPreference.FULL) "FULL" else "FAST"
            Log.d("AICore", "Attempting audit with ModelPreference.$prefName")
            
            try {
                val model = getModel(preference)
                // Use generateContentStream for reduced perceived latency and better prefix caching utilization.
                val responseTextBuilder = StringBuilder()
                model.generateContentStream(prompt).collect { chunk ->
                    responseTextBuilder.append(chunk.candidates.firstOrNull()?.text ?: "")
                }
                
                val responseText = responseTextBuilder.toString()
                if (responseText.isBlank()) {
                    throw IllegalStateException("Model returned empty response")
                }

                // Parse the structured JSON output into our data class.
                val cleanedJson = extractJson(responseText)
                val response = json.decodeFromString<AuditResponse>(cleanedJson)
                
                Log.d("AICore", "Audit successful with ModelPreference.$prefName")
                
                return response.copy(modelVariant = "Gemma 4 ($prefName)")
            } catch (e: Exception) {
                lastException = e
                if (preference == ModelPreference.FULL) {
                    Log.w("AICore", "ModelPreference.FULL failed, falling back to FAST: ${e.message}")
                    Log.d("AICore", "Fallback to ModelPreference.FAST")
                }
            } catch (e: Throwable) {
                lastException = e
            }
        }

        Log.e("AICore", "Audit failed with ${lastException?.message}")
        // Return a user-friendly error response instead of crashing if all inference attempts fail.
        return AuditResponse(
            cyclomaticComplexity = 0,
            vulnerabilities = listOf("Inference failed due to hardware limitations or safety filters."),
            refactoredSolution = "Error details: ${lastException?.message ?: "Unknown AICore Error"}. Please ensure Gemini Nano is updated in Google Play Services.",
            modelVariant = "None"
        )
    }

    /**
     * Extracts the raw JSON object from a string by finding the first '{' and last '}'.
     */
    private fun extractJson(input: String): String {
        val start = input.indexOf('{')
        val end = input.lastIndexOf('}')
        val jsonContent = if ((start != -1) && (end != -1) && (start < end)) {
            input.substring(start, end + 1)
        } else {
            input.trim()
        }
        return repairJson(jsonContent)
    }

    /**
     * Applies simple repairs for common LLM JSON mistakes, like missing commas.
     */
    private fun repairJson(input: String): String {
        return input
            .replace(Regex("""(?<=[}\]])\s+(?=[{\[])"""), ", ")
            .replace(Regex("""(?<=")\s+(?=")"""), ", ")
    }

    /**
     * Sanitizes input to remove sensitive system identifiers that might trigger safety blocks.
     */
    private fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[0-9a-fA-F]{8,}-?"), "[ID]") // Remove potential UUIDs or long hex IDs
            .replace(Regex("#\\d+ SMP PREEMPT.*"), "[KERNEL_DETAILS]") // Remove specific kernel build times
            .replace(Regex("build/[0-9a-zA-Z.]+"), "[BUILD_PATH]") // Remove internal paths
            .replace(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"), "[DATE]") // Remove specific dates
            .trim()
    }
}

/**
 * Structured response for code audit results.
 */
@Serializable
data class AuditResponse(
    val cyclomaticComplexity: Int,
    val vulnerabilities: List<String>,
    val refactoredSolution: String,
    val modelVariant: String = "Unknown"
)
