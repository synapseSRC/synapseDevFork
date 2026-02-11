package com.synapse.social.studioasinc.shared.domain.usecase.auth

import com.synapse.social.studioasinc.shared.domain.model.ValidationResult
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidateEmailUseCaseTest {

    private val validateEmail = ValidateEmailUseCase()

    @Test
    fun testValidEmail() {
        val result = validateEmail("test@example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun testInvalidEmail() {
        val result = validateEmail("invalid-email")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun testEmptyEmail() {
        val result = validateEmail("")
        assertTrue(result is ValidationResult.Invalid)
    }
}
