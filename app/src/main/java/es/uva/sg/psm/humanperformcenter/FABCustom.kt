package es.uva.sg.psm.humanperformcenter

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun FABCustom(navController: NavController) {
    FloatingActionButton(
        modifier = Modifier.padding(all = 20.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        onClick = {
            navController.navigate(Screen.AddEditTransactionScreen.route + "/0L")
        }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Añadir transacciones"
        )
    }
}