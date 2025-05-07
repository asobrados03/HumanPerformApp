package es.uva.sg.psm.humanperformcenter

import android.content.Context
import androidx.room.Room
import es.uva.sg.psm.humanperformcenter.data.BudgetRepository
import es.uva.sg.psm.humanperformcenter.data.CategoryRepository
import es.uva.sg.psm.humanperformcenter.data.FinancialDatabase
import es.uva.sg.psm.humanperformcenter.data.TransactionRepository

object Graph {
    private lateinit var database: FinancialDatabase

    val transactionRepository by lazy {
        TransactionRepository(transactionDao = database.transactionDao())
    }

    val categoryRepository by lazy {
        CategoryRepository(categoryDao = database.categoryDao())
    }

    val budgetRepository by lazy {
        BudgetRepository(budgetDao = database.budgetDao())
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(
            context,
            FinancialDatabase::class.java,
            "financialapp.db"
        ).build()
    }
}