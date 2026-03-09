package com.example.dndinventorymanager.data.network

import retrofit2.http.GET
import retrofit2.http.Url

interface Open5eApi {
    @GET
    suspend fun getMagicItems(@Url url: String): Open5eResponse

    @GET
    suspend fun getSpells(@Url url: String): Open5eSpellResponse
}

data class Open5eResponse(
    val count: Int,
    val next: String?,
    val results: List<Open5eMagicItem>
)

data class Open5eMagicItem(
    val slug: String,
    val name: String,
    val type: String,
    val desc: String,
    val rarity: String,
    val requires_attunement: String?,
    val document__title: String?
)

data class Open5eSpellResponse(
    val count: Int,
    val next: String?,
    val results: List<Open5eSpell>
)

data class Open5eSpell(
    val slug: String,
    val name: String,
    val desc: String,
    val higher_level: String? = null,
    val range: String? = null,
    val target: String? = null,
    val components: String? = null,
    val material: String? = null,
    val ritual: String? = null,
    val duration: String? = null,
    val concentration: String? = null,
    val casting_time: String? = null,
    val level: String? = null,
    val level_int: Int? = null,
    val school: String? = null,
    val dnd_class: String? = null,
    val archetype: String? = null,
    val circles: String? = null,
    val document__title: String? = null,
    val damage_dice: String? = null,
    val damage_type: String? = null
)
