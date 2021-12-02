package hu.bme.aut.stufftracker.dao

import androidx.room.*
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff

@Dao
interface StuffDAO {
    @Query("SELECT * FROM stuff")
    fun getAll(): List<Stuff>

    @Query("SELECT * FROM stuff WHERE id = :id")
    fun getStuffById(id: Long): Stuff

    @Query("SELECT * FROM stuff WHERE addressId = :addressid AND categoryId = :categoryid" )
    fun getStuffWithAddressAndCategory(addressid : Long, categoryid: Long): List<Stuff>

    @Query("SELECT * FROM stuff WHERE addressId = :addressid")
    fun getStuffWithAddress(addressid: Long): List<Stuff>

    @Insert
    fun insert(shoppingItems: Stuff): Long

    @Update
    fun update(shoppingItem: Stuff)

    @Delete
    fun deleteItem(shoppingItem: Stuff)

    @Query("DELETE FROM stuff WHERE addressId = :addressid")
    fun deleteAddress(addressid: Long)

    @Query("DELETE FROM stuff WHERE categoryId = :categoryid")
    fun deleteCategory(categoryid: Long)

    @Query("UPDATE stuff SET addressId=:id WHERE id=:stuffId")
    fun changeAddress(id: Long, stuffId: Long)
}