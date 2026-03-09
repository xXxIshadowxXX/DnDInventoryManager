package com.example.dndinventorymanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val clazz: String,
    val level: Int,
    val gold: Int,
    val spellSlots: String = "0,0,0,0,0,0,0,0,0", // Max slots CSV for levels 1-9
    val currentSlots: String = "0,0,0,0,0,0,0,0,0", // Current available slots CSV
    val maxPrepared: Int = 0
)
