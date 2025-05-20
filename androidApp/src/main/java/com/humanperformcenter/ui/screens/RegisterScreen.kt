package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.data.repository.AuthRepository
import com.humanperformcenter.DateVisualTransformation
import com.humanperformcenter.LogoAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit,
    onNavigateToLogin: () -> Unit,
    navController: NavHostController
) {
    // — estados base —
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var fechaNacimientoText by rememberSaveable { mutableStateOf("") }
    var codigoPostal by rememberSaveable { mutableStateOf("") }
    var dni by rememberSaveable { mutableStateOf("") }
    var aceptoTerminos by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // — sexo desplegable —
    val sexOptions = listOf("Masculino", "Femenino", "Otro")
    var sexo by rememberSaveable { mutableStateOf(sexOptions[0]) }
    var expandedSexo by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Registro", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            // Nombre / Apellidos / Email / Teléfono / Contraseña
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apellidos, onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Icono de contraseña")
                },
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
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // SEXO desplegable usando Material3 ExposedDropdownMenuBox
            ExposedDropdownMenuBox(
                expanded = expandedSexo,
                onExpandedChange = { expandedSexo = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sexo,
                    onValueChange = {},
                    leadingIcon = { Icon(Icons.Default.Cloud, null) },
                    label = { Text("Sexo") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSexo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedSexo,
                    onDismissRequest = { expandedSexo = false }
                ) {
                    sexOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sexo = option
                                expandedSexo = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = fechaNacimientoText,
                onValueChange = { new ->
                    // sólo dígitos, máximo 8 (ddMMyyyy)
                    val digits = new.filter { it.isDigit() }.take(8)
                    fechaNacimientoText = digits
                },
                label = { Text("Fecha (dd/MM/yyyy)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = DateVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(8.dp))

            // Código postal / DNI
            OutlinedTextField(
                value = codigoPostal, onValueChange = { codigoPostal = it },
                label = { Text("Código Postal") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dni, onValueChange = { dni = it },
                label = { Text("DNI") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it })
                Text("Acepto términos y política de privacidad")
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!aceptoTerminos) {
                        errorMessage = "Debes aceptar los términos"
                        return@Button
                    }
                    scope.launch {
                        val req = RegisterRequest(
                            nombre, apellidos, email, telefono,
                            password, sexo,
                            fechaNacimientoText,
                            codigoPostal, dni
                        )
                        val res = AuthRepository.registrar(req)
                        if (res.isSuccess) onRegistroExitoso()
                        else errorMessage = res.exceptionOrNull()?.message
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registro")
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes una cuenta? Acceso")
            }
        }
    }
}
