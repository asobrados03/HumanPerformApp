package com.humanperformcenter.shared

import android.content.Context
import androidx.work.WorkManager

class AndroidSessionNotificationManager(private val context: Context) : SessionNotificationManager {
    override fun cancelNotification(bookingId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("session_$bookingId")
    }
}