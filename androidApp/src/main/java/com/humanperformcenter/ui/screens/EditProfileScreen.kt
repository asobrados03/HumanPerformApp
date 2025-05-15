package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.humaneperformcenter.shared.data.model.User

@Composable
fun EditProfileScreen(
    user: User,
    onSave: (User) -> Unit
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

    Spacer(Modifier.width(20.dp))

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = user.email, onValueChange = {}, label = { Text("Correo") }, enabled = false)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") })
        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Fecha de nacimiento") })
        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Sexo") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(value = profilePictureUrl, onValueChange = { profilePictureUrl = it }, label = { Text("URL de foto de perfil") })
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") })
        OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Rol") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
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
        }) {
            Text("Guardar")
        }
    }
}
