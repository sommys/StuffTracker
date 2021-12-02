package hu.bme.aut.stufftracker.dao

import androidx.room.*
import hu.bme.aut.stufftracker.domain.MyAddress

@Dao
interface MyAddressDAO {
    @Query("SELECT * FROM addresses")
    fun getAll(): List<MyAddress>

    @Insert
    fun insert(address: MyAddress): Long

    @Update
    fun update(address: MyAddress)

    @Delete
    fun deleteItem(address: MyAddress)

    @Query("SELECT * FROM addresses WHERE id != :id")
    abstract fun getOthers(id: Long): List<MyAddress>
}