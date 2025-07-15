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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.humanperformcenter.R
import com.humanperformcenter.app.navigation.Calendar
import com.humanperformcenter.app.navigation.NavItem
import com.humanperformcenter.app.navigation.Stats
import com.humanperformcenter.app.navigation.NewProduct
import com.humanperformcenter.app.navigation.User

@Composable
fun NavigationBar(navController: NavController) {
    val items = listOf(
        NavItem(NewProduct, "Producto",  R.drawable.exercise),
        NavItem(Calendar,   "Calendario", R.drawable.calendar),
        NavItem(Stats,      "Estadísticas",R.drawable.stories),
        NavItem(User,       "Usuario",    R.drawable.person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor   = MaterialTheme.colorScheme.primary
    ) {
        val currentRoute = navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

        items.forEach { item ->
            // En lugar de convertir a string, comparamos con el objeto serializado
            val routeString = item.route::class.qualifiedName

            NavigationBarItem(
                icon = {
                    Icon(
                        painter           = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier          = Modifier.size(32.dp)
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
                // Verificamos si la ruta actual coincide con el tipo de clase
                selected = currentRoute?.contains(routeString ?: "") == true,
                onClick  = {
                    // Navegamos usando el objeto de ruta directamente, no el string
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                alwaysShowLabel = true,
                enabled         = true
            )
        }
    }
}