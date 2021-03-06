package hu.bme.aut.stufftracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hu.bme.aut.stufftracker.dao.CategoryDAO
import hu.bme.aut.stufftracker.dao.MyAddressDAO
import hu.bme.aut.stufftracker.dao.StuffDAO
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff

@Database(entities = [Stuff::class, MyAddress::class, Category::class], version = 1)
abstract class StuffDatabase : RoomDatabase() {
    abstract fun categoryDAO(): CategoryDAO
    abstract fun myAddressDAO(): MyAddressDAO
    abstract fun stuffDAO(): StuffDAO

    companion object {

        @Volatile
        private var dbInstance: StuffDatabase? = null

        @JvmStatic
        fun getDatabase(applicationContext: Context): StuffDatabase {
            val localInstance = dbInstance

            if (localInstance != null) {
                return localInstance
            }

            return return synchronized(this) {
                var instance = dbInstance
                if(instance == null){
                    instance = Room.databaseBuilder(
                        applicationContext,
                        StuffDatabase::class.java,
                        "stuff-db"
                    ).build()
                    dbInstance = instance
                }
                instance
            }
        }
    }
}