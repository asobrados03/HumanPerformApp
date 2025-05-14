package com.humanperformcenter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val service: String,
    val product: String,
    val date: Long,
    val hour: String,
    val professional: String
)