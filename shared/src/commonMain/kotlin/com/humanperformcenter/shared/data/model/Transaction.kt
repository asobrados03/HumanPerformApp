package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long = 0L,
    val type: TransactionType = TransactionType.Ingreso,
    val amount: Double = 0.0,
    val category: Long = 0L,
    val date: Long = 0L,
    val description: String = ""
)