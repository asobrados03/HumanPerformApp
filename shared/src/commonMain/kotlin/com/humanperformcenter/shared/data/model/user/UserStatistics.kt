package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStatistics(
    @SerialName("last_month_workouts")
    val lastMonthWorkouts: Int = 0,
    @SerialName("most_frequent_trainer")
    val mostFrequentTrainer: String? = null,
    @SerialName("pending_bookings")
    val pendingBookings: Int = 0
)