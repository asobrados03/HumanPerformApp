package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.viewModels.AuthViewModel
import com.humanperformcenter.viewModels.AuthViewModelFactory
import com.humanperformcenter.viewModels.LoginState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    navController: NavHostController
) {
    // 1. Obtener el ViewModel
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AppModule.authUseCase)
    )

    // 2. Suscribirnos al estado de login
    val loginState by viewModel.loginState.observeAsState(LoginState.Idle)

    // Estados locales para campos de texto y checkbox
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var remember by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues = padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Acceso", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        val icon = if (passwordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )


            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = remember, onCheckedChange = { remember = it })
                Text("Recordar contraseña")
            }

            errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

            when (loginState) {
                is LoginState.Loading -> {
                    CircularProgressIndicator()
                }
                is LoginState.Error -> {
                    Text(
                        text = (loginState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is LoginState.Success -> {
                    // una vez llega el Success, reseteamos y navegamos
                    LaunchedEffect(Unit) {
                        viewModel.reset()
                        onLoginSuccess()
                    }
                }
                else -> { /* Idle: no hacer nada */ }
            }

            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                enabled = loginState != LoginState.Loading,
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Acceso")
            }

            TextButton(onClick = { /* TODO: recuperar contraseña */ }) {
                Text("¿Olvidaste tu contraseña?")
            }
            TextButton(onClick = { onNavigateToRegister() }) {
                Text("Regístrate ya")
            }
        }
    }
}
