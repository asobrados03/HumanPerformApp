package es.uva.sg.psm.humanperformcenter

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
fun SetStatusBarColor(
    statusBarColor: Color,
    navigationBarColor: Color
) {
    val context = LocalContext.current as ComponentActivity

    DisposableEffect(Unit) {
        context.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark( // Íconos blancos en la barra de estado
                statusBarColor.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.dark( // Íconos blancos en la barra de navegación
                navigationBarColor.toArgb()
            )
        )

        onDispose { }
    }
}
