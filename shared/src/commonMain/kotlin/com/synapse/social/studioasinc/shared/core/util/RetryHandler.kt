package com.synapse.social.studioasinc.shared.core.util

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

object RetryHandler {
    data class RetryConfig(
        val maxAttempts: Int = 3,
        val initialDelayMs: Long = 1000L,
        val maxDelayMs: Long = 4000L,
        val exponentialBase: Double = 2.0
    )

    sealed class RetryResult<T> {
        data class Success<T>(val value: T, val attemptNumber: Int) : RetryResult<T>()
        data class Failure<T>(
            val exception: Throwable,
            val attemptNumber: Int,
            val isRetryable: Boolean
        ) : RetryResult<T>()
    }

    suspend fun <T> executeWithRetry(
        config: RetryConfig = RetryConfig(),
        operation: suspend (attemptNumber: Int) -> T
    ): RetryResult<T> {
        var currentAttempt = 1
        var lastException: Throwable? = null

        while (currentAttempt <= config.maxAttempts) {
            try {
                val result = operation(currentAttempt)
                return RetryResult.Success(result, currentAttempt)
            } catch (e: Exception) {
                lastException = e
                
                if (currentAttempt == config.maxAttempts || !isRetryableException(e)) {
                    return RetryResult.Failure(e, currentAttempt, isRetryableException(e))
                }

                val delayMs = calculateBackoffDelay(currentAttempt, config)
                delay(delayMs)
                currentAttempt++
            }
        }

        return RetryResult.Failure(
            lastException ?: Exception("Unknown error"),
            currentAttempt,
            false
        )
    }

    suspend fun <T> executeWithRetryResult(
        config: RetryConfig = RetryConfig(),
        operation: suspend (attemptNumber: Int) -> Result<T>
    ): RetryResult<T> {
        return executeWithRetry(config) { attempt ->
            operation(attempt).getOrThrow()
        }
    }

    private fun isRetryableException(exception: Throwable): Boolean {
        return when (exception) {
            is kotlinx.coroutines.TimeoutCancellationException -> true
            else -> exception.message?.contains("network", ignoreCase = true) == true ||
                    exception.message?.contains("timeout", ignoreCase = true) == true ||
                    exception.message?.contains("connection", ignoreCase = true) == true
        }
    }

    private fun calculateBackoffDelay(attempt: Int, config: RetryConfig): Long {
        val exponentialDelay = config.initialDelayMs * config.exponentialBase.pow(attempt - 1).toLong()
        return min(exponentialDelay, config.maxDelayMs)
    }

    fun shouldRetry(exception: Throwable, attemptNumber: Int, maxAttempts: Int): Boolean {
        return attemptNumber < maxAttempts && isRetryableException(exception)
    }

    fun getRetryMessage(attemptNumber: Int, maxAttempts: Int): String {
        return "Retrying... (attempt $attemptNumber of $maxAttempts)"
    }
}
