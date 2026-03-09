package com.example.dndinventorymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dndinventorymanager.data.entities.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterEntity)

    @Update
    suspend fun update(character: CharacterEntity)

    @Delete
    suspend fun delete(character: CharacterEntity)

    @Query("UPDATE characters SET currentSlots = :slots WHERE id = :id")
    suspend fun updateCurrentSlots(id: Long, slots: String)

    @Query("UPDATE characters SET spellSlots = :slots, maxPrepared = :maxPrepared WHERE id = :id")
    suspend fun updateSpellcasting(id: Long, slots: String, maxPrepared: Int)
}
