package hu.bme.aut.stufftracker.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @ColumnInfo(name="name")var name: String? = null
) {
    @ColumnInfo(name="id") @PrimaryKey(autoGenerate = true) var id: Long? = null
}