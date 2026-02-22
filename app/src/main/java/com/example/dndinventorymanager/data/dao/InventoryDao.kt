package com.example.dndinventorymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.dndinventorymanager.data.entities.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE characterId = :characterId")
    fun getInventoryForCharacter(characterId: Long): Flow<List<InventoryItemEntity>>

    @Insert
    suspend fun insert(item: InventoryItemEntity): Long

    @Update
    suspend fun update(item: InventoryItemEntity)

    @Delete
    suspend fun delete(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE inventory_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)

    @Query("UPDATE inventory_items SET equipped = :equipped WHERE id = :id")
    suspend fun updateEquipped(id: Long, equipped: Boolean)

    @Query("UPDATE inventory_items SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("UPDATE inventory_items SET containerName = :containerName WHERE id = :id")
    suspend fun updateContainer(id: Long, containerName: String?)
}
