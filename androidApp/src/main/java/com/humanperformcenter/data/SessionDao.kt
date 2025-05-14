package com.humanperformcenter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session)

    @Delete
    suspend fun delete(session: Session)

    @Query("SELECT * FROM sessions ORDER BY date ASC")
    fun getAllSessions(): Flow<List<Session>>
}