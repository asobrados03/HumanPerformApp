package com.humanperformcenter.shared.presentation.ui.models

import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.network.API_BASE_URL

/**
 * Modelo optimizado para la Vista (HireView).
 * Contiene toda la info del servicio + el estado calculado de si ya está contratado.
 */
data class ServiceUiModel(
    val service: ServiceAvailable,
    val isHired: Boolean
) {
    // 💡 EXTRA: Movemos la lógica de formateo de URL aquí.
    // Así la UI no tiene que saber nada de "API_BASE_URL".
    val fullImageUrl: String?
        get() = service.image?.let { "$API_BASE_URL/service_images/$it" }
}