package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class EwalletResponse(
    val transactions: List<EwalletTransaction>
)