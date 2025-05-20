package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.LogoAppBar

@Composable
fun EditProfileScreen(
    user: User,
    onSave: (User) -> Unit,
    navController: NavHostController
) {
    var name by remember { mutableStateOf<String>(user.name) }
    var lastName by remember { mutableStateOf<String>(user.lastName) }
    var dob by remember { mutableStateOf<String>(user.dateOfBirth) }
    var gender by remember { mutableStateOf<String>(user.gender) }
    var password by remember { mutableStateOf<String>("") }
    var profilePictureUrl by remember { mutableStateOf(user.profilePictureUrl ?: "") }
    var address by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
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
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Apellidos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = dob, onValueChange = { dob = it },
                label = { Text("Fecha de nacimiento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Sexo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = profilePictureUrl,
                onValueChange = { profilePictureUrl = it },
                label = { Text("URL de foto de perfil") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Rol") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            OutlinedTextField(value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .then(Modifier.widthIn(max = 600.dp))
            )
            Button(onClick = {
                onSave(
                    user.copy(
                        name = name,
                        lastName = lastName,
                        dateOfBirth = dob,
                        gender = gender,
                        profilePictureUrl = profilePictureUrl
                    )
                )
            }, modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
            ) {
                Text("Guardar")
            }
        }
    }
}
