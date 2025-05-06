package es.uva.sg.psm.planificadorfinanciero.data

import es.uva.sg.psm.planificadorfinanciero.data.Session

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val dao: SessionDao) {
    fun getAllSessions(): Flow<List<Session>> = dao.getAllSessions()

    suspend fun insert(session: Session) = dao.insert(session)

    suspend fun delete(session: Session) = dao.delete(session)
}