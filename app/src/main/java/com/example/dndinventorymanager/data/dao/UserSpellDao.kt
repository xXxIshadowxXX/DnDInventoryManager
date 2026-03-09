package com.example.dndinventorymanager.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dndinventorymanager.data.entities.UserSpellEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSpellDao {
    @Query("SELECT * FROM user_spells WHERE characterId = :characterId")
    fun getUserSpells(characterId: Long): Flow<List<UserSpellEntity>>

    @Query("SELECT * FROM user_spells WHERE id = :id")
    suspend fun getUserSpellById(id: Long): UserSpellEntity?

    @Query("""
        SELECT COUNT(*) FROM user_spells 
        JOIN spells ON user_spells.spellId = spells.id 
        WHERE user_spells.characterId = :characterId 
        AND user_spells.isPrepared = 1 
        AND spells.level > 0
    """)
    suspend fun getPreparedCount(characterId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSpell: UserSpellEntity)

    @Query("UPDATE user_spells SET isPrepared = :isPrepared WHERE id = :id")
    suspend fun updatePrepared(id: Long, isPrepared: Boolean)

    @Query("UPDATE user_spells SET isPrepared = (loadouts LIKE '%' || :loadoutName || '%') WHERE characterId = :characterId")
    suspend fun applyLoadout(characterId: Long, loadoutName: String)
    
    @Query("UPDATE user_spells SET loadouts = :loadouts WHERE id = :id")
    suspend fun updateLoadouts(id: Long, loadouts: String)

    @Query("DELETE FROM user_spells WHERE id = :id")
    suspend fun deleteById(id: Long)
}
