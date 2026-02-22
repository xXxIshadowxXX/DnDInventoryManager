package com.example.dndinventorymanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dndinventorymanager.data.dao.CharacterDao
import com.example.dndinventorymanager.data.dao.CustomItemDao
import com.example.dndinventorymanager.data.dao.InventoryDao
import com.example.dndinventorymanager.data.dao.ItemDao
import com.example.dndinventorymanager.data.entities.CharacterEntity
import com.example.dndinventorymanager.data.entities.CustomItemEntity
import com.example.dndinventorymanager.data.entities.InventoryItemEntity
import com.example.dndinventorymanager.data.entities.ItemEntity

@Database(
    entities = [
        CharacterEntity::class,
        ItemEntity::class,
        CustomItemEntity::class,
        InventoryItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun itemDao(): ItemDao
    abstract fun customItemDao(): CustomItemDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dnd_inventory.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
