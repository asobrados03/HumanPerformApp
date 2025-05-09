package com.humanperformcenter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Budget",
    indices = [Index(
        value = ["budget-month", "budget-year", "budget-category"],
        unique = true
    )],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,           // Entidad padre
            parentColumns = ["id"],     // Columna PK en Category
            childColumns = ["budget-category"],        // Columna FK en Budget
            onDelete = ForeignKey.CASCADE,      // Acción al eliminar
            onUpdate = ForeignKey.CASCADE       // Acción al actualizar
        )
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "budget-category")
    val category: Long = 0L,
    @ColumnInfo(name = "budget-monthlyLimit")
    val monthlyLimit: Double = 0.0,
    @ColumnInfo(name = "budget-currentExpenditure")
    val currentExpenditure: Double = 0.0,
    @ColumnInfo(name = "budget-month")
    val month: String = "0", // 01: Enero, 02: Febrero, ... ,y 12: Diciembre
    @ColumnInfo(name = "budget-year")
    val year: Int = 0
)