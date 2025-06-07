package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.ui.components.LogoAppBar

@Composable
fun MyProfileScreen(
    user: LoginResponse,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Título de la pantalla
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                ProfileInfoCard(
                    title = "Información Personal",
                    content = {
                        ProfileInfoItem(
                            label = "Nombre completo",
                            value = user.fullName
                        )

                        ProfileInfoItem(
                            label = "Correo electrónico",
                            value = user.email
                        )

                        ProfileInfoItem(
                            label = "Teléfono",
                            value = user.phone
                        )

                        ProfileInfoItem(
                            label = "Sexo",
                            value = formatSex(user.sex)
                        )

                        ProfileInfoItem(
                            label = "Fecha de nacimiento",
                            value = formatDate(user.dateOfBirth)
                        )

                        user.dni?.let { dni ->
                            ProfileInfoItem(
                                label = "DNI",
                                value = dni
                            )
                        }

                        user.postcode?.let { postcode ->
                            ProfileInfoItem(
                                label = "Código postal",
                                value = postcode.toString(),
                                isLast = true
                            )
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            content()
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (!isLast) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Funciones de formateo
fun formatSex(sex: String): String {
    return when (sex.lowercase()) {
        "male" -> "Masculino"
        "female" -> "Femenino"
        else -> sex
    }
}

fun formatDate(date: String): String {
    return try {
        // Formato esperado: yyyy-mm-dd
        val parts = date.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            "$day/$month/$year"
        } else {
            date // Si no tiene el formato esperado, devolver original
        }
    } catch (e: Exception) {
        date // En caso de error, devolver original
    }
}