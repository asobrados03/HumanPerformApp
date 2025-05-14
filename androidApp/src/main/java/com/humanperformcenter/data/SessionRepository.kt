package com.humanperformcenter.data

import com.humanperformcenter.data.Session
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    suspend fun insert(session: Session) = sessionDao.insert(session)
    suspend fun delete(session: Session) = sessionDao.delete(session)
    fun getAllSessions() = sessionDao.getAllSessions()
    fun getSession() = {}
}