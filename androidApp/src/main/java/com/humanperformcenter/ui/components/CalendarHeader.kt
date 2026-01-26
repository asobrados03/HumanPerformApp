package com.humanperformcenter.ui.components

import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CalendarHeader(
    displayedMonth: Month,
    displayedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthName = remember(displayedMonth) {
        displayedMonth.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"))
            .replaceFirstChar { it.titlecase() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardDoubleArrowLeft,
            contentDescription = "Mes anterior",
            tint = Color.Gray,
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onPreviousMonth)
                .padding(4.dp)
        )

        Text(
            text = "$monthName $displayedYear",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Icon(
            imageVector = Icons.Default.KeyboardDoubleArrowRight,
            contentDescription = "Mes siguiente",
            tint = Color.Gray,
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onNextMonth)
                .padding(4.dp)
        )
    }
}