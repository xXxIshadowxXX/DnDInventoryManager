package com.example.dndinventorymanager.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dndinventorymanager.data.entities.SpellEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpellDao {
    @Query("SELECT * FROM spells ORDER BY level ASC, name ASC")
    fun getSpells(): Flow<List<SpellEntity>>

    @Query("SELECT name FROM spells")
    suspend fun getAllNames(): List<String>

    @Query("SELECT id FROM spells")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM spells WHERE id = :id")
    suspend fun getSpellById(id: String): SpellEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(spells: List<SpellEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(spells: List<SpellEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spell: SpellEntity)

    @Query("DELETE FROM spells WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM spells")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM spells")
    suspend fun count(): Int
}
