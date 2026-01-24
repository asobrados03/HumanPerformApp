package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Professional(
    val id: Int,
    val name: String,
    @SerialName("profile_photo")
    val photoName: String? = null,
    val service: String? = null
)
