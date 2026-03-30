package com.humanperformcenter.shared.data.remote.common

import com.humanperformcenter.shared.data.model.ErrorResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

suspend fun HttpResponse.safeErrorBody(): String =
    try {
        body<ErrorResponse>().error
    } catch (_: Exception) {
        bodyAsText()
    }
