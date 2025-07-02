package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserBooking(
    val id: Int,
    val date: String,
    val hour: String,
    val service: String,
    val coach_name: String,
    val coach_profile_pic: String?
)