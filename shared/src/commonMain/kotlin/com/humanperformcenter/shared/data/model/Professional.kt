package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Professional(
    val id: String,
    val name: String,
    val type: ProfessionalType,
    val photoUrl: String? = null
)