package com.example.intellidash.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Main navigation keys for IntelliDash.
 */
sealed interface IntelliDashKey : NavKey {
    @Serializable
    data object Home : IntelliDashKey

    @Serializable
    data object ContextAuditor : IntelliDashKey

    @Serializable
    data object MediaIndexer : IntelliDashKey
}
