package com.humanperformcenter.shared.presentation.ui.models

import kotlinx.datetime.LocalDate

data class ProcessedBooking(
    val primaryId: Int?,
    val date: LocalDate
)