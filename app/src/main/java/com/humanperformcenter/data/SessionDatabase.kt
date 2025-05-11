package com.humanperformcenter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionDao

@Database(entities = [Session::class], version = 1)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: SessionDatabase? = null

        fun getDatabase(context: Context): SessionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SessionDatabase::class.java,
                    "session_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}