package com.humanperformcenter.ui.screens

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.AuthViewModel
import com.humanperformcenter.ui.viewmodel.AuthViewModelFactory
import com.humanperformcenter.ui.viewmodel.state.LoginState
import androidx.core.content.edit

private const val PREFS_NAME = "login_prefs"
private const val KEY_EMAIL = "KEY_EMAIL"
private const val KEY_PASSWORD = "KEY_PASSWORD"
private const val KEY_REMEMBER = "KEY_REMEMBER"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    navController: NavHostController
) {
    // 1) Obtenemos el contexto y las SharedPreferences
    val context = LocalContext.current
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 2) Estados locales para el email, contraseña, checkbox y visibilidad de contraseña
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var remember by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // 3) ViewModel de autenticación
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AppModule.authUseCase)
    )
    val loginState by viewModel.loginState.observeAsState(LoginState.Idle)

    // 4) Al cargar la pantalla por primera vez, leemos lo que hubiera guardado en SharedPreferences
    LaunchedEffect(Unit) {
        val savedRemember = prefs.getBoolean(KEY_REMEMBER, false)
        if (savedRemember) {
            // Si estaba marcado "Recordar", leemos email y password guardados
            email = prefs.getString(KEY_EMAIL, "") ?: ""
            password = prefs.getString(KEY_PASSWORD, "") ?: ""
            remember = true
        }
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues = paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Acceso", style = MaterialTheme.typography.headlineMedium)

            // Campo de email
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

            // Checkbox “Recordar contraseña”
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = remember,
                    onCheckedChange = { checked ->
                        remember = checked

                        if (!checked) {
                            // Si desmarcamos, borramos lo guardado en prefs
                            prefs.edit {
                                remove(KEY_EMAIL)
                                    .remove(KEY_PASSWORD)
                                    .putBoolean(KEY_REMEMBER, false)
                            }
                        }
                    }
                )
                Text(text = "Recordar contraseña")
            }

            // Mensaje de error (antes de enviar)
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            // Animar el estado de login
            when (loginState) {
                is LoginState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is LoginState.Error -> {
                    Text(
                        text = (loginState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                is LoginState.Success -> {
                    LaunchedEffect(Unit) {
                        viewModel.resetStates()
                        onLoginSuccess()
                    }
                }
                else -> { /* Idle: no hacemos nada */ }
            }

            // Botón de “Acceso”
            Button(
                onClick = {
                    // 1) Antes de llamar a viewModel.login, si “remember” está activo,
                    //    guardamos en SharedPreferences email y password. Si no, borramos.
                    if (remember) {
                        prefs.edit {
                            putString(KEY_EMAIL, email)
                                .putString(KEY_PASSWORD, password)
                                .putBoolean(KEY_REMEMBER, true)
                        }
                    } else {
                        prefs.edit {
                            remove(KEY_EMAIL)
                                .remove(KEY_PASSWORD)
                                .putBoolean(KEY_REMEMBER, false)
                        }
                    }

                    // 2) Llamamos al ViewModel para iniciar login
                    viewModel.login(email.trim(), password)
                },
                enabled = loginState != LoginState.Loading,
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Acceso")
            }

            TextButton(
                onClick = { /* TODO: recuperar contraseña */ },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            // Botón “Regístrate ya”
            TextButton(
                onClick = { onNavigateToRegister() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Regístrate ya")
            }
        }
    }
}
