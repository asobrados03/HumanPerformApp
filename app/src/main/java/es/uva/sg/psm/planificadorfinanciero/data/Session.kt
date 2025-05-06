package es.uva.sg.psm.planificadorfinanciero.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Session")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "session-service")
    val service: String = "",

    @ColumnInfo(name = "session-product")
    val product: String = "",

    @ColumnInfo(name = "session-date")
    val date: Long = 0L,

    @ColumnInfo(name = "session-hour")
    val hour: String = "",

    @ColumnInfo(name = "session-professional")
    val professional: String = ""
)