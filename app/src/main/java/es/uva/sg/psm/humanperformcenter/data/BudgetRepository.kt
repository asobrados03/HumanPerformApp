package es.uva.sg.psm.humanperformcenter.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    suspend fun addBudget(budget: Budget) {
        budgetDao.addBudget(budget)
    }

    fun getBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetById(id: Long): Flow<Budget> {
        return budgetDao.getBudgetById(id)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    fun getBudgetForCategoryMonthAndYear(categoryId: Long, month: String, year: Int): Flow<Budget?> {
        return budgetDao.getBudgetForCategoryMonthAndYear(categoryId, month, year)
    }
}