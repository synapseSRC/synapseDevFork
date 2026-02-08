package com.synapse.social.studioasinc.feature.auth.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsernameValidatorTest {

    @Test
    fun `validate returns Valid for valid username`() {
        val validUsernames = listOf("user", "user1", "user_1", "valid_user_name", "USER123")
        validUsernames.forEach { username ->
            val result = UsernameValidator.validate(username)
            assertTrue("Expected valid result for username: $username", result is UsernameValidator.ValidationResult.Valid)
        }
    }

    @Test
    fun `validate returns Error for empty username`() {
        val result = UsernameValidator.validate("")
        assertTrue(result is UsernameValidator.ValidationResult.Error)
        assertEquals("Username is required", (result as UsernameValidator.ValidationResult.Error).message)
    }

    @Test
    fun `validate returns Error for username shorter than 3 characters`() {
        val invalidUsernames = listOf("us", "u")
        invalidUsernames.forEach { username ->
            val result = UsernameValidator.validate(username)
            assertTrue("Expected error result for username: $username", result is UsernameValidator.ValidationResult.Error)
            assertEquals("Username must be at least 3 characters", (result as UsernameValidator.ValidationResult.Error).message)
        }
    }

    @Test
    fun `validate returns Valid for username with exactly 3 characters`() {
        val username = "usr"
        val result = UsernameValidator.validate(username)
        assertTrue(result is UsernameValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate returns Error for username longer than 20 characters`() {
        val username = "this_username_is_too_long_for_validation"
        val result = UsernameValidator.validate(username)
        assertTrue(result is UsernameValidator.ValidationResult.Error)
        assertEquals("Username must be at most 20 characters", (result as UsernameValidator.ValidationResult.Error).message)
    }

    @Test
    fun `validate returns Valid for username with exactly 20 characters`() {
        val username = "valid_username_20cha"
        val result = UsernameValidator.validate(username)
        assertTrue(result is UsernameValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate returns Error for username with invalid characters`() {
        val invalidUsernames = listOf("user name", "user-name", "user@name", "user!", "user#", "user$")
        invalidUsernames.forEach { username ->
            val result = UsernameValidator.validate(username)
            assertTrue("Expected error result for username: $username", result is UsernameValidator.ValidationResult.Error)
            assertEquals("Username can only contain letters, numbers, and underscores", (result as UsernameValidator.ValidationResult.Error).message)
        }
    }
}
