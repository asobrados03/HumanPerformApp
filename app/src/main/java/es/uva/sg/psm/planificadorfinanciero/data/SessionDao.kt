package es.uva.sg.psm.planificadorfinanciero.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM Session ORDER BY `session-date` DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session)

    @Delete
    suspend fun delete(session: Session)
}