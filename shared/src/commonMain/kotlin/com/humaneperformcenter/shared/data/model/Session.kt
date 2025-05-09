package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: Long = 0L,

    val service: String = "",

    val product: String = "",

    val date: Long = 0L,

    val hour: String = "",

    val professional: String = ""
)