package com.example.intellidash

import android.content.Context

/**
 * A factory that provides instances of classes containing AppFunctions.
 *
 * This class follows the pattern of a factory to instantiate AppFunction enclosing classes
 * with necessary dependencies (like Context for AICoreClient).
 */
class LocalContextAppFunctionFactory(private val context: Context) {
    /**
     * Creates an instance of the specified [enclosingClass].
     *
     * @param enclosingClass The class to instantiate.
     * @return An instance of the class, or null if the class is not supported by this factory.
     */
    fun <T : Any> createEnclosingClass(enclosingClass: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return when (enclosingClass) {
            AuditFunctions::class.java -> AuditFunctions(context) as T
            else -> null
        }
    }
}
