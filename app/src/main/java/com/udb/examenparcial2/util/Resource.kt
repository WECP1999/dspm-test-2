package com.udb.examenparcial2.util

/**
 * A generic wrapper that encapsulates the result of a repository operation,
 * including its loading state.
 *
 * Usage in a ViewModel:
 * ```
 * when (result) {
 *     is Resource.Success -> // use result.data
 *     is Resource.Error   -> // show result.message
 *     is Resource.Loading -> // show a progress indicator
 * }
 * ```
 */
sealed class Resource<T> {

    /** The operation completed successfully. [data] holds the result. */
    data class Success<T>(val data: T) : Resource<T>()

    /** The operation failed. [message] is a human-readable description; [exception] is optional. */
    data class Error<T>(
        val message: String,
        val exception: Exception? = null
    ) : Resource<T>()

    /** The operation is in progress. */
    class Loading<T> : Resource<T>()
}
