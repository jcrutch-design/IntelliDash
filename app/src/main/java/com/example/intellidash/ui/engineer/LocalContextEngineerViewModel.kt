package com.example.intellidash.ui.engineer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.intellidash.AICoreClient
import com.example.intellidash.AuditResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Local Context Engineer screen.
 * Manages the state of code audits and AICore status.
 */
class LocalContextEngineerViewModel(application: Application) : AndroidViewModel(application) {

    private val aiCoreClient = AICoreClient(application)

    private val _uiState = MutableStateFlow(EngineerUiState())
    val uiState: StateFlow<EngineerUiState> = _uiState.asStateFlow()

    init {
        checkAiCoreStatus()
    }

    private fun checkAiCoreStatus() {
        // In a real implementation, we would use FirebaseAIOnDevice.checkStatus()
        // or a similar ML Kit API. For now, we simulate the status check.
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(aiCoreStatus = AiCoreStatus.CHECKING)
            // Simulating a check
            kotlinx.coroutines.delay(1000)
            _uiState.value = _uiState.value.copy(aiCoreStatus = AiCoreStatus.AVAILABLE)
        }
    }

    fun onCodeChanged(newCode: String) {
        _uiState.value = _uiState.value.copy(codeSnippet = newCode)
    }

    fun runAudit() {
        val currentCode = _uiState.value.codeSnippet
        if (currentCode.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuditing = true, auditError = null)
            try {
                val response = aiCoreClient.auditCodeSnippet("snippet.kt", currentCode)
                _uiState.value = _uiState.value.copy(
                    isAuditing = false,
                    auditResponse = response,
                    currentModelVariant = response.modelVariant,
                    auditHistory = _uiState.value.auditHistory + AuditRecord(
                        fileName = "snippet.kt",
                        timestamp = System.currentTimeMillis(),
                        response = response
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAuditing = false,
                    auditError = e.message ?: "Unknown error occurred during audit"
                )
            }
        }
    }

    fun selectAuditRecord(record: AuditRecord) {
        _uiState.value = _uiState.value.copy(
            selectedRecord = record,
            auditResponse = record.response,
            codeSnippet = "" // Optionally clear or set to something else
        )
    }

    fun runSystemAudit() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSystemAuditing = true, systemAuditError = null)
            try {
                val systemInfo = readSystemInfo()
                val response = aiCoreClient.auditSystemSecurity(systemInfo)
                _uiState.value = _uiState.value.copy(
                    isSystemAuditing = false,
                    systemAuditResponse = response,
                    currentModelVariant = response.modelVariant
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSystemAuditing = false,
                    systemAuditError = e.message ?: "System audit failed"
                )
            }
        }
    }

    private suspend fun readSystemInfo(): String = withContext(Dispatchers.IO) {
        try {
            // Attempt to read sensitive system files using root access
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /proc/version; getenforce; getprop ro.build.fingerprint"))
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // Fallback for non-rooted devices
            "Kernel: ${System.getProperty("os.version")}\nFingerprint: ${android.os.Build.FINGERPRINT}\nSELinux: Unknown (Root Required)"
        }
    }
}

data class EngineerUiState(
    val codeSnippet: String = "",
    val isAuditing: Boolean = false,
    val auditResponse: AuditResponse? = null,
    val auditError: String? = null,
    val aiCoreStatus: AiCoreStatus = AiCoreStatus.INITIALIZING,
    val auditHistory: List<AuditRecord> = emptyList(),
    val selectedRecord: AuditRecord? = null,
    val isSystemAuditing: Boolean = false,
    val systemAuditResponse: AuditResponse? = null,
    val systemAuditError: String? = null,
    val currentModelVariant: String? = null
)

data class AuditRecord(
    val fileName: String,
    val timestamp: Long,
    val response: AuditResponse
)

enum class AiCoreStatus {
    INITIALIZING,
    CHECKING,
    AVAILABLE,
    UNAVAILABLE,
    DOWNLOADING
}
