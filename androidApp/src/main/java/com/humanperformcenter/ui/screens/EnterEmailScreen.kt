package com.humanperformcenter.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.PasswordResetInfo
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.state.ResetPasswordState

@Composable
fun EnterEmailScreen(
    onEmailSubmit: (String) -> Unit,
    resetPasswordState: ResetPasswordState,
    onResetState: () -> Unit,
    navController: NavHostController
) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isLoading = resetPasswordState is ResetPasswordState.Loading

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is ResetPasswordState.Success -> {
                snackbarHostState.showSnackbar(
                    message = resetPasswordState.message,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )
                navController.navigate(PasswordResetInfo)
                onResetState()
            }
            is ResetPasswordState.Error -> {
                snackbarHostState.showSnackbar(
                    message = resetPasswordState.message,
                    duration = SnackbarDuration.Short
                )
                onResetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Introduce tu correo electrónico para restablecer la contraseña")
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    isEmailValid = EMAIL_ADDRESS.matcher(it).matches()
                },
                label = { Text("Correo electrónico") },
                isError = !isEmailValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            if (!isEmailValid) {
                Text(
                    "Introduce un correo válido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Enviando...", Toast.LENGTH_SHORT).show()
                    if (isEmailValid) {
                        onEmailSubmit(email)
                    }
                },
                enabled = email.isNotBlank() && isEmailValid && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar")
            }
        }
    }
}
