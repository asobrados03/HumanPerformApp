package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import com.humanperformcenter.shared.presentation.ui.UploadState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserDocumentsViewModel(
    private val userDocumentUseCase: UserDocumentUseCase
) : ViewModel() {
    private val _uploadState = MutableStateFlow<UploadState>(viewModelScope, UploadState.Idle)
    @NativeCoroutinesState
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadDocument(userId: Int, name: String, data: ByteArray) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            userDocumentUseCase.uploadDocument(userId, name, data)
                .onSuccess { message ->
                    _uploadState.value = UploadState.Success(message)
                }
                .onFailure { throwable ->
                    _uploadState.value = UploadState.Error(
                        throwable.message.orEmpty().ifEmpty {
                            "Error desconocido al subir documento"
                        }
                    )
                }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}
