package es.uva.sg.psm.humanperformcenter

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
import es.uva.sg.psm.humanperformcenter.data.User

@Composable
fun EditProfileScreen(
    user: User,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var dob by remember { mutableStateOf(user.dateOfBirth) }
    var gender by remember { mutableStateOf(user.gender) }
    var password by remember { mutableStateOf("") }

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
        Button(onClick = {
            onSave(user.copy(name = name, lastName = lastName, dateOfBirth = dob, gender = gender))
        }) {
            Text("Guardar")
        }
    }
}
