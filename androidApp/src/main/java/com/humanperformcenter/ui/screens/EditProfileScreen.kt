package com.humanperformcenter.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.R
import com.humanperformcenter.data.SexOption
import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.ui.components.EditableUserProfileImage
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.ProfilePhotoSheet
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.state.DeleteProfilePicState
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import com.humanperformcenter.ui.viewmodel.state.UpdateState.Field
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    userViewModel: UserViewModel,
    onSave: (User, ByteArray?) -> Unit,
    onDeleteProfilePic: () -> Unit,
    navController: NavHostController
) {
    val updateState: UpdateState by userViewModel.updateState.collectAsStateWithLifecycle()

    val deleteProfilePicState: DeleteProfilePicState by userViewModel.deleteProfilePicState
        .collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(deleteProfilePicState) {
        when(val state = deleteProfilePicState) {
            is DeleteProfilePicState.Success -> {
                userViewModel.clearDeleteProfilePicState()
                navController.popBackStack()
            }
            is DeleteProfilePicState.Error -> {
                userViewModel.clearDeleteProfilePicState()
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

    var profilePicBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profilePicName by remember { mutableStateOf(user.profilePictureName) }
    var profilePicUri by rememberSaveable { mutableStateOf<Uri?>(null) }

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
            var postcodeError by remember { mutableStateOf("") }

            var postAddress by rememberSaveable { mutableStateOf(user.postAddress) }
            var postAddressError by remember { mutableStateOf("") }

            var dni by rememberSaveable { mutableStateOf(user.dni ?: "") }
            var dniError by remember { mutableStateOf("") }

            val scrollState = rememberScrollState()

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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

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
                            onDeleteProfilePic()
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
                    onValueChange = {
                        fullName = it
                        if (fullNameError.isNotEmpty()) fullNameError = ""
                    },
                    isError = fullNameError.isNotEmpty(),
                    supportingText = {
                        if (fullNameError.isNotEmpty()) Text(text = fullNameError, color = Color.Red)
                    },
                    label = { Text("Nombre y Apellidos") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )

                OutlinedTextField(
                    value = dateOfBirthText,
                    onValueChange = { new ->
                        val filtered = new.filter { it.isDigit() || it == '/' }.take(10)
                        dateOfBirthText = filtered
                        if (dateOfBirthError.isNotEmpty()) dateOfBirthError = ""
                    },
                    isError = dateOfBirthError.isNotEmpty(),
                    supportingText = {
                        if (dateOfBirthError.isNotEmpty()) Text(text = dateOfBirthError, color = Color.Red)
                    },
                    label = { Text("Fecha de nacimiento") },
                    placeholder = { Text("dd/MM/yyyy") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )

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

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError.isNotEmpty()) phoneError = ""
                    },
                    isError = phoneError.isNotEmpty(),
                    supportingText = {
                        if (phoneError.isNotEmpty()) Text(text = phoneError, color = Color.Red)
                    },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = postAddress,
                    onValueChange = {
                        postAddress = it
                        if (postAddressError.isNotEmpty()) postAddressError = ""
                    },
                    isError = postAddressError.isNotEmpty(),
                    supportingText = {
                        if (postAddressError.isNotEmpty()) Text(text = postAddressError, color = Color.Red)
                    },
                    label = { Text("Dirección Postal") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )

                OutlinedTextField(
                    value = postcodeText,
                    onValueChange = { newText ->
                        if (newText.all { it.isDigit() }) {
                            postcodeText = newText
                        }
                        if (postcodeError.isNotEmpty()) postcodeError = ""
                    },
                    isError = postcodeError.isNotEmpty(),
                    supportingText = {
                        if (postcodeError.isNotEmpty()) Text(text = postcodeError, color = Color.Red)
                    },
                    label = { Text("Código Postal") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

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
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .widthIn(max = 600.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            postAddress = postAddress,
                            dni = dni.ifBlank { null },
                            profilePictureName = profilePicName
                        )

                        // Guardamos en el SavedStateHandle
                        navController
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("new_profile_uri", profilePicUri.toString())

                        // Limpiar errores previos y delegar en el ViewModel
                        onSave(updated, profilePicBytes)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Guardar")
                }
            }

            LaunchedEffect(updateState) {
                when (updateState) {
                    is UpdateState.ValidationErrors -> {
                        // Asignamos los errores a cada campo
                        val errors = (updateState as UpdateState.ValidationErrors).fieldErrors
                        fullNameError = errors[Field.FULL_NAME] ?: ""
                        dateOfBirthError = errors[Field.DATE_OF_BIRTH] ?: ""
                        sexError = errors[Field.SEX] ?: ""
                        postAddressError = errors[Field.POST_ADDRESS] ?: ""
                        phoneError = errors[Field.PHONE] ?: ""
                        dniError = errors[Field.DNI] ?: ""

                        // Mostramos snackbar con los campos que tienen error
                        val camposConError = errors.keys.joinToString(", ") { it.name }
                        snackbarHostState.showSnackbar(
                            message = "Revisa los campos con errores: $camposConError",
                            duration = SnackbarDuration.Long
                        )
                    }
                    is UpdateState.Error -> {
                        snackbarHostState.showSnackbar(
                            message = (updateState as UpdateState.Error).message,
                            duration = SnackbarDuration.Short
                        )
                        userViewModel.clearUpdateState()
                    }
                    is UpdateState.Success -> {
                        snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                        userViewModel.clearUpdateState()
                        navController.popBackStack()
                    }
                    else -> {}
                }
            }

            // Spinner si estamos en Loading
            if (updateState is UpdateState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
