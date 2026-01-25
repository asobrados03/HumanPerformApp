package com.humanperformcenter.shared.presentation.ui

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse(
    val success: Boolean,
    val error: String? = null
)
