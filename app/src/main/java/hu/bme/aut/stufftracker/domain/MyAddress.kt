package hu.bme.aut.stufftracker.domain

import android.location.Address
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*
@Entity(tableName = "addresses")
data class MyAddress(
    @ColumnInfo(name="zip")var zipCode: String? = null,
    @ColumnInfo(name="city")var city: String? = null,
    @ColumnInfo(name="country")var country: String? = null,
    @ColumnInfo(name="street")var street: String? = null,
    @ColumnInfo(name="streetNum")var streetNum: Int? = null,
    @ColumnInfo(name="alias")var alias: String? = null) {
    @ColumnInfo(name="id") @PrimaryKey(autoGenerate = true) var id: Long? = null
    override fun toString(): String {
        return "$alias\n($zipCode $city, $country, $street $streetNum)";
    }
}