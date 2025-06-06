package com.humanperformcenter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.SexOption
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.util.DateVisualTransformation
import com.humanperformcenter.ui.viewmodel.AuthViewModel
import com.humanperformcenter.ui.viewmodel.AuthViewModelFactory
import com.humanperformcenter.ui.viewmodel.state.RegisterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit,
    onNavigateToLogin: () -> Unit,
    navController: NavHostController
) {
    // 1. Obtener el ViewModel
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AppModule.authUseCase)
    )

    // 2. Suscribirnos al estado de login
    val registerState by viewModel.registerState.observeAsState(RegisterState.Idle)

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
    var aceptoPolitica by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // — sexo desplegable —
    val sexOptions = listOf(
        SexOption("Masculino", "Male", Icons.Default.Man),
        SexOption("Femenino", "Female", Icons.Default.Woman)
    )

    // 1) Sólo guardamos un Int. -1 = nada seleccionado
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var expandedSex by rememberSaveable { mutableStateOf(false) }

    // 2) Derivamos la opción seleccionada (o null)
    val selectedSex = selectedIndex.takeIf { it >= 0 }?.let { sexOptions[it] }

    val scroll = rememberScrollState()

    val uriHandler = LocalUriHandler.current

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

            ExposedDropdownMenuBox(
                expanded = expandedSex,
                onExpandedChange = { expandedSex = !expandedSex },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSex?.label ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sexo") },
                    leadingIcon = {
                        if (selectedSex != null) {
                            Icon(selectedSex.icon, contentDescription = null)
                        } else {
                            Icon(
                                painterResource(id = R.drawable.generos),
                                contentDescription = "Sexo",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSex) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expandedSex,
                    onDismissRequest = { expandedSex = false }
                ) {
                    sexOptions.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            leadingIcon = { Icon(option.icon, contentDescription = null) },
                            onClick = {
                                selectedIndex = index
                                expandedSex = false
                            }
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
                label = { Text("Fecha de nacimiento (dd/mm/yyyy)") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
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

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = aceptoTerminos,
                        onCheckedChange = { aceptoTerminos = it }
                    )
                    Text(text = "Acepto ")
                    Text(
                        text = "términos y condiciones",
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://www.humanperformcenter.com/cliente/condiciones")
                            },
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = aceptoPolitica,
                        onCheckedChange = { aceptoPolitica = it }
                    )
                    Text(text = "Acepto ")
                    Text(
                        text = "política de privacidad",
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://www.humanperformcenter.com/cliente/politica-privacidad")
                            },
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            when (registerState) {
                is RegisterState.Loading -> {
                    CircularProgressIndicator()
                }
                is RegisterState.Success -> {
                    // Registro exitoso → navegar y limpiar estado
                    LaunchedEffect(Unit) {
                        viewModel.resetStates()
                        onRegistroExitoso()
                    }
                }
                is RegisterState.Error -> {
                    errorMessage = (registerState as RegisterState.Error).message
                }
                RegisterState.Idle -> {
                    // No hacer nada
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!aceptoTerminos) {
                        errorMessage = "Debes aceptar los términos y condiciones"
                        return@Button
                    } else if (!aceptoPolitica) {
                        errorMessage = "Debes aceptar la política de privacidad"
                        return@Button
                    }

                    val genderValue = selectedSex!!.backendValue

                    val req = RegisterRequest(
                        nombre, apellidos, email, telefono,
                        password, genderValue,
                        fechaNacimientoText,
                        codigoPostal, dni
                    )
                    viewModel.register(req)
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
