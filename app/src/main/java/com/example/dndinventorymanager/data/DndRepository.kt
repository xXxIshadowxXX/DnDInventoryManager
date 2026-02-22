package com.example.dndinventorymanager.data

import com.example.dndinventorymanager.data.dao.CharacterDao
import com.example.dndinventorymanager.data.dao.CustomItemDao
import com.example.dndinventorymanager.data.dao.InventoryDao
import com.example.dndinventorymanager.data.dao.ItemDao
import com.example.dndinventorymanager.data.entities.CharacterEntity
import com.example.dndinventorymanager.data.entities.CustomItemEntity
import com.example.dndinventorymanager.data.entities.InventoryItemEntity
import com.example.dndinventorymanager.data.entities.ItemEntity
import com.example.dndinventorymanager.data.network.DndApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DndRepository(
    private val characterDao: CharacterDao,
    private val itemDao: ItemDao,
    private val customItemDao: CustomItemDao,
    private val inventoryDao: InventoryDao,
    private val settingsDataStore: SettingsDataStore,
    private val api: DndApi
) {
    val characters: Flow<List<CharacterEntity>> = characterDao.getCharacters()
    val items: Flow<List<ItemEntity>> = itemDao.getItems()
    val customItems: Flow<List<CustomItemEntity>> = customItemDao.getCustomItems()
    val activeCharacterId: Flow<Long?> = settingsDataStore.activeCharacterIdFlow

    fun activeCharacter(): Flow<CharacterEntity?> {
        return activeCharacterId.combine(characters) { id, list ->
            list.firstOrNull { it.id == id }
        }
    }

    fun inventoryForCharacter(characterId: Long): Flow<List<InventoryDisplayItem>> {
        return combine(
            inventoryDao.getInventoryForCharacter(characterId),
            items,
            customItems
        ) { inventoryItems, baseItems, customItemsList ->
            val baseMap = baseItems.associateBy { it.id }
            val customMap = customItemsList.associateBy { it.id.toString() }
            inventoryItems.mapNotNull { inventory ->
                when (inventory.itemType) {
                    InventoryType.BASE.name -> {
                        val item = baseMap[inventory.itemId] ?: return@mapNotNull null
                        InventoryDisplayItem(
                            id = inventory.id,
                            itemType = inventory.itemType,
                            itemId = inventory.itemId,
                            name = item.name,
                            rarity = item.rarity,
                            sourcebook = item.sourcebook,
                            description = item.description,
                            quantity = inventory.quantity,
                            equipped = inventory.equipped,
                            notes = inventory.notes,
                            containerName = inventory.containerName,
                            weight = item.weight,
                            value = item.value,
                            category = item.category,
                            type = item.type,
                            damage = item.damage,
                            range = item.range,
                            properties = item.properties
                        )
                    }

                    InventoryType.CUSTOM.name -> {
                        val item = customMap[inventory.itemId] ?: return@mapNotNull null
                        InventoryDisplayItem(
                            id = inventory.id,
                            itemType = inventory.itemType,
                            itemId = inventory.itemId,
                            name = item.name,
                            rarity = item.rarity,
                            sourcebook = item.sourcebook,
                            description = item.description,
                            quantity = inventory.quantity,
                            equipped = inventory.equipped,
                            notes = inventory.notes,
                            containerName = inventory.containerName,
                            weight = item.weight,
                            value = item.value,
                            category = item.category,
                            type = item.type,
                            damage = item.damage,
                            range = item.range,
                            properties = item.properties
                        )
                    }

                    else -> null
                }
            }
        }
    }

    suspend fun setActiveCharacter(id: Long?) {
        withContext(Dispatchers.IO) {
            settingsDataStore.setActiveCharacterId(id)
        }
    }

    suspend fun createCharacter(name: String, clazz: String, level: Int, gold: Int) {
        withContext(Dispatchers.IO) {
            characterDao.insert(CharacterEntity(name = name, clazz = clazz, level = level, gold = gold))
        }
    }

    suspend fun createBaseItem(item: ItemEntity) {
        withContext(Dispatchers.IO) {
            itemDao.insert(item)
        }
    }

    suspend fun updateBaseItem(item: ItemEntity) {
        withContext(Dispatchers.IO) {
            itemDao.insert(item)
        }
    }

    suspend fun deleteBaseItem(id: String) {
        withContext(Dispatchers.IO) {
            itemDao.deleteById(id)
        }
    }

    suspend fun deleteAllBaseItems() {
        withContext(Dispatchers.IO) {
            itemDao.deleteAll()
        }
    }

    suspend fun createCustomItem(item: CustomItemEntity) {
        withContext(Dispatchers.IO) {
            customItemDao.insert(item)
        }
    }

    suspend fun updateCustomItem(item: CustomItemEntity) {
        withContext(Dispatchers.IO) {
            customItemDao.update(item)
        }
    }

    suspend fun deleteCustomItem(item: CustomItemEntity) {
        withContext(Dispatchers.IO) {
            customItemDao.delete(item)
        }
    }

    suspend fun addInventoryItem(
        characterId: Long,
        itemType: InventoryType,
        itemId: String,
        quantity: Int,
        equipped: Boolean,
        notes: String,
        containerName: String?
    ) {
        withContext(Dispatchers.IO) {
            inventoryDao.insert(
                InventoryItemEntity(
                    characterId = characterId,
                    itemType = itemType.name,
                    itemId = itemId,
                    quantity = quantity,
                    equipped = equipped,
                    notes = notes,
                    containerName = containerName
                )
            )
        }
    }

    suspend fun updateInventoryQuantity(id: Long, quantity: Int) {
        withContext(Dispatchers.IO) {
            if (quantity <= 0) {
                inventoryDao.deleteById(id)
            } else {
                inventoryDao.updateQuantity(id, quantity)
            }
        }
    }

    suspend fun updateInventoryEquipped(id: Long, equipped: Boolean) {
        withContext(Dispatchers.IO) {
            inventoryDao.updateEquipped(id, equipped)
        }
    }

    suspend fun updateInventoryNotes(id: Long, notes: String) {
        withContext(Dispatchers.IO) {
            inventoryDao.updateNotes(id, notes)
        }
    }

    suspend fun updateInventoryContainer(id: Long, containerName: String?) {
        withContext(Dispatchers.IO) {
            inventoryDao.updateContainer(id, containerName)
        }
    }

    suspend fun seedBaseItemsIfEmpty() {
        withContext(Dispatchers.IO) {
            if (itemDao.count() == 0) {
                val seedItems = listOf(
                    ItemEntity(
                        id = "longsword",
                        name = "Longsword",
                        rarity = "Common",
                        sourcebook = "PHB",
                        description = "Versatile martial weapon.",
                        weight = "3 lb.",
                        value = "15 gp",
                        category = "Weapon",
                        type = "Martial Melee",
                        damage = "1d8 slashing",
                        range = "Melee",
                        properties = "Versatile (1d10)"
                    ),
                    ItemEntity(
                        id = "healing_potion",
                        name = "Potion of Healing",
                        rarity = "Common",
                        sourcebook = "DMG",
                        description = "Heals 2d4 + 2 HP when consumed.",
                        weight = "0.5 lb.",
                        value = "50 gp",
                        category = "Potion",
                        type = "Consumable"
                    )
                )
                itemDao.insertAll(seedItems)
            }
        }
    }

    suspend fun syncSrdItemsFromApi() {
        withContext(Dispatchers.IO) {
            // Fetch both regular equipment and magic items indices
            val equipmentDef = async { api.getEquipmentIndex() }
            val magicItemsDef = async { api.getMagicItemsIndex() }
            
            val equipmentList = try { equipmentDef.await().results } catch(e: Exception) { emptyList() }
            val magicItemsList = try { magicItemsDef.await().results } catch(e: Exception) { emptyList() }
            
            // Sync regular equipment in larger chunks
            equipmentList.chunked(100).forEach { chunk ->
                val detailedItems = chunk.map { item ->
                    async {
                        try {
                            val detail = api.getEquipmentDetail(item.index)
                            val damageStr = detail.damage?.let { 
                                "${it.damage_dice} ${it.damage_type?.name ?: ""}".trim() 
                            }
                            val rangeStr = detail.range?.let {
                                if (it.long != null) "${it.normal}/${it.long} ft." else "${it.normal} ft."
                            } ?: detail.category_range
                            
                            ItemEntity(
                                id = detail.index,
                                name = detail.name,
                                rarity = detail.rarity?.name ?: "Common",
                                sourcebook = "SRD",
                                description = detail.desc?.joinToString("\n") ?: "No description.",
                                weight = detail.weight?.let { "$it lb." } ?: "",
                                value = detail.cost?.let { "${it.quantity} ${it.unit}" } ?: "",
                                category = detail.equipment_category?.name ?: "",
                                type = detail.weapon_category ?: "",
                                damage = damageStr,
                                range = rangeStr,
                                properties = detail.properties?.joinToString(", ") { it.name }
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                itemDao.insertAll(detailedItems)
            }

            // Sync magic items in larger chunks
            magicItemsList.chunked(100).forEach { chunk ->
                val detailedItems = chunk.map { item ->
                    async {
                        try {
                            val detail = api.getMagicItemDetail(item.index)
                            ItemEntity(
                                id = detail.index,
                                name = detail.name,
                                rarity = detail.rarity?.name ?: "Varies",
                                sourcebook = "SRD Magic",
                                description = detail.desc?.joinToString("\n") ?: "Magic item.",
                                weight = detail.weight?.let { "$it lb." } ?: "",
                                value = "", // Magic items often don't have standard costs in the API
                                category = detail.equipment_category?.name ?: "Magic Item",
                                type = "",
                                damage = null,
                                range = null,
                                properties = null
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                itemDao.insertAll(detailedItems)
            }
        }
    }
}

enum class InventoryType {
    BASE,
    CUSTOM
}

data class InventoryDisplayItem(
    val id: Long,
    val itemType: String,
    val itemId: String,
    val name: String,
    val rarity: String,
    val sourcebook: String,
    val description: String,
    val quantity: Int,
    val equipped: Boolean,
    val notes: String,
    val containerName: String?,
    val weight: String = "",
    val value: String = "",
    val category: String = "",
    val type: String = "",
    val damage: String? = null,
    val range: String? = null,
    val properties: String? = null
)
