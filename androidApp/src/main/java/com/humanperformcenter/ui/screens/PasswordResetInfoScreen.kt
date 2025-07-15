package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.LogoAppBar

@Composable
fun PasswordResetInfoScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Revisa la bandeja de entrada de tu correo electrónico, allí habrás recibido una nueva contraseña."
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Una vez hayas iniciado sesión, tendrás que cambiar la contraseña generada automáticamente por la tuya personal."
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Para cambiar la contraseña dirígete a:")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Usuario > Configuración > Cambiar contraseña",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
