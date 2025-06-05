package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.SexOption
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
            
            var fullName by rememberSaveable { mutableStateOf(user.fullName) }
            var dateOfBirth by rememberSaveable { mutableStateOf(user.dateOfBirth) }
            var phone by rememberSaveable { mutableStateOf(user.phone) }

            var postcodeText by rememberSaveable { mutableStateOf(user.postcode?.toString() ?: "") }
            val postcodeInt: Int? = postcodeText.toIntOrNull()

            var dni by rememberSaveable { mutableStateOf(user.dni ?: "") }

            val scrollState = rememberScrollState()

            val sexOptions = listOf(
                SexOption("Masculino", "Male", Icons.Default.Man),
                SexOption("Femenino",  "Female", Icons.Default.Woman)
            )

            // — 2) Calcular índice inicial según el valor que trae user.sex —
            //     Si user.sex coincide con alguna backendValue (“Male” o “Female”), usamos ese índice.
            //     Si user.sex no coincide con ninguna opción, selectedIndex queda en -1.
            val initialIndex = sexOptions.indexOfFirst { it.backendValue.equals(user.sex, ignoreCase = true) }
                .takeIf { it >= 0 } ?: -1

            var selectedIndex by rememberSaveable { mutableIntStateOf(initialIndex) }
            var expandedSex by remember { mutableStateOf(false) }

            // 3) Derivar la opción (puede ser null si selectedIndex == -1):
            val selectedSex: SexOption? =
                selectedIndex.takeIf { it >= 0 }?.let { sexOptions[it] }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre y Apellidos") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Fecha de nacimiento") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )
                // — 4) Bloque “Sexo” con ExposedDropdownMenuBox —
                ExposedDropdownMenuBox(
                    expanded = expandedSex,
                    onExpandedChange = { expandedSex = !expandedSex },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        // Si selectedGender != null, mostramos su etiqueta (“Masculino”/“Femenino”);
                        // si es null, mostramos text raw = user.sex (al cargar) o cadena vacía.
                        // Esto hace que, al inicio, si user.sex = "Male", muestre "Masculino".
                        value = selectedSex?.label ?: user.sex.takeIf { it.isNotBlank() } ?: "",
                        onValueChange = { /* no se puede editar a mano */ },
                        readOnly = true,
                        label = { Text("Sexo") },
                        leadingIcon = {
                            if (selectedSex != null) {
                                Icon(selectedSex.icon, contentDescription = null)
                            } else {
                                // Si user.sex traía un valor que no coincide con "Male"/"Female",
                                // mostramos un icono genérico (por ejemplo R.drawable.generos).
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
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
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
                Button(
                    onClick = {
                        // Si escogió un índice válido, cogemos el backendValue (“Male”/“Female”).
                        // Si no, mantenemos el valor que ya existía en user.sex.
                        val nuevoSexo = selectedSex?.backendValue ?: user.sex

                        onSave(
                            user.copy(
                                fullName = fullName,
                                dateOfBirth = dateOfBirth,
                                sex = nuevoSexo,
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
