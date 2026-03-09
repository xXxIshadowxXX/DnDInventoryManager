package com.example.dndinventorymanager.data.network

import retrofit2.http.GET
import retrofit2.http.Path

interface DndApi {
    @GET("api/equipment")
    suspend fun getEquipmentIndex(): EquipmentIndexResponse

    @GET("api/magic-items")
    suspend fun getMagicItemsIndex(): EquipmentIndexResponse

    @GET("api/equipment/{index}")
    suspend fun getEquipmentDetail(@Path("index") index: String): EquipmentDetailResponse

    @GET("api/magic-items/{index}")
    suspend fun getMagicItemDetail(@Path("index") index: String): EquipmentDetailResponse

    @GET("api/spells")
    suspend fun getSpellsIndex(): SpellIndexResponse

    @GET("api/spells/{index}")
    suspend fun getSpellDetail(@Path("index") index: String): SpellDetailResponse
}

data class EquipmentIndexResponse(
    val count: Int,
    val results: List<EquipmentIndexItem>
)

data class EquipmentIndexItem(
    val index: String,
    val name: String,
    val url: String
)

data class EquipmentDetailResponse(
    val index: String,
    val name: String,
    val equipment_category: EquipmentCategory?,
    val weapon_category: String?,
    val weapon_range: String?,
    val category_range: String?,
    val damage: Damage?,
    val range: Range?,
    val throw_range: Range?,
    val properties: List<Property>?,
    val weight: Double?,
    val cost: Cost?,
    val desc: List<String>?,
    val rarity: RarityDetail?
)

data class EquipmentCategory(
    val index: String,
    val name: String
)

data class Cost(
    val quantity: Double?,
    val unit: String?
)

data class RarityDetail(
    val name: String
)

data class Damage(
    val damage_dice: String?,
    val damage_type: DamageType?
)

data class DamageType(
    val index: String,
    val name: String
)

data class Range(
    val normal: Int?,
    val long: Int?
)

data class Property(
    val index: String,
    val name: String
)

data class SpellIndexResponse(
    val count: Int,
    val results: List<SpellIndexItem>
)

data class SpellIndexItem(
    val index: String,
    val name: String,
    val url: String
)

data class SpellDetailResponse(
    val index: String,
    val name: String,
    val desc: List<String>,
    val higher_level: List<String>? = null,
    val range: String,
    val components: List<String>,
    val material: String? = null,
    val ritual: Boolean,
    val duration: String,
    val concentration: Boolean,
    val casting_time: String,
    val level: Int,
    val school: School,
    val classes: List<ClassItem>
)

data class School(val name: String)
data class ClassItem(val name: String)
