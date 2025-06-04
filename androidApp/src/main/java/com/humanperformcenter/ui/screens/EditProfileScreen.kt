package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    user: LoginResponse,
    updateState: UpdateState,
    onSave: (LoginResponse) -> Unit,
    navController: NavHostController
) {
    // 1) SnackbarHostState para mostrar errores
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 2) ÚNICO Scaffold para esta pantalla, con TopAppBar y SnackbarHost
    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 3) Spinner si estamos en Loading
            if (updateState is UpdateState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // 4) Efecto secundario: Success o Error
            LaunchedEffect(updateState) {
                when (val state = updateState) {
                    is UpdateState.Success -> {
                        navController.popBackStack()
                    }
                    is UpdateState.Error -> {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = state.message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    else -> Unit
                }
            }

            // 5) Aquí va tu formulario puro
            var fullName by remember { mutableStateOf(user.fullName) }
            var dob by remember { mutableStateOf(user.dateOfBirth) }
            var sex by remember { mutableStateOf(user.sex) }
            var phone by remember { mutableStateOf(user.phone) }

            var postcodeText by remember { mutableStateOf(user.postcode?.toString() ?: "") }
            val postcodeInt = postcodeText.toIntOrNull()

            var dni by remember { mutableStateOf(user.dni ?: "") }

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = user.email, onValueChange = {},
                    label = { Text("Correo") },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text("Nombre y Apellidos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                OutlinedTextField(
                    value = dob, onValueChange = { dob = it },
                    label = { Text("Fecha de nacimiento") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                OutlinedTextField(
                    value = sex,
                    onValueChange = { sex = it },
                    label = { Text("Sexo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                OutlinedTextField(
                    value = postcodeText,
                    onValueChange = { newText ->
                        if (newText.all { it.isDigit() }) {
                            postcodeText = newText
                        }
                    },
                    label = { Text("Código Postal") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it },
                    label = { Text("DNI") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                Button(
                    onClick = {
                        onSave(
                            user.copy(
                                fullName = fullName,
                                dateOfBirth = dob,
                                sex = sex,
                                phone = phone,
                                postcode = postcodeInt,
                                dni = if (dni.isBlank()) null else dni
                            )
                        )
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
