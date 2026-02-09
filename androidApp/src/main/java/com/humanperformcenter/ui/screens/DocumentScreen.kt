package com.humanperformcenter.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.ui.components.user.DocumentsSheet
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.shared.presentation.ui.UploadState
import java.io.File

@Composable
fun DocumentScreen(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val uiState by userViewModel.uploadState.collectAsStateWithLifecycle()

    // Estados para el documento seleccionado
    var documentBytes by remember { mutableStateOf<ByteArray?>(null) }
    var documentName by remember { mutableStateOf("") }

    var showSheet by remember { mutableStateOf(false) }

    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // Procesamos directamente el URI y guardamos solo lo que necesitamos
        uri?.loadDocument(context) { name, bytes ->
            documentName = name
            documentBytes = bytes
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            tempCameraUri!!.loadDocument(context) { name, bytes ->
                documentName = name
                documentBytes = bytes
            }
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        // Solo guardamos los datos procesados, no el URI original
        uri?.loadDocument(context) { name, bytes ->
            documentName = name
            documentBytes = bytes
        }
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón para abrir el menú de selección
            Button(onClick = { showSheet = true }) {
                Text("Seleccionar archivo")
            }

            Spacer(Modifier.height(12.dp))

            // Mostramos el nombre y permitimos subir el archivo
            if (documentName.isNotBlank()) {
                Text("Archivo: $documentName")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        userViewModel.uploadDocument(documentName, documentBytes!!)
                    },
                    enabled = documentBytes != null && uiState !is UploadState.Loading
                ) {
                    if (uiState is UploadState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Subir")
                }
            }

            if(showSheet) {
                DocumentsSheet(
                    showSheet = true,
                    onDismiss = { showSheet = false },
                    onCamera = {
                        showSheet = false
                        // Crear archivo temporal para la cámara
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
                        takePhotoLauncher.launch(uri)
                    },
                    onGallery = {
                        showSheet = false
                        pickImageLauncher.launch("image/*")
                    },
                    onFiles = {
                        showSheet = false
                        pickFileLauncher.launch(arrayOf("*/*"))
                    }
                )
            }
        }
    }

    // Manejo de estados de la subida
    LaunchedEffect(uiState) {
        when (uiState) {
            is UploadState.Success -> {
                snackbarHostState.showSnackbar("Archivo subido correctamente")
                userViewModel.resetUploadState()
                navController.popBackStack()
            }
            is UploadState.Error -> {
                snackbarHostState.showSnackbar("Error al subir: ${(uiState as UploadState.Error).message}")
                userViewModel.resetUploadState()
                navController.popBackStack()
            }
            else -> { /* Loading o Idle: nada que hacer */ }
        }
    }
}

// Función auxiliar para procesar cualquier URI y extraer nombre y bytes
private fun Uri.loadDocument(
    context: android.content.Context,
    onLoaded: (name: String, bytes: ByteArray) -> Unit
) {
    context.contentResolver.openInputStream(this)?.use { stream ->
        val bytes = stream.readBytes()
        val cursor = context.contentResolver.query(this, null, null, null, null)
        val name = cursor?.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && idx >= 0) it.getString(idx) else this.lastPathSegment ?: ""
        } ?: (this.lastPathSegment ?: "")
        onLoaded(name, bytes)
    }
}