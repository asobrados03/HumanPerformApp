package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EstadisticasUsuario(
    val entrenamientosMesPasado: Int = 0,
    val entrenadorMasUsado: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)