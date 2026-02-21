package com.synapse.social.studioasinc.shared.core.util

object ErrorHandler {
    enum class ErrorType {
        REPLY,
        FORWARD,
        EDIT,
        DELETE,
        AI_SUMMARY,
        NETWORK,
        GENERIC
    }

    fun getErrorMessage(
        errorType: ErrorType,
        exception: Throwable,
        messageId: String? = null,
        userId: String? = null
    ): String {
        logError(errorType, exception, messageId, userId)

        return when (errorType) {
            ErrorType.REPLY -> getReplyErrorMessage(exception)
            ErrorType.FORWARD -> getForwardErrorMessage(exception)
            ErrorType.EDIT -> getEditErrorMessage(exception)
            ErrorType.DELETE -> getDeleteErrorMessage(exception)
            ErrorType.AI_SUMMARY -> getAISummaryErrorMessage(exception)
            ErrorType.NETWORK -> getNetworkErrorMessage(exception)
            ErrorType.GENERIC -> getGenericErrorMessage(exception)
        }
    }

    private fun getReplyErrorMessage(exception: Throwable): String {
        return when {
            isNetworkError(exception) -> "Network error. Please check your connection and try again."
            isRateLimitError(exception) -> {
                val minutes = extractMinutesFromRateLimitError(exception)
                "Rate limit exceeded. Please wait $minutes minutes before replying again."
            }
            else -> "Failed to send reply. Please try again."
        }
    }

    private fun getForwardErrorMessage(exception: Throwable): String {
        return when {
            isNetworkError(exception) -> "Network error. Please check your connection and try again."
            else -> "Failed to forward message. Please try again."
        }
    }

    private fun getEditErrorMessage(exception: Throwable): String {
        return when {
            isNetworkError(exception) -> "Network error. Please check your connection and try again."
            else -> "Failed to edit message. Please try again."
        }
    }

    private fun getDeleteErrorMessage(exception: Throwable): String {
        return when {
            isNetworkError(exception) -> "Network error. Please check your connection and try again."
            else -> "Failed to delete message. Please try again."
        }
    }

    private fun getAISummaryErrorMessage(exception: Throwable): String {
        return when {
            isNetworkError(exception) -> "Network error. Please check your connection and try again."
            else -> "Failed to generate AI summary. Please try again."
        }
    }

    private fun getNetworkErrorMessage(exception: Throwable): String {
        return "Network connection error. Please check your internet connection and try again."
    }

    private fun getGenericErrorMessage(exception: Throwable): String {
        return exception.message ?: "An unexpected error occurred. Please try again."
    }

    fun isNetworkError(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("network") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("unreachable")
    }

    fun isRateLimitError(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("rate limit") ||
                message.contains("too many requests") ||
                message.contains("429")
    }

    fun isRetryableError(exception: Throwable): Boolean {
        return isNetworkError(exception) && !isRateLimitError(exception)
    }

    private fun extractMinutesFromRateLimitError(exception: Throwable): Int {
        val message = exception.message ?: ""
        val regex = Regex("""(\d+)\s*minutes?""")
        val match = regex.find(message)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 5
    }

    private fun logError(
        errorType: ErrorType,
        exception: Throwable,
        messageId: String?,
        userId: String?
    ) {
        println("ErrorHandler: $errorType - ${exception.message}")
        if (messageId != null) println("Message ID: $messageId")
        if (userId != null) println("User ID: $userId")
    }
}
