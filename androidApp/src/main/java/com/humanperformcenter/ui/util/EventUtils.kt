package com.humanperformcenter.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun createICSFile(eventTitle: String, startDateTime: Instant, durationMinutes: Int = 60): String {
    val start = startDateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    // start is LocalDateTime here, so we use plus(DateTimePeriod)
    val endInstant = startDateTime.plus(durationMinutes.toLong() * 60, DateTimeUnit.SECOND)
    val end = endInstant.toLocalDateTime(TimeZone.currentSystemDefault())

    val dtStart = "%04d%02d%02dT%02d%02d%02d".format(
        start.year, start.month.number, start.day,
        start.hour, start.minute, start.second
    )
    val dtEnd = "%04d%02d%02dT%02d%02d%02d".format(
        end.year, end.month.number, end.day,
        end.hour, end.minute, end.second
    )

    return """
        BEGIN:VCALENDAR
        VERSION:2.0
        PRODID:-//HumanPerformApp//ES
        BEGIN:VEVENT
        SUMMARY:$eventTitle
        DTSTART:$dtStart
        DTEND:$dtEnd
        END:VEVENT
        END:VCALENDAR
    """.trimIndent()
}


fun shareICS(context: Context, fileContent: String, fileName: String = "evento.ics") {
    val file = File(context.cacheDir, fileName)
    file.writeText(fileContent)

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/calendar"
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(Intent.createChooser(intent, "Añadir al calendario"))
}