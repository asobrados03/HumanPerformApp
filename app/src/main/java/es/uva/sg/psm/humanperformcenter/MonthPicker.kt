package es.uva.sg.psm.humanperformcenter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.uva.sg.psm.planificadorfinanciero.R

@Composable
fun MonthPicker(
    visible: Boolean,
    currentMonth: Int, // Este es un índice (1-12)
    currentYear: Int,
    confirmButtonCLicked: (Int, Int) -> Unit, // Espera un mes (índice) y un año
    cancelClicked: () -> Unit
) {

    val months = listOf(
        "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
        "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
    )

    var selectedMonthIndex by remember { mutableIntStateOf(currentMonth - 1) } // Indexamos desde 0
    var year by remember { mutableIntStateOf(currentYear) }

    if (visible) {
        AlertDialog(
            backgroundColor = colorResource(R.color.blue_white),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp), // Limitar la altura máxima
            title = {},
            text = {
                Column(
                    modifier = Modifier.padding(16.dp) // Añadir algo de padding para evitar que el contenido quede pegado a los bordes
                ) {
                    // Selección de año
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { year-- },
                            modifier = Modifier.rotate(90f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(35.dp)
                            )
                        }

                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            text = year.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { year++ },
                            modifier = Modifier.rotate(-90f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }

                    // Selección de mes
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // Limitar a 3 columnas por fila
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp)
                    ) {
                        items(months) { monthName ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable {
                                        // Asignamos el índice del mes en lugar del nombre
                                        selectedMonthIndex = months.indexOf(monthName)
                                    }
                                    .background(
                                        color = if (selectedMonthIndex == months.indexOf(monthName)) colorResource(R.color.blue_dark) else colorResource(R.color.blue_white),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthName,
                                    color = if (selectedMonthIndex == months.indexOf(monthName)) colorResource(R.color.blue_white) else colorResource(R.color.bold_from_palette),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, bottom = 20.dp), // Reducir el espacio al fondo para que los botones no se vean desproporcionados
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { cancelClicked() },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, color = colorResource(R.color.blue_transparent)),
                        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = colorResource(R.color.blue_transparent)),
                        modifier = Modifier.padding(end = 20.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = colorResource(R.color.bold_from_palette),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            // Pasa el índice del mes seleccionado y el año
                            confirmButtonCLicked(selectedMonthIndex + 1, year)
                        },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, color = colorResource(R.color.blue_dark)),
                        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = colorResource(R.color.blue_transparent)),
                        modifier = Modifier.padding(end = 20.dp)
                    ) {
                        Text(
                            text = "OK",
                            color = colorResource(R.color.blue_dark),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            onDismissRequest = {}
        )
    }
}