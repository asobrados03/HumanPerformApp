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

    private fun buildViewModel(result: Result<String>) =
        UserDocumentsViewModel(UserDocumentUseCase(FakeUserDocumentsRepository(result)))

    @Test
    fun uploadDocument_when_success_emits_loading_then_success() = runTest {
        val viewModel = buildViewModel(Result.success("Subido"))

        viewModel.uploadState.test {
            assertEquals(UploadState.Idle, awaitItem())
            viewModel.uploadDocument(1, "dni.pdf", byteArrayOf(1, 2))
            assertEquals(UploadState.Loading, awaitItem())
            assertEquals(UploadState.Success("Subido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uploadDocument_when_failure_with_message_emits_error_with_that_message() = runTest {
        val viewModel = buildViewModel(Result.failure(IllegalStateException("Fallo")))

        viewModel.uploadState.test {
            assertEquals(UploadState.Idle, awaitItem())
            viewModel.uploadDocument(1, "dni.pdf", byteArrayOf(1, 2))
            assertEquals(UploadState.Loading, awaitItem())
            assertEquals(UploadState.Error("Fallo"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uploadDocument_when_failure_without_message_emits_fallback_error() = runTest {
        val viewModel = buildViewModel(Result.failure(IllegalStateException()))

        viewModel.uploadState.test {
            assertEquals(UploadState.Idle, awaitItem())
            viewModel.uploadDocument(1, "dni.pdf", byteArrayOf(1, 2))
            assertEquals(UploadState.Loading, awaitItem())
            assertEquals(UploadState.Error("Error desconocido al subir documento"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetUploadState_after_success_emits_idle() = runTest {
        val viewModel = buildViewModel(Result.success("Subido"))

        viewModel.uploadState.test {
            assertEquals(UploadState.Idle, awaitItem())
            viewModel.uploadDocument(1, "dni.pdf", byteArrayOf(1, 2))
            assertEquals(UploadState.Loading, awaitItem())
            assertEquals(UploadState.Success("Subido"), awaitItem())

            viewModel.resetUploadState()
            assertEquals(UploadState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
