package com.example.dndinventorymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.dndinventorymanager.data.entities.CustomItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomItemDao {
    @Query("SELECT * FROM customitemtable ORDER BY name")
    fun getCustomItems(): Flow<List<CustomItemEntity>>

    @Insert
    suspend fun insert(item: CustomItemEntity): Long

    @Update
    suspend fun update(item: CustomItemEntity)

    @Delete
    suspend fun delete(item: CustomItemEntity)
}
