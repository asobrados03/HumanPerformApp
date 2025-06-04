package com.humanperformcenter.data

class SessionRepository(private val sessionDao: SessionDao) {
    suspend fun insert(session: Session) = sessionDao.insert(session)
    suspend fun delete(session: Session) = sessionDao.delete(session)
    fun getAllSessions() = sessionDao.getAllSessions()
    fun getSession() = {}
}