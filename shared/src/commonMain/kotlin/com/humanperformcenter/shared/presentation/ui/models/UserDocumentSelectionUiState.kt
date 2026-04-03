package com.humanperformcenter.shared.presentation.ui.models

data class UserDocumentSelectionUiState(
    val selectedDocumentName: String = "",
    val selectedDocumentBytes: ByteArray? = null,
    val tempCameraUri: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UserDocumentSelectionUiState

        if (selectedDocumentName != other.selectedDocumentName) return false
        if (!selectedDocumentBytes.contentEquals(other.selectedDocumentBytes)) return false
        if (tempCameraUri != other.tempCameraUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectedDocumentName.hashCode()
        result = 31 * result + (selectedDocumentBytes?.contentHashCode() ?: 0)
        result = 31 * result + (tempCameraUri?.hashCode() ?: 0)
        return result
    }
}
