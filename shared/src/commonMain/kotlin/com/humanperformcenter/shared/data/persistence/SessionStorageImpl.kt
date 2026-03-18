package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.storage.SessionStorage

object SessionStorageImpl : SessionStorage {
    override suspend fun clearSession() {
        SecureStorage.clear()
    }
}
