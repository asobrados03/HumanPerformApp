package com.humanperformcenter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.humanperformcenter.R

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Aquí podrías dibujar círculos de fondo como en tu diseño (opcional)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.colored_logo),
                    contentDescription = "Logo Human Perform",
                    modifier = Modifier.size(300.dp)
                )

                // Botón Registro
                GradientButton(
                    text = "Registro",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF6D2A6F), Color(0xFFEF0E29))
                    ),
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height( FiftyDp ) // por ejemplo 50.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Acceso
                GradientButton(
                    text = "Acceso",
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF6D2A6F), Color(0xFFEF0E29))
                    ),
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height( FiftyDp )
                )
            }
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),  // eliminamos padding interno para controlar todo via Box
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(50))
                .fillMaxSize()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White
            )
        }
    }
}

// Para mantener el código más legible, define un alias:
private val FiftyDp = 50.dp
