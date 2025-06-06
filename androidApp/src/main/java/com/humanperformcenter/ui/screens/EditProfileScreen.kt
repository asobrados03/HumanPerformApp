package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.SexOption
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import com.humanperformcenter.ui.viewmodel.state.UpdateState.Field
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: LoginResponse,
    updateState: UpdateState,
    onSave: (LoginResponse) -> Unit,
    navController: NavHostController
) {
    // 1) Snackbar para errores genéricos de red/servidor
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
            // 2) Spinner si estamos en Loading
            if (updateState is UpdateState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // 3) Efecto para Success (cerrar pantalla) o Error genérico (snackbar)
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

            // 4) Estados locales de UI para retener el texto y los mensajes de error por campo
            var fullName by rememberSaveable { mutableStateOf(user.fullName) }
            var fullNameError by remember { mutableStateOf("") }

            // Convertir "yyyy-MM-dd" ⇒ "dd/MM/yyyy" para mostrar
            val initialDateText: String = user.dateOfBirth.takeIf { it.isNotBlank() }?.let { db ->
                val p = db.split("-")
                if (p.size == 3) {
                    val y = p[0].padStart(4, '0')
                    val m = p[1].padStart(2, '0')
                    val d = p[2].padStart(2, '0')
                    "$d/$m/$y"
                } else ""
            } ?: ""
            var dateOfBirthText by rememberSaveable { mutableStateOf(initialDateText) }
            var dateOfBirthError by remember { mutableStateOf("") }

            var phone by rememberSaveable { mutableStateOf(user.phone) }
            var phoneError by remember { mutableStateOf("") }

            var postcodeText by rememberSaveable { mutableStateOf(user.postcode?.toString() ?: "") }
            val postcodeInt: Int? = postcodeText.toIntOrNull()

            var dni by rememberSaveable { mutableStateOf(user.dni ?: "") }

            val scrollState = rememberScrollState()

            // Opciones de sexo
            val sexOptions = listOf(
                SexOption("Masculino", "Male", Icons.Default.Man),
                SexOption("Femenino",  "Female", Icons.Default.Woman)
            )
            val initialIndex = sexOptions
                .indexOfFirst { it.backendValue.equals(user.sex, ignoreCase = true) }
                .takeIf { it >= 0 } ?: -1

            var selectedIndex by rememberSaveable { mutableIntStateOf(initialIndex) }
            var expandedSex by remember { mutableStateOf(false) }
            var sexError by remember { mutableStateOf("") }
            val selectedSex: SexOption? =
                selectedIndex.takeIf { it >= 0 }?.let { sexOptions[it] }

            // 5) Cuando updateState cambie a ValidationErrors, volcamos los mensajes a los estados locales
            LaunchedEffect(updateState) {
                if (updateState is UpdateState.ValidationErrors) {
                    val fieldErrors = updateState.fieldErrors
                    fullNameError = fieldErrors[Field.FULL_NAME] ?: ""
                    dateOfBirthError = fieldErrors[Field.DATE_OF_BIRTH] ?: ""
                    sexError = fieldErrors[Field.SEX] ?: ""
                    phoneError = fieldErrors[Field.PHONE] ?: ""
                }
            }

            // 6) Contenido del formulario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Correo (no editable) ---
                OutlinedTextField(
                    value = user.email,
                    onValueChange = { /* no editable */ },
                    label = { Text("Correo") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )

                // --- Nombre completo ---
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (fullNameError.isNotEmpty()) fullNameError = ""
                    },
                    label = { Text("Nombre y Apellidos") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                if (fullNameError.isNotEmpty()) {
                    Text(
                        text = fullNameError,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }

                // --- Fecha de nacimiento ---
                OutlinedTextField(
                    value = dateOfBirthText,
                    onValueChange = { new ->
                        val filtered = new.filter { it.isDigit() || it == '/' }.take(10)
                        dateOfBirthText = filtered
                        if (dateOfBirthError.isNotEmpty()) dateOfBirthError = ""
                    },
                    label = { Text("Fecha de nacimiento") },
                    placeholder = { Text("dd/MM/yyyy") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                if (dateOfBirthError.isNotEmpty()) {
                    Text(
                        text = dateOfBirthError,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }

                // --- Sexo desplegable ---
                ExposedDropdownMenuBox(
                    expanded = expandedSex,
                    onExpandedChange = { expandedSex = !expandedSex },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedSex?.label
                            ?: user.sex.takeIf { it.isNotBlank() } ?: "",
                        onValueChange = { /* readOnly */ },
                        readOnly = true,
                        label = { Text("Sexo") },
                        leadingIcon = {
                            if (selectedSex != null) {
                                Icon(selectedSex.icon, contentDescription = null)
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.generos),
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
                                    if (sexError.isNotEmpty()) sexError = ""
                                }
                            )
                        }
                    }
                }
                if (sexError.isNotEmpty()) {
                    Text(
                        text = sexError,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }

                // --- Teléfono ---
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError.isNotEmpty()) phoneError = ""
                    },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                if (phoneError.isNotEmpty()) {
                    Text(
                        text = phoneError,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }

                // --- Código Postal (opcional) ---
                OutlinedTextField(
                    value = postcodeText,
                    onValueChange = { newText ->
                        if (newText.all { it.isDigit() }) {
                            postcodeText = newText
                        }
                    },
                    label = { Text("Código Postal") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // --- DNI (opcional) ---
                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it },
                    label = { Text("DNI") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Botón Guardar: delega validación en el ViewModel + caso de uso ---
                Button(
                    onClick = {
                        // Convertir "dd/MM/yyyy" ⇒ "yyyy-MM-dd"
                        val partes = dateOfBirthText.split("/")
                        val d = partes.getOrNull(0)?.padStart(2, '0') ?: ""
                        val m = partes.getOrNull(1)?.padStart(2, '0') ?: ""
                        val y = partes.getOrNull(2)?.padStart(4, '0') ?: ""
                        val dateOfBirthBackend =
                            if (partes.size == 3) "$y-$m-$d" else ""

                        // Construir el candidato final
                        val nuevoSexo = selectedSex?.backendValue ?: user.sex
                        val updated = user.copy(
                            fullName = fullName.trim(),
                            dateOfBirth = dateOfBirthBackend,
                            sex = nuevoSexo,
                            phone = phone.trim(),
                            postcode = postcodeInt,
                            dni = dni.ifBlank { null }
                        )

                        // Limpiar errores previos y delegar en el ViewModel
                        onSave(updated)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
