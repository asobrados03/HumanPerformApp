package com.humanperformcenter.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.SexOption
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult.RegisterField
import com.humanperformcenter.ui.components.EditableUserProfileImage
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.ProfilePhotoSheet
import com.humanperformcenter.ui.viewmodel.AuthViewModel
import com.humanperformcenter.ui.viewmodel.AuthViewModelFactory
import com.humanperformcenter.ui.viewmodel.state.RegisterState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistroExitoso: () -> Unit,
    onNavigateToLogin: () -> Unit,
    navController: NavHostController
) {
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AppModule.authUseCase)
    )

    val registerState by viewModel.registerState.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }

    var surnames by rememberSaveable { mutableStateOf("") }
    var surnamesError by remember { mutableStateOf("") }

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var dateOfBirthText by rememberSaveable { mutableStateOf("") }
    var dateOfBirthError by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    var postalCode by rememberSaveable { mutableStateOf("") }
    var postalCodeError by remember { mutableStateOf("") }

    var postalAddress by rememberSaveable { mutableStateOf("") }
    var postalAddressError by rememberSaveable { mutableStateOf("") }

    var dni by rememberSaveable { mutableStateOf("") }
    var dniError by remember { mutableStateOf("") }

    var hasAcceptedTerms by rememberSaveable { mutableStateOf(false) }
    var hasAcceptedPrivacyPolicy by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var profilePicBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profilePicName by remember { mutableStateOf<String?>(null) }
    var profilePicUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    var showSheet by remember { mutableStateOf(false) }

    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profilePicUri = it

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
                profilePicUri = uri

                coroutineScope.launch {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        profilePicBytes = stream.readBytes()
                    }

                    val name = uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?: "IMG_${System.currentTimeMillis()}.jpg"
                    profilePicName = name
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

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
            nameError = fieldErrors[RegisterField.FIRST_NAME] ?: ""
            surnamesError = fieldErrors[RegisterField.LAST_NAME] ?: ""
            emailError = fieldErrors[RegisterField.EMAIL] ?: ""
            phoneError = fieldErrors[RegisterField.PHONE] ?: ""
            passwordError = fieldErrors[RegisterField.PASSWORD] ?: ""
            sexError = fieldErrors[RegisterField.SEX] ?: ""
            postalCodeError = fieldErrors[RegisterField.POSTCODE] ?: ""
            postalAddressError = fieldErrors[RegisterField.POSTAL_ADDRESS] ?: ""
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

            EditableUserProfileImage(
                photoName = profilePicName,
                photoUri  = profilePicUri,
                onChangePhotoClick = { showSheet = true },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                dateOfBirthText = dateFormatter.format(Date(millis))
                                if (dateOfBirthError.isNotEmpty()) dateOfBirthError = ""
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        headline = {
                            Text(
                                text = if (datePickerState.selectedDateMillis != null) "Fecha seleccionada" else "Selecciona fecha",
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        }
                    )
                }
            }

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

                        cameraLauncher.launch(uri)
                    },
                    onGallery      = {
                        showSheet = false
                        imagePickerLauncher.launch("image/*")
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError.isNotEmpty()) nameError = ""
                },
                isError = nameError.isNotEmpty(),
                supportingText = {
                    if (nameError.isNotEmpty()) Text(text = nameError, color = Color.Red)
                },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = surnames,
                onValueChange = {
                    surnames = it
                    if (surnamesError.isNotEmpty()) surnamesError = ""
                },
                isError = surnamesError.isNotEmpty(),
                supportingText = {
                    if (surnamesError.isNotEmpty()) Text(text = surnamesError, color = Color.Red)
                },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) emailError = ""
                },
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) Text(text = emailError, color = Color.Red)
                },
                label = { Text("Correo electrónico") },
                placeholder = { Text("usuario@ejemplo.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    if (phoneError.isNotEmpty()) phoneError = ""
                },
                isError = phoneError.isNotEmpty(),
                supportingText = {
                    if (phoneError.isNotEmpty()) Text(text = phoneError, color = Color.Red)
                },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError.isNotEmpty()) passwordError = ""
                },
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) Text(text = passwordError, color = Color.Red)
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
                    isError = sexError.isNotEmpty(),
                    supportingText = {
                        if (sexError.isNotEmpty()) Text(text = sexError, color = Color.Red)
                    },
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

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = dateOfBirthText,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    isError = dateOfBirthError.isNotEmpty(),
                    supportingText = {
                        if (dateOfBirthError.isNotEmpty()) Text(text = dateOfBirthError,
                            color = Color.Red)
                    },
                    label = { Text("Fecha de nacimiento") },
                    placeholder = { Text("Selecciona tu fecha") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth,
                        contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (dateOfBirthError.isNotEmpty()) Color.Red
                                            else MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = postalAddress,
                onValueChange = {
                    postalAddress = it
                    if (postalAddressError.isNotEmpty()) postalAddressError = ""
                },
                isError = postalAddressError.isNotEmpty(),
                supportingText = {
                    if (postalAddressError.isNotEmpty()) Text(text = postalAddressError, color = Color.Red)
                },
                label = { Text("Dirección Postal") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = postalCode,
                onValueChange = {
                    postalCode = it
                    if (postalCodeError.isNotEmpty()) postalCodeError = ""
                },
                isError = postalCodeError.isNotEmpty(),
                supportingText = {
                    if (postalCodeError.isNotEmpty()) Text(text = postalCodeError, color = Color.Red)
                },
                label = { Text("Código Postal") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = dni,
                onValueChange = {
                    dni = it
                    if (dniError.isNotEmpty()) dniError = ""
                },
                isError = dniError.isNotEmpty(),
                supportingText = {
                    if (dniError.isNotEmpty()) Text(text = dniError, color = Color.Red)
                },
                label = { Text("DNI") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasAcceptedTerms,
                        onCheckedChange = {
                            hasAcceptedTerms = it
                            errorMessage = null
                        }
                    )
                    Text(text = "Acepto ")
                    Text(
                        text = "términos y condiciones",
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://www.humanperformcenter.com/condiciones")
                            },
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasAcceptedPrivacyPolicy,
                        onCheckedChange = {
                            hasAcceptedPrivacyPolicy = it
                            errorMessage = null
                        }
                    )
                    Text(text = "Acepto ")
                    Text(
                        text = "política de privacidad",
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://www.humanperformcenter.com/politica-privacidad")
                            },
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

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
                    errorMessage = (registerState as RegisterState.Error).message
                }
                else -> { }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        !hasAcceptedTerms ->
                            errorMessage = "Debes aceptar los términos y condiciones"
                        !hasAcceptedPrivacyPolicy ->
                            errorMessage = "Debes aceptar la política de privacidad"
                        else -> {
                            // Si pasa las validaciones, se envía la petición
                            errorMessage = null
                            val sexValue = selectedSex?.backendValue ?: ""
                            val req = RegisterRequest(
                                name,
                                surnames,
                                email,
                                phoneNumber,
                                password,
                                sexValue,
                                dateOfBirthText,
                                postalCode,
                                postalAddress,
                                dni,
                                "android",
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
