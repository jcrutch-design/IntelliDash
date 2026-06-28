package com.example.intellidash.ui.engineer

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation keys for the Engineer feature.
 */
@Serializable
sealed interface EngineerKey : NavKey {
    @Serializable
    data object Dashboard : EngineerKey
    
    @Serializable
    data class AuditDetail(val timestamp: Long) : EngineerKey
}

