package com.humanperformcenter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Transaction",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,           // Entidad padre
            parentColumns = ["id"],     // Columna PK en Category
            childColumns = ["transaction-category"],        // Columna FK en Transaction
            onDelete = ForeignKey.CASCADE,      // Acción al eliminar
            onUpdate = ForeignKey.CASCADE       // Acción al actualizar
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "transaction-type")
    val type: TransactionType = TransactionType.Ingreso,
    @ColumnInfo(name = "transaction-amount")
    val amount: Double = 0.0,
    @ColumnInfo(name = "transaction-category", index = true)
    val category: Long = 0L,
    @ColumnInfo(name = "transaction-date")
    val date: Long = 0L,
    @ColumnInfo(name = "transaction-description")
    val description: String = ""
)