package com.humanperformcenter

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humaneperformcenter.shared.data.model.RegisterRequest
import com.humaneperformcenter.shared.data.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit,
    onNavigateToLogin: () -> Unit,
    navController: NavHostController
) {
    // Estados para cada campo del formulario:
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var sexo by rememberSaveable { mutableStateOf("Selecciona sexo") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var codigoPostal by rememberSaveable { mutableStateOf("") }
    var dni by rememberSaveable { mutableStateOf("") }
    var aceptoTerminos by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Lista de opciones de sexo, podría ser ["Masculino","Femenino","Otro"] etc.
    val sexOptions = listOf("Masculino", "Femenino", "Otro")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = Color(0xFFB71C1C), // Rojo fuerte, ajustable
                    titleContentColor = Color.White
                ),
                navigationIcon = {},
                actions = {}
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            item { Text(text = "Registro", style = MaterialTheme.typography.headlineMedium) }
            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            item {
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = { /* icono ojo toggling visualTransformation */ },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }
            item {
                // Campo Sexo como un menú desplegable (dropdown menu)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = sexo,
                        onValueChange = {},
                        label = { Text("Sexo") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                sexo = option
                                expanded = false
                            })
                        }
                    }
                }
            }
            item {
                // Fecha de nacimiento - se podría usar un DatePicker dialog
                OutlinedTextField(
                    value = fechaNacimiento,
                    onValueChange = { fechaNacimiento = it },
                    label = { Text("Fecha de Nacimiento (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    // En una implementación real, al hacer click podríamos abrir un DatePicker dialog
                    readOnly = true,
                    modifier = Modifier.clickable {
                        // TODO: mostrar DatePicker dialog y actualizar fechaNacimiento
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = codigoPostal,
                    onValueChange = { codigoPostal = it },
                    label = { Text("Código Postal") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it },
                    label = { Text("DNI") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it })
                    Text("Acepto los términos y política de privacidad")
                }
            }
            item {
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                Button(
                    onClick = {
                        // Validar campos antes de enviar
                        if (!aceptoTerminos) {
                            errorMessage = "Debes aceptar los términos para registrarte"
                            return@Button
                        }
                        scope.launch {
                            val request = RegisterRequest(nombre, apellidos, email, telefono, password, sexo, fechaNacimiento, codigoPostal, dni)
                            val resultado = AuthRepository.registrar(request)
                            if (resultado.isSuccess) {
                                onRegistroExitoso()
                            } else {
                                errorMessage = resultado.exceptionOrNull()?.message ?: "Error en registro"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registro")
                }
            }
            item {
                TextButton(onClick = { onNavigateToLogin() }) {
                    Text("¿Ya tienes una cuenta? Acceso")
                }
            }
        }
    }
}
