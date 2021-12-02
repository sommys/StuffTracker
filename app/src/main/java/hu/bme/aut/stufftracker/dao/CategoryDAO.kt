package hu.bme.aut.stufftracker.dao

import androidx.room.*
import hu.bme.aut.stufftracker.domain.Category

@Dao
interface CategoryDAO {
    @Query("SELECT * FROM categories")
    fun getAll(): List<Category>

    @Query("SELECT * FROM categories WHERE name = :name")
    fun getByName(name: String): Category

    @Insert
    fun insert(shoppingItems: Category): Long

    @Update
    fun update(shoppingItem: Category)

    @Delete
    fun deleteItem(shoppingItem: Category)

    @Query("SELECT * FROM categories WHERE id=:categoryId")
    fun getById(categoryId: Long): Category
}