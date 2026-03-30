package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.domain.storage.SessionStorage

class SessionStorageImpl(
    private val authLocalDataSource: AuthLocalDataSource,
) : SessionStorage {
    override suspend fun clearSession() {
        authLocalDataSource.clearSession()
    }
}
