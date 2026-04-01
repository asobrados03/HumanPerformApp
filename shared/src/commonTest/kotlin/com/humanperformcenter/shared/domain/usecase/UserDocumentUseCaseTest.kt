package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserDocumentUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun uploadDocument_cuandoDatosValidos_devuelveRutaDocumento() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.success("doc://123")))
        assertEquals("doc://123", useCase.uploadDocument(1, "dni.pdf", byteArrayOf(1)).getOrNull())
    }

    @Test
    fun uploadDocument_cuandoNombreVacio_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("name required"))))
        assertTrue(useCase.uploadDocument(1, "", byteArrayOf(1)).isFailure)
    }

    @Test
    fun uploadDocument_cuandoArchivoVacio_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("file empty"))))
        assertTrue(useCase.uploadDocument(1, "dni.pdf", byteArrayOf()).isFailure)
    }

    @Test
    fun uploadDocument_cuandoRepositorioFalla_propagaFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(RuntimeException("503"))))
        assertTrue(useCase.uploadDocument(1, "dni.pdf", byteArrayOf(2)).isFailure)
    }

    private fun buildUseCase(repo: UserDocumentsRepository): UserDocumentUseCase {
        startKoin { modules(module { single<UserDocumentsRepository> { repo }; single { UserDocumentUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(private val result: Result<String>) : UserDocumentsRepository {
        override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray) = result
    }
}
