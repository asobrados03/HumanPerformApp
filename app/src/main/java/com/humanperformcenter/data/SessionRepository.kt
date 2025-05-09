package com.humanperformcenter.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val dao: SessionDao) {
    fun getAllSessions(): Flow<List<Session>> = dao.getAllSessions()

    suspend fun insert(session: Session) = dao.insert(session)

    suspend fun delete(session: Session) = dao.delete(session)
}