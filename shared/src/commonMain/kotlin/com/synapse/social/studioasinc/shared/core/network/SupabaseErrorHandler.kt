package com.synapse.social.studioasinc.shared.core.network

import io.github.aakira.napier.Napier

object SupabaseErrorHandler {

    fun mapExceptionToMessage(e: Exception): String {
        val message = e.message
        return when {
            message?.contains("relation \"users\" does not exist", ignoreCase = true) == true ->
                "Database table 'users' does not exist. Please create the users table in your Supabase database."
            message?.contains("connection", ignoreCase = true) == true ->
                "Cannot connect to Supabase. Check your internet connection and Supabase configuration."
            message?.contains("unauthorized", ignoreCase = true) == true ->
                "Unauthorized access to Supabase. Check your API key and RLS policies."
            else -> "Database error: ${e.message}"
        }
    }

    fun <T> toResult(e: Exception, logTag: String, logMessage: String): Result<T> {
        Napier.e(message = logMessage, throwable = e, tag = logTag)
        val userFriendlyMessage = mapExceptionToMessage(e)
        return Result.failure(Exception(userFriendlyMessage, e))
    }
}
