package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserBooking(
    val id: Int,
    val date: String,
    val hour: String,
    val service: String,
    val product: String,
    @SerialName("service_id")
    val serviceId: Int? = null,
    @SerialName("product_id")
    val productId: Int? = null,
    @SerialName("coach_name")
    val coachName: String? = null,
    @SerialName("coach_profile_pic")
    val coachProfilePic: String? = null,
)
