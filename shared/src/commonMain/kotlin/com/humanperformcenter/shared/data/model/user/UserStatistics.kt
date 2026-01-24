package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserStatistics(
    val entrenamientosMesPasado: Int = 0,
    val entrenadorMasUsado: String? = null,
    val reservasPendientes: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)