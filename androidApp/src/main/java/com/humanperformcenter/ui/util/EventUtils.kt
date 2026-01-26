package com.humanperformcenter.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun createICSFile(eventTitle: String, startDateTime: Instant, durationMinutes: Int = 60): String {
    // 1. Definimos el formato exacto que requiere el archivo .ics (yyyyMMddTHHmmss)
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    val zoneId = ZoneId.systemDefault()

    // 2. Calculamos el instante final sumando los minutos
    val endDateTime = startDateTime.plus(durationMinutes.toLong(), ChronoUnit.MINUTES)

    // 3. Formateamos a String aplicando la zona horaria del usuario
    // .atZone convierte el Instant (UTC) a ZonedDateTime con la hora local
    val dtStart = formatter.format(startDateTime.atZone(zoneId))
    val dtEnd = formatter.format(endDateTime.atZone(zoneId))

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

// Esta función se mantiene igual, ya usa librerías estándar de Android
fun shareICS(context: Context, fileContent: String, fileName: String = "evento.ics") {
    try {
        val file = File(context.cacheDir, fileName)
        file.writeText(fileContent)

        // Asegúrate de tener configurado el FileProvider en el AndroidManifest
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Debe coincidir con authorities en Manifest
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Añadir al calendario"))
    } catch (e: Exception) {
        e.printStackTrace()
        // Aquí podrías loguear el error o mostrar un Toast si falla la escritura/intent
    }
}