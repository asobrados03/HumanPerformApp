package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import com.humanperformcenter.shared.presentation.ui.UploadState
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentsViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserDocumentsViewModelTest {

    private class FakeUserDocumentsRepository(
        private val result: Result<String>
    ) : UserDocumentsRepository {
        override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> = result
    }

    @Test
    fun userDocumentsViewModel_whenUploadSucceeds_emitsLoadingThenSuccess() = runTest {
        // Arrange
        val viewModel = UserDocumentsViewModel(
            UserDocumentUseCase(FakeUserDocumentsRepository(Result.success("Subido")))
        )

        // Act + Assert
        viewModel.uploadState.test {
            assertEquals(UploadState.Idle, awaitItem())
            viewModel.uploadDocument(1, "dni.pdf", byteArrayOf(1, 2))
            assertEquals(UploadState.Loading, awaitItem())
            assertEquals(UploadState.Success("Subido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun userDocumentsViewModel_whenUploadFails_emitsLoadingThenError() = runTest {
        // Arrange
        val viewModel = UserDocumentsViewModel(
            UserDocumentUseCase(FakeUserDocumentsRepository(Result.failure(IllegalStateException("Fallo"))))
        )

        // Act + Assert
        viewModel.uploadState.test {
            assertEquals(UploadState.Idle, awaitItem())
            viewModel.uploadDocument(1, "dni.pdf", byteArrayOf(1, 2))
            assertEquals(UploadState.Loading, awaitItem())
            assertEquals(UploadState.Error("Fallo"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
