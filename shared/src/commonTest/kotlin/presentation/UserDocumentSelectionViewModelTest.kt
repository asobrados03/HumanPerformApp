package com.humanperformcenter.shared.presentation

import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentSelectionViewModel
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDocumentSelectionViewModelTest {

    @Test
    fun setTempCameraUri_updatesOnlyTempUri() {
        val viewModel = UserDocumentSelectionViewModel()

        viewModel.setTempCameraUri("content://camera/temp")

        val state = viewModel.uiState.value
        assertEquals("content://camera/temp", state.tempCameraUri)
        assertEquals("", state.selectedDocumentName)
        assertNull(state.selectedDocumentBytes)
    }

    @Test
    fun onDocumentSelected_setsDocumentAndClearsTempUri() {
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
    fun clearSelection_resetsAllSelectionFields() {
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
