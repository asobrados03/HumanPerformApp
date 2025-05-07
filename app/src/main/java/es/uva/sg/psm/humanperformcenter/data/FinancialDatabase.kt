package es.uva.sg.psm.humanperformcenter.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Transaction::class,
        Category::class,
        Budget::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinancialDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
}