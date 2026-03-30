package com.humanperformcenter.shared.presentation

import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentSelectionViewModel
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDocumentSelectionViewModelTest {

    @Test
    fun setTempCameraUri_when_called_updates_only_temp_uri() {
        val viewModel = UserDocumentSelectionViewModel()

        viewModel.setTempCameraUri("content://camera/temp")

        val state = viewModel.uiState.value
        assertEquals("content://camera/temp", state.tempCameraUri)
        assertEquals("", state.selectedDocumentName)
        assertNull(state.selectedDocumentBytes)
    }

    @Test
    fun onDocumentSelected_when_called_updates_document_and_clears_temp_uri() {
        val viewModel = UserDocumentSelectionViewModel()
        val bytes = byteArrayOf(1, 2, 3)
        viewModel.setTempCameraUri("content://camera/temp")

        viewModel.onDocumentSelected("dni.pdf", bytes)

        val state = viewModel.uiState.value
        assertEquals("dni.pdf", state.selectedDocumentName)
        assertContentEquals(bytes, state.selectedDocumentBytes)
        assertNull(state.tempCameraUri)
    }

    @Test
    fun clearSelection_when_called_resets_all_fields() {
        val viewModel = UserDocumentSelectionViewModel()
        viewModel.setTempCameraUri("content://camera/temp")
        viewModel.onDocumentSelected("dni.pdf", byteArrayOf(1, 2, 3))

        viewModel.clearSelection()

        val state = viewModel.uiState.value
        assertEquals("", state.selectedDocumentName)
        assertNull(state.selectedDocumentBytes)
        assertNull(state.tempCameraUri)
    }
}
