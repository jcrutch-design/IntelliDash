package com.example.intellidash

/**
 * A factory that provides instances of classes containing AppFunctions.
 */
class LocalContextAppFunctionFactory {
    /**
     * Creates an instance of the specified [enclosingClass].
     *
     * @param enclosingClass The class to instantiate.
     * @return An instance of the class, or null if the class is not supported by this factory.
     */
    fun <T : Any> createEnclosingClass(enclosingClass: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return when (enclosingClass) {
            AuditFunctions::class.java -> AuditFunctions() as T
            else -> null
        }
    }
}
