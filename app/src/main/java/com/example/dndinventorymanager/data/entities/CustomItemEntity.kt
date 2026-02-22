package com.example.dndinventorymanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customitemtable")
data class CustomItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val rarity: String,
    val sourcebook: String,
    val description: String,
    val weight: String = "",
    val value: String = "",
    val category: String = "",
    val type: String = "",
    val damage: String? = null,
    val range: String? = null,
    val properties: String? = null
)
