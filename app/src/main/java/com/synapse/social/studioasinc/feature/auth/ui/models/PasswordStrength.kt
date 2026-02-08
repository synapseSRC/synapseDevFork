package com.synapse.social.studioasinc.feature.auth.ui.models

import androidx.compose.ui.graphics.Color



sealed class PasswordStrength(
    val progress: Float,
    val label: String,
    val color: Color
) {


    object Weak : PasswordStrength(
        progress = 0.33f,
        label = "Weak",
        color = Color.Red
    )



    object Fair : PasswordStrength(
        progress = 0.66f,
        label = "Fair",
        color = Color(0xFFFFA500)
    )



    object Strong : PasswordStrength(
        progress = 1.0f,
        label = "Strong",
        color = Color.Green
    )
}
