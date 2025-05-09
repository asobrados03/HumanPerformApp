package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)