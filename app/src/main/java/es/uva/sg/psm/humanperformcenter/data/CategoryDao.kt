package es.uva.sg.psm.humanperformcenter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addCategory(categoryEntity: Category): Long // Room automáticamente devuelve el ID generado

    @Query("Select * from `Category`")
    abstract fun getAllCategories(): Flow<List<Category>>

    @Update
    abstract suspend fun updateCategory(categoryEntity: Category)

    @Delete
    abstract suspend fun deleteCategory(category: Category)

    @Query("Select * from `Category` where id=:id")
    abstract fun getCategoryById(id:Long): Flow<Category>

    @Query("SELECT * FROM `Category` WHERE name=:name LIMIT 1")
    abstract fun getCategoryByName(name: String): Flow<Category>

    @Query("SELECT * FROM `Category` WHERE type=:type")
    abstract fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

}