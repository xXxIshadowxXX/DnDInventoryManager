package com.example.dndinventorymanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spells")
data class SpellEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val level: Int,
    val school: String,
    val castingTime: String,
    val range: String,
    val components: String,
    val duration: String,
    val description: String,
    val sourcebook: String = "SRD",
    val classes: String = "", // Comma separated list
    val target: String = "",
    val damageRoll: String = "",
    val higherLevel: String = "",
    val verbal: Boolean = false,
    val somatic: Boolean = false,
    val material: String = ""
)
