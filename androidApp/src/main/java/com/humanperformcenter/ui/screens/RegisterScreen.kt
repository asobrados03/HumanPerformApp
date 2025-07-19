package com.humanperformcenter.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.LocationCity
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.SexOption
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult.RegisterField
import com.humanperformcenter.ui.components.EditableUserProfileImage
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.ProfilePhotoSheet
import com.humanperformcenter.ui.util.DateVisualTransformation
import com.humanperformcenter.ui.viewmodel.AuthViewModel
import com.humanperformcenter.ui.viewmodel.AuthViewModelFactory
import com.humanperformcenter.ui.viewmodel.state.RegisterState
import kotlinx.coroutines.launch
import java.io.File

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

    // 2. Suscribirnos al estado de registro
    val registerState by viewModel.registerState.observeAsState(RegisterState.Idle)

    // — estados base —
    var nombre by rememberSaveable { mutableStateOf("") }
    var nombreError by remember { mutableStateOf("") }

    var apellidos by rememberSaveable { mutableStateOf("") }
    var apellidosError by remember { mutableStateOf("") }

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    var telefono by rememberSaveable { mutableStateOf("") }
    var telefonoError by remember { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var fechaNacimientoText by rememberSaveable { mutableStateOf("") }
    var fechaNacimientoError by remember { mutableStateOf("") }

    var codigoPostal by rememberSaveable { mutableStateOf("") }
    var codigoPostalError by remember { mutableStateOf("") }

    var direccionPostal by rememberSaveable { mutableStateOf("") }
    var direccionPostalError by rememberSaveable { mutableStateOf("") }

    var dni by rememberSaveable { mutableStateOf("") }
    var dniError by remember { mutableStateOf("") }

    var aceptoTerminos by rememberSaveable { mutableStateOf(false) }
    var aceptoPolitica by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // 🔹 Estados para la imagen de perfil
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var profilePicBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profilePicName by remember { mutableStateOf<String?>(null) }
    var profilePicUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Estado para el ModalBottomSheet
    var showSheet by remember { mutableStateOf(false) }

    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profilePicUri = it

            // ② y seguimos leyendo bytes y nombre como antes
            coroutineScope.launch {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    profilePicBytes = stream.readBytes()
                }
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && idx >= 0) {
                        profilePicName = cursor.getString(idx)
                    }
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { uri ->
                // 4) actualizamos el estado local
                profilePicUri = uri

                // 5) leemos bytes y nombre como haces en galería
                coroutineScope.launch {
                    // bytes
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        profilePicBytes = stream.readBytes()
                    }
                    // nombre (aquí, como es un file provider, extraemos del path)
                    val name = uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?: "IMG_${System.currentTimeMillis()}.jpg"
                    profilePicName = name
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Sexo desplegable
    val sexOptions = listOf(
        SexOption("Masculino", "Male", Icons.Default.Man),
        SexOption("Femenino", "Female", Icons.Default.Woman)
    )
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var expandedSex by rememberSaveable { mutableStateOf(false) }
    var sexError by remember { mutableStateOf("") }
    val selectedSex = selectedIndex.takeIf { it >= 0 }?.let { sexOptions[it] }

    val scroll = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.ValidationErrors) {
            val fieldErrors = (registerState as RegisterState.ValidationErrors).fieldErrors
            nombreError = fieldErrors[RegisterField.FIRST_NAME] ?: ""
            apellidosError = fieldErrors[RegisterField.LAST_NAME] ?: ""
            emailError = fieldErrors[RegisterField.EMAIL] ?: ""
            telefonoError = fieldErrors[RegisterField.PHONE] ?: ""
            passwordError = fieldErrors[RegisterField.PASSWORD] ?: ""
            fechaNacimientoError = fieldErrors[RegisterField.DATE_OF_BIRTH] ?: ""
            sexError = fieldErrors[RegisterField.SEX] ?: ""
            codigoPostalError = fieldErrors[RegisterField.POSTCODE] ?: ""
            direccionPostalError = fieldErrors[RegisterField.POSTAL_ADDRESS] ?: ""
            dniError = fieldErrors[RegisterField.DNI] ?: ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Registro", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            // 🔹 Botón para seleccionar imagen
            EditableUserProfileImage(
                photoName = profilePicName,
                photoUri  = profilePicUri,
                onChangePhotoClick = { showSheet = true },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (showSheet) {
                ProfilePhotoSheet(
                    showSheet      = true,
                    onDismiss      = { showSheet = false },
                    onDelete       = {
                        profilePicName = null
                        profilePicUri  = null
                        profilePicBytes= null
                        showSheet      = false
                    },
                    onCamera       = {
                        showSheet = false
                        // 1) crear archivo temporal
                        val file = File(
                            context.cacheDir,
                            "IMG_${System.currentTimeMillis()}.jpg"
                        )
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        tempCameraUri = uri
                        // 2) lanzar cámara
                        cameraLauncher.launch(uri)
                    },
                    onGallery      = {
                        showSheet = false
                        imagePickerLauncher.launch("image/*")
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Nombre / Apellidos / Email / Teléfono / Contraseña
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (nombreError.isNotEmpty()) nombreError = ""
                },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            if (nombreError.isNotEmpty()) {
                Text(
                    text = nombreError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = {
                    apellidos = it
                    if (apellidosError.isNotEmpty()) apellidosError = ""
                },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            if (apellidosError.isNotEmpty()) {
                Text(
                    text = apellidosError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) emailError = ""
                },
                label = { Text("Correo electrónico") },
                placeholder = { Text("usuario@ejemplo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    if (telefonoError.isNotEmpty()) telefonoError = ""
                },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            if (telefonoError.isNotEmpty()) {
                Text(
                    text = telefonoError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError.isNotEmpty()) passwordError = ""
                },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (passwordError.isNotEmpty()) {
                Text(
                    text = passwordError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Sexo desplegable
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

            Spacer(Modifier.height(8.dp))

            // Fecha de nacimiento
            OutlinedTextField(
                value = fechaNacimientoText,
                onValueChange = { new ->
                    val filtered = new.filter { it.isDigit() || it == '/' }.take(10)
                    fechaNacimientoText = filtered
                    if (fechaNacimientoError.isNotEmpty()) fechaNacimientoError = ""
                },
                label = { Text("Fecha de nacimiento") },
                placeholder = { Text("dd/mm/yyyy") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = DateVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (fechaNacimientoError.isNotEmpty()) {
                Text(
                    text = fechaNacimientoError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = direccionPostal,
                onValueChange = {
                    direccionPostal = it
                    if (direccionPostalError.isNotEmpty()) direccionPostalError = ""
                },
                label = { Text("Dirección Postal") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Código postal / DNI
            OutlinedTextField(
                value = codigoPostal,
                onValueChange = {
                    codigoPostal = it
                    if (codigoPostalError.isNotEmpty()) codigoPostalError = ""
                },
                label = { Text("Código Postal") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (codigoPostalError.isNotEmpty()) {
                Text(
                    text = codigoPostalError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = dni,
                onValueChange = {
                    dni = it
                    if (dniError.isNotEmpty()) dniError = ""
                },
                label = { Text("DNI") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            if (dniError.isNotEmpty()) {
                Text(
                    text = dniError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Términos y condiciones / Política de privacidad
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = aceptoTerminos,
                        onCheckedChange = {
                            aceptoTerminos = it
                            errorMessage = null
                        }
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

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = aceptoPolitica,
                        onCheckedChange = {
                            aceptoPolitica = it
                            errorMessage = null
                        }
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

            Spacer(Modifier.height(8.dp))

            // Mensaje de error (local o proveniente del servidor)
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Estado de registro: Loading, Error o Success
            when (registerState) {
                is RegisterState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is RegisterState.Success -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            message = "Te has registrado exitosamente",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        viewModel.resetStates()
                        onRegistroExitoso()
                    }
                }
                is RegisterState.Error -> {
                    // Mostrar el mensaje de error recibido desde el repositorio (JSON)
                    errorMessage = (registerState as RegisterState.Error).message
                }
                else -> { /* Idle: nada que hacer */ }
            }

            Spacer(Modifier.height(16.dp))

            // Botón de Registro
            Button(
                onClick = {
                    // Validaciones locales antes de enviar
                    when {
                        !aceptoTerminos ->
                            errorMessage = "Debes aceptar los términos y condiciones"
                        !aceptoPolitica ->
                            errorMessage = "Debes aceptar la política de privacidad"
                        else -> {
                            // Si pasa las validaciones, se envía la petición
                            errorMessage = null
                            val sexValue = selectedSex?.backendValue ?: ""
                            val req = RegisterRequest(
                                nombre,
                                apellidos,
                                email,
                                telefono,
                                password,
                                sexValue,
                                fechaNacimientoText,
                                codigoPostal,
                                direccionPostal,
                                dni,
                                profilePicBytes,
                                profilePicName
                            )
                            viewModel.register(req)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }

            Spacer(Modifier.height(8.dp))

            // Enlace a pantalla de login
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes una cuenta? Inicia sesión")
            }
        }
    }
}
