package com.humanperformcenter.ui.viewmodel.state

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}
