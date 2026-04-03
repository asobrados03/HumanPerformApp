package com.humanperformcenter.shared.presentation.viewmodel

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Guardamos la URI temporal como String para evitar dependencias Android/iOS específicas en el ViewModel KMM.
data class UserDocumentSelectionUiState(
    val selectedDocumentName: String = "",
    val selectedDocumentBytes: ByteArray? = null,
    val tempCameraUri: String? = null
)

class UserDocumentSelectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(viewModelScope, UserDocumentSelectionUiState())

    @NativeCoroutinesState
    val uiState: StateFlow<UserDocumentSelectionUiState> = _uiState.asStateFlow()

    fun setTempCameraUri(uri: String?) {
        _uiState.update { currentState ->
            currentState.copy(tempCameraUri = uri)
        }
    }

    fun onDocumentSelected(name: String, bytes: ByteArray) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDocumentName = name,
                selectedDocumentBytes = bytes,
                tempCameraUri = null
            )
        }
    }

    fun clearSelection() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDocumentName = "",
                selectedDocumentBytes = null,
                tempCameraUri = null
            )
        }
    }
}
