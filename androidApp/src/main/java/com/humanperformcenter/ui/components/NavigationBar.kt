package com.humanperformcenter.ui.components

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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.humanperformcenter.R
import com.humanperformcenter.app.navigation.Calendar
import com.humanperformcenter.app.navigation.NavItem
import com.humanperformcenter.app.navigation.Stats
import com.humanperformcenter.app.navigation.Service
import com.humanperformcenter.app.navigation.User

@Composable
fun NavigationBar(navController: NavController) {
    // Defino mis items con la KClass de cada destino:
    val items = listOf(
        NavItem(Service::class, "Producto", R.drawable.exercise),
        NavItem(Calendar::class, "Calendario",   R.drawable.calendar),
        NavItem(Stats::class,    "Estadísticas", R.drawable.stats),
        NavItem(User::class,     "Usuario",      R.drawable.person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor   = MaterialTheme.colorScheme.primary
    ) {
        // Obtengo la ruta actual (p. ej. "com.humanperformcenter.app.navigation.User")
        val currentRoute = navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

        items.forEach { item ->
            // Extraigo el nombre de la ruta de la KClass
            val routeName = item.route.qualifiedName!!

            NavigationBarItem(
                icon = {
                    Icon(
                        painter            = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier           = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text      = item.title,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(top = 4.dp)
                    )
                },
                selected = (currentRoute == routeName),
                onClick  = {
                    // Evito navegar si ya estoy en esa misma ruta:
                    if (currentRoute != routeName) {
                        navController.navigate(routeName) {
                            // Vuelve al destino raíz del NavHost (ideal para bottom nav)
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // No apile rutas duplicadas, reutiliza si existe
                            launchSingleTop = true
                            // Restaura estado (scroll, formulario, etc.) si ya estaba en backStack
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = true,
                enabled         = true
            )
        }
    }
}