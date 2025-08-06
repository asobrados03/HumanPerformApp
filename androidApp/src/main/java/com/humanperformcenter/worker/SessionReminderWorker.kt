package com.humanperformcenter.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters


class SessionReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val service = inputData.getString("service") ?: return Result.failure()
        val coach = inputData.getString("coach") ?: "Coach"
        val date = inputData.getString("date") ?: return Result.failure()
        val hour = inputData.getString("hour") ?: return Result.failure()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "session_reminder",
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "session_reminder")
            .setContentTitle("⏰ ¡Tienes una sesión en 1 hora!")
            .setContentText("Servicio: $service con $coach a las $hour")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)

        return Result.success()
    }
}
