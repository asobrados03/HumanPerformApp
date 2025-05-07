package es.uva.sg.psm.humanperformcenter.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.sg.psm.humanperformcenter.Graph
import es.uva.sg.psm.humanperformcenter.data.Budget
import es.uva.sg.psm.humanperformcenter.data.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

class BudgetViewModel(
    private val budgetRepository: BudgetRepository = Graph.budgetRepository
) : ViewModel() {
    var budgetCategoryState by mutableLongStateOf(0L)
    var budgetMonthlyLimitState by mutableDoubleStateOf(0.0)
    var budgetCurrentExpenditureState by mutableDoubleStateOf(0.0)

    private val currentMoment = Clock.System.now()
    private val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())

    private val currentMonth = localDateTime.month.number.toString()
    private val currentYear = localDateTime.year

    var budgetMonthState by mutableStateOf(currentMonth)
    var budgetYearState by mutableIntStateOf(currentYear)

    fun onBudgetCategoryChanged(newCategory: Long) {
        budgetCategoryState = newCategory
    }

    fun onBudgetMonthlyLimitChanged(newMonthlyLimit: Double) {
        budgetMonthlyLimitState = newMonthlyLimit
    }

    fun onBudgetMonthChanged(newMonth: String) {
        budgetMonthState = newMonth
    }

    fun onBudgetYearChanged(newYear: Int) {
        budgetYearState = newYear
    }

    lateinit var getAllBudgets: Flow<List<Budget>>

    init {
        viewModelScope.launch {
            getAllBudgets = budgetRepository.getBudgets()
        }
    }

    fun addBudget(budget: Budget) {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.addBudget(budget)
        }
    }

    fun getBudgetById(id: Long): Flow<Budget> {
        return budgetRepository.getBudgetById(id)
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.deleteBudget(budget)
        }
    }

    fun getBudgetForCategoryMonthAndYear(categoryId: Long, month: String, year: Int): Flow<Budget?> {
        return budgetRepository.getBudgetForCategoryMonthAndYear(categoryId, month, year)
    }
}