package es.uva.sg.psm.humanperformcenter.data

import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    suspend fun addCategory(category: Category): Long{
        return categoryDao.addCategory(category)
    }

    fun getCategories() : Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoryById(id: Long): Flow<Category> {
        return categoryDao.getCategoryById(id)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    fun getCategoryByName(name: String): Flow<Category> {
        return categoryDao.getCategoryByName(name)
    }

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> = categoryDao.getCategoriesByType(type)
}