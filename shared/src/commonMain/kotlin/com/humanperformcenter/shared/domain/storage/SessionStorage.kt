package com.humanperformcenter.shared.domain.storage

interface SessionStorage {
    suspend fun clearSession()
}
