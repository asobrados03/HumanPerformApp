package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: Long = 0L,
    val category: Long = 0L,
    val monthlyLimit: Double = 0.0,
    val currentExpenditure: Double = 0.0,
    val month: String = "0", // 01: Enero, 02: Febrero, ... ,y 12: Diciembre
    val year: Int = 0
)