package com.example.intellidash

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction

/**
 * Provides AppFunctions for auditing code snippets using local AI capabilities.
 */
class AuditFunctions(private val context: android.content.Context) {
    private val aiCoreClient = AICoreClient(context)

    /**
     * Audits a code snippet to identify cyclomatic complexity, security vulnerabilities, and provide a refactored solution.
     *
     * This function leverages on-device AI (Gemini Nano via AICore) to perform a deep analysis of the provided source code.
     * It evaluates the logical complexity and searches for common security pitfalls, returning a structured result
     * that includes a suggested improvement.
     *
     * @param context The [AppFunctionContext] in which the function is executed.
     * @param fileName The name of the source file containing the code snippet.
     * @param rawCodeSnippet The raw source code text to be audited.
     * @return An [AuditResult] containing the analysis results and refactored code.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun auditCodeSnippet(
        context: AppFunctionContext,
        fileName: String,
        rawCodeSnippet: String
    ): AuditResult {
        val response = aiCoreClient.auditCodeSnippet(fileName, rawCodeSnippet)
        return AuditResult(
            cyclomaticComplexity = response.cyclomaticComplexity,
            vulnerabilities = response.vulnerabilities,
            refactoredSolution = response.refactoredSolution
        )
    }
}

/**
 * Represents the structured result of a code audit.
 *
 * @property cyclomaticComplexity The calculated cyclomatic complexity of the code snippet.
 * @property vulnerabilities A list of identified security vulnerabilities or code smells.
 * @property refactoredSolution A suggested refactored version of the code that addresses the identified issues.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AuditResult(
    val cyclomaticComplexity: Int,
    val vulnerabilities: List<String>,
    val refactoredSolution: String
)
