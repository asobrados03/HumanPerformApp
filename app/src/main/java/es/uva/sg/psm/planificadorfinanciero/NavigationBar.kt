package es.uva.sg.psm.planificadorfinanciero

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavigationBar(navController: NavController) {
    val items = listOf(
        NavItem(Screen.DashboardScreen.route, "Producto", R.drawable.exercise),
        NavItem(Screen.HistoryScreen.route, "Calendario", R.drawable.calendar),
        NavItem(Screen.StaticsScreen.route, "Blog", R.drawable.stories),
        NavItem(Screen.BudgetScreen.route, "Usuario", R.drawable.person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    // Cargar el recurso usando painterResource y ajustar el tamaño
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(32.dp) // Ajusta el tamaño
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                // No override de color aquí para que el color global se aplique
                alwaysShowLabel = true,
                enabled = true
            )
        }
    }
}