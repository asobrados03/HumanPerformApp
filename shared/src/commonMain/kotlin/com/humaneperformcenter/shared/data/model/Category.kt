package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long = 0L,
    val name: String,
    val type: TransactionType
)