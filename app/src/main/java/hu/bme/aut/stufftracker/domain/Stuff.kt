package hu.bme.aut.stufftracker.domain

import androidx.room.*

@Entity(tableName = "stuff",
        foreignKeys = [
            ForeignKey(entity = MyAddress::class, parentColumns = arrayOf("id"), childColumns = arrayOf("addressId")),
            ForeignKey(entity = Category::class, parentColumns = arrayOf("id"), childColumns = arrayOf("categoryId"))
        ]
)
data class Stuff(
    @ColumnInfo(name="name") var name: String? = null,
    @ColumnInfo(name="quantity") var quantity: Int? = null,
    @ColumnInfo(name="description") var description: String? = null,
    @ColumnInfo(name="imageUrl") var imageURL: String? = null,
    @ColumnInfo(name="addressId") var addressId: Long? = null,
    @ColumnInfo(name="categoryId") var categoryId: Long? = null) {
    @ColumnInfo(name="id") @PrimaryKey(autoGenerate = true) var id: Long? = null
}