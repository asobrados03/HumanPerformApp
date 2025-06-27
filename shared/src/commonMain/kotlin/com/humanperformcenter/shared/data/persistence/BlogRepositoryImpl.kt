package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.BlogEntry
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.BlogRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object BlogRepositoryImpl: BlogRepository {
    override suspend fun readBlogs(): Result<List<BlogEntry>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Hacemos la petición GET al endpoint /blogs
            val resp: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/blogs") {
                contentType(ContentType.Application.Json)
            }

            // Comprobamos el código HTTP
            if (resp.status == HttpStatusCode.OK) {
                // Deserializamos la lista de BlogResponse
                val blogs: List<BlogEntry> = resp.body()
                Result.success(blogs)
            } else {
                Result.failure(Exception("Error al leer blogs: código HTTP ${resp.status.value}"))
            }
        } catch (e: Exception) {
            // Cualquier excepción (timeout, fallo de red, parseo…) cae aquí
            Result.failure(e)
        }
    }
}