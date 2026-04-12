package com.humanperformcenter.ui.components.hire_product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter

@Composable
fun ProductFiltersSection(
    selectedFilter: ProductTypeFilter,
    onFilterChange: (ProductTypeFilter) -> Unit,
    selectedSessionCount: Int,
    onSessionChange: (Int) -> Unit,
    availableSessions: List<Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Dropdown Tipo
        CustomDropdown(
            label = selectedFilter.label,
            modifier = Modifier.weight(1f)
        ) { closeMenu ->
            ProductTypeFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = { onFilterChange(filter); closeMenu() }
                )
            }
        }

        // Dropdown Sesiones
        val sessionLabel = when (selectedSessionCount) {
            0 -> "Todas las sesiones"
            1 -> "1 sesión"
            else -> "$selectedSessionCount sesiones"
        }

        CustomDropdown(
            label = sessionLabel,
            modifier = Modifier.weight(1f)
        ) { closeMenu ->
            DropdownMenuItem(
                text = { Text("Todas las sesiones") },
                onClick = { onSessionChange(0); closeMenu() }
            )
            availableSessions.forEach { session ->
                DropdownMenuItem(
                    text = { Text(if (session == 1) "1 sesión" else "$session sesiones") },
                    onClick = { onSessionChange(session); closeMenu() }
                )
            }
        }
    }
}