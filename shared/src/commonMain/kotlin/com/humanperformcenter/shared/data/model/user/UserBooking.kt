package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserBooking(
    val id: Int,
    val date: String,
    val hour: String,
    val service: String,
    @SerialName("service_id")
    val serviceId: Int?,
    @SerialName("product_id")
    val productId: Int?,
    @SerialName("coach_name")
    val coachName: String,
    @SerialName("coach_profile_pic")
    val coachProfilePic: String?
)