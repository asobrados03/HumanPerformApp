package com.humanperformcenter.ui.screens

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.presentation.ui.UploadState
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.user.DocumentsSheet
import com.humanperformcenter.ui.util.rememberUserDocumentsCoordinator
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentSelectionViewModel

@Composable
fun DocumentScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    documentsViewModel: UserDocumentSelectionViewModel? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val documentsCoordinator = rememberUserDocumentsCoordinator()
    val selectionViewModel = documentsViewModel ?: remember { UserDocumentSelectionViewModel() }

    val uploadState by userViewModel.uploadState.collectAsStateWithLifecycle()
    val user by userViewModel.userData.collectAsStateWithLifecycle()
    val documentsUiState by selectionViewModel.uiState.collectAsStateWithLifecycle()

    var showSheet by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri
            ?.let(documentsCoordinator::loadDocument)
            ?.let { document ->
                selectionViewModel.onDocumentSelected(document.name, document.bytes)
            }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            documentsUiState.tempCameraUri
                ?.let(android.net.Uri::parse)
                ?.let(documentsCoordinator::loadDocument)
                ?.let { document ->
                    selectionViewModel.onDocumentSelected(document.name, document.bytes)
                }
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri
            ?.let(documentsCoordinator::loadDocument)
            ?.let { document ->
                selectionViewModel.onDocumentSelected(document.name, document.bytes)
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
            Button(onClick = { showSheet = true }) {
                Text("Seleccionar archivo")
            }

            Spacer(Modifier.height(12.dp))

            if (documentsUiState.selectedDocumentName.isNotBlank()) {
                Text("Archivo: ${documentsUiState.selectedDocumentName}")
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val documentBytes = documentsUiState.selectedDocumentBytes ?: return@Button
                        val documentName = documentsUiState.selectedDocumentName
                        user?.id?.let { userViewModel.uploadDocument(it, documentName, documentBytes) }
                    },
                    enabled = documentsUiState.selectedDocumentBytes != null && uploadState !is UploadState.Loading
                ) {
                    if (uploadState is UploadState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Subir")
                }
            }

            if (showSheet) {
                DocumentsSheet(
                    showSheet = true,
                    onDismiss = { showSheet = false },
                    onCamera = {
                        showSheet = false
                        val tempCameraUri = documentsCoordinator.createTempCameraUri()
                        selectionViewModel.setTempCameraUri(tempCameraUri.toString())
                        takePhotoLauncher.launch(tempCameraUri)
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

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                selectionViewModel.clearSelection()
                snackbarHostState.showSnackbar("Archivo subido correctamente")
                userViewModel.resetUploadState()
                navController.popBackStack()
            }
            is UploadState.Error -> {
                snackbarHostState.showSnackbar("Error al subir: ${(uploadState as UploadState.Error).message}")
                userViewModel.resetUploadState()
                navController.popBackStack()
            }
            else -> Unit
        }
    }
}
