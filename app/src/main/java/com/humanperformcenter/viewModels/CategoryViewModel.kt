package com.humanperformcenter.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.Graph
import com.humanperformcenter.data.Category
import com.humanperformcenter.data.CategoryRepository
import com.humanperformcenter.data.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository = Graph.categoryRepository
) : ViewModel() {
    var categoryNameState by mutableStateOf("")
    var categoryTypeState by mutableStateOf(TransactionType.Ingreso)

    lateinit var getAllCategories: Flow<List<Category>>

    init {
        viewModelScope.launch {
            getAllCategories = categoryRepository.getCategories()
        }
    }

    fun getCategoryById(id: Long): Flow<Category> {
        return categoryRepository.getCategoryById(id)
    }

    fun getCategoryByName(name: String): Flow<Category?> {
        return categoryRepository.getCategoryByName(name)
    }

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return categoryRepository.getCategoriesByType(type)
    }
}