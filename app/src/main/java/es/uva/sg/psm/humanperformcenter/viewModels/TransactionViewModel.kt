package es.uva.sg.psm.humanperformcenter.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.sg.psm.humanperformcenter.Graph
import es.uva.sg.psm.humanperformcenter.data.Budget
import es.uva.sg.psm.humanperformcenter.data.BudgetRepository
import es.uva.sg.psm.humanperformcenter.data.Category
import es.uva.sg.psm.humanperformcenter.data.CategoryRepository
import es.uva.sg.psm.humanperformcenter.data.Transaction
import es.uva.sg.psm.humanperformcenter.data.TransactionRepository
import es.uva.sg.psm.humanperformcenter.data.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TransactionViewModel(
    private val transactionRepository: TransactionRepository = Graph.transactionRepository,
    private val categoryRepository: CategoryRepository = Graph.categoryRepository,
    private val budgetRepository: BudgetRepository = Graph.budgetRepository
) : ViewModel() {
    var transactionTypeState by mutableStateOf(TransactionType.Ingreso)
    var transactionAmountState by mutableDoubleStateOf(0.0)
    var transactionCategoryState by mutableLongStateOf(0L)
    var transactionDateState by mutableLongStateOf(0L)
    var transactionDescriptionState by mutableStateOf("")

    fun onTransactionTypeChanged(newType: TransactionType) {
        transactionTypeState = newType
    }

    fun onTransactionAmountChanged(newAmount: Double) {
        transactionAmountState = newAmount
    }

    fun onTransactionDateChanged(newDate: Long) {
        transactionDateState = newDate
    }

    fun onTransactionDescriptionChanged(newString: String) {
        transactionDescriptionState = newString
    }

    lateinit var getAllTransactions: Flow<List<Transaction>>

    init {
        viewModelScope.launch {
            getAllTransactions = transactionRepository.getTransactions()
        }
    }

    fun getTransactionById(id: Long): Flow<Transaction> {
        return transactionRepository.getTransactionById(id)
    }

    fun deleteTransactionAndCheckCategory(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.deleteTransaction(transaction)

            val transactionCount = transactionRepository.getTransactionCountForCategory(transaction.category)
            if (transactionCount == 0) {
                val categoryToBeDeleted = categoryRepository.getCategoryById(transaction.category).first()
                categoryRepository.deleteCategory(categoryToBeDeleted)
            }
        }
    }

    fun getTransactionsByCategoryAndDate(categoryId: Long, year: Int, month: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByCategoryAndDate(categoryId, year, month)
    }

    private suspend fun handleCategory(categoryName: String, existingCategory: Category?): Long {
        return when {
            existingCategory == null -> {
                val newCategory = Category(
                    name = categoryName.trim(),
                    type = transactionTypeState
                )
                categoryRepository.addCategory(newCategory)
            }
            existingCategory.type != transactionTypeState -> {
                val newCategory = Category(
                    name = categoryName.trim(),
                    type = transactionTypeState
                )
                categoryRepository.addCategory(newCategory)
            }
            else -> existingCategory.id
        }
    }

    fun saveTransaction(
        id: Long,
        categoryName: String,
        category: Category?,
        onError: suspend (String) -> Unit,
        onSuccess: suspend () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val categoryId = handleCategory(categoryName, category)
                val newTransaction = Transaction(
                    id = id,
                    type = transactionTypeState,
                    amount = transactionAmountState,
                    description = transactionDescriptionState.trim(),
                    category = categoryId,
                    date = transactionDateState
                )

                if (id != 0L) {
                    // Obtener la transacción original antes de actualizarla
                    val oldTransaction = transactionRepository.getTransactionById(id).first()
                    updateTransactionAndBudget(oldTransaction, newTransaction)
                } else {
                    addTransactionAndUpdateBudget(newTransaction)
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error desconocido")
                }
            }
        }
    }

    private suspend fun updateTransactionAndBudget(oldTransaction: Transaction, newTransaction: Transaction) {
        // Solo actualizamos presupuestos para gastos
        if (oldTransaction.type == TransactionType.Gasto) {
            val oldBudget = findBudget(oldTransaction.category, oldTransaction.date)

            oldBudget?.let { budget ->
                // Revertir el gasto anterior
                val updatedAmount = budget.currentExpenditure - oldTransaction.amount
                budgetRepository.updateBudget(budget.copy(currentExpenditure = updatedAmount))
            }
        }

        // Si la nueva transacción es un gasto, actualizamos el nuevo presupuesto
        if (newTransaction.type == TransactionType.Gasto) {
            val newBudget = if (hasChangedCategoryOrDate(oldTransaction, newTransaction)) {
                findBudget(newTransaction.category, newTransaction.date)
            } else {
                findBudget(oldTransaction.category, oldTransaction.date)
            }

            newBudget?.let { budget ->
                val updatedAmount = budget.currentExpenditure + newTransaction.amount
                budgetRepository.updateBudget(budget.copy(currentExpenditure = updatedAmount))
            }
        }

        // Actualizar la transacción
        transactionRepository.updateTransaction(newTransaction)
    }

    private suspend fun addTransactionAndUpdateBudget(transaction: Transaction) {
        // Agregar la transacción
        transactionRepository.addTransaction(transaction)

        // Solo actualizar presupuesto si es un gasto
        if (transaction.type == TransactionType.Gasto) {
            val budget = findBudget(transaction.category, transaction.date)
            budget?.let {
                val updatedAmount = it.currentExpenditure + transaction.amount
                budgetRepository.updateBudget(it.copy(currentExpenditure = updatedAmount))
            }
        }
    }

    private suspend fun findBudget(categoryId: Long, date: Long): Budget? {
        val instant = Instant.fromEpochMilliseconds(date)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        val year = localDate.year
        // Asegurar formato "01", "02", etc.
        val month = localDate.monthNumber.toString().padStart(2, '0')

        return budgetRepository.getBudgetForCategoryMonthAndYear(
            categoryId = categoryId,
            year = year,
            month = month
        ).first()
    }

    private fun hasChangedCategoryOrDate(oldTransaction: Transaction, newTransaction: Transaction): Boolean {
        return oldTransaction.category != newTransaction.category ||
                oldTransaction.date != newTransaction.date
    }
}