package com.humanperformcenter.worker

import android.content.Context
import androidx.work.*
import com.humanperformcenter.shared.data.model.user.UserBooking
import java.time.*
import java.util.concurrent.TimeUnit

fun scheduleSessionNotification(context: Context, booking: UserBooking) {
    try {
        // Convertir fecha ISO (ej. 2025-06-03T00:00:00.000Z) a LocalDate
        val datePart = Instant.parse(booking.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Convertir la hora (ej. 18:00:00) a LocalTime
        val timePart = LocalTime.parse(booking.hour)

        // Combinar fecha y hora
        val sessionDateTime = LocalDateTime.of(datePart, timePart)

        val now = LocalDateTime.now()
        val notifyTime = sessionDateTime
            .minusHours(1)
            .minusMinutes(0)

        if (notifyTime.isAfter(now)) {
            val delay = Duration.between(now, notifyTime).toMillis()

            val workRequest = OneTimeWorkRequestBuilder<SessionReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        "service" to booking.service,
                        "coach" to booking.coachName,
                        "date" to booking.date,
                        "hour" to booking.hour
                    )
                )
                .addTag("session_${booking.id}") // Útil para cancelar luego
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
