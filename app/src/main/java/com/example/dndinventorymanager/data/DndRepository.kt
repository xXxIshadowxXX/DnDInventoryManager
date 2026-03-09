package com.example.dndinventorymanager.data

import android.content.Context
import android.util.Log
import com.example.dndinventorymanager.data.dao.CharacterDao
import com.example.dndinventorymanager.data.dao.CustomItemDao
import com.example.dndinventorymanager.data.dao.InventoryDao
import com.example.dndinventorymanager.data.dao.ItemDao
import com.example.dndinventorymanager.data.dao.SpellDao
import com.example.dndinventorymanager.data.dao.UserSpellDao
import com.example.dndinventorymanager.data.entities.CharacterEntity
import com.example.dndinventorymanager.data.entities.CustomItemEntity
import com.example.dndinventorymanager.data.entities.InventoryItemEntity
import com.example.dndinventorymanager.data.entities.ItemEntity
import com.example.dndinventorymanager.data.entities.SpellEntity
import com.example.dndinventorymanager.data.entities.UserSpellEntity
import com.example.dndinventorymanager.data.network.DndApi
import com.example.dndinventorymanager.data.network.DndApiClient
import com.example.dndinventorymanager.data.network.EquipmentDetailResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DndRepository(
    private val characterDao: CharacterDao,
    private val itemDao: ItemDao,
    private val customItemDao: CustomItemDao,
    private val inventoryDao: InventoryDao,
    private val spellDao: SpellDao,
    private val userSpellDao: UserSpellDao,
    private val settingsDataStore: SettingsDataStore,
    private val api: DndApi,
    private val context: Context
) {
    val characters: Flow<List<CharacterEntity>> = characterDao.getCharacters()
    val items: Flow<List<ItemEntity>> = itemDao.getItems()
    val customItems: Flow<List<CustomItemEntity>> = customItemDao.getCustomItems()
    val spells: Flow<List<SpellEntity>> = spellDao.getSpells()
    val activeCharacterId: Flow<Long?> = settingsDataStore.activeCharacterIdFlow
    val initialSyncDone: Flow<Boolean> = settingsDataStore.initialSyncDoneFlow

    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    fun activeCharacter(): Flow<CharacterEntity?> {
        return activeCharacterId.combine(characters) { id, list ->
            list.firstOrNull { it.id == id }
        }
    }

    fun userSpellsForCharacter(characterId: Long): Flow<List<UserSpellDisplayItem>> {
        return combine(
            userSpellDao.getUserSpells(characterId),
            spells
        ) { userSpells, allSpells ->
            val spellMap = allSpells.associateBy { it.id }
            userSpells.mapNotNull { userSpell ->
                val spell = spellMap[userSpell.spellId] ?: return@mapNotNull null
                UserSpellDisplayItem(
                    id = userSpell.id,
                    spellId = spell.id,
                    name = spell.name,
                    level = spell.level,
                    school = spell.school,
                    castingTime = spell.castingTime,
                    range = spell.range,
                    components = spell.components,
                    duration = spell.duration,
                    description = spell.description,
                    isPrepared = userSpell.isPrepared,
                    loadouts = userSpell.loadouts,
                    target = spell.target,
                    damageRoll = spell.damageRoll,
                    higherLevel = spell.higherLevel,
                    verbal = spell.verbal,
                    somatic = spell.somatic,
                    material = spell.material
                )
            }.sortedWith(compareBy({ it.level }, { it.name }))
        }.flowOn(Dispatchers.Default)
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
        }.flowOn(Dispatchers.Default)
    }

    suspend fun setActiveCharacter(id: Long?) {
        withContext(Dispatchers.IO) {
            settingsDataStore.setActiveCharacterId(id)
        }
    }

    suspend fun createCharacter(name: String, clazz: String, level: Int, gold: Int, spellSlots: String, maxPrepared: Int) {
        withContext(Dispatchers.IO) {
            characterDao.insert(CharacterEntity(
                name = name, 
                clazz = clazz, 
                level = level, 
                gold = gold, 
                spellSlots = spellSlots, 
                currentSlots = spellSlots, // Start with full slots
                maxPrepared = maxPrepared
            ))
        }
    }

    suspend fun updateCharacterSpellcasting(id: Long, slots: String, maxPrepared: Int) {
        withContext(Dispatchers.IO) {
            characterDao.updateSpellcasting(id, slots, maxPrepared)
        }
    }

    suspend fun updateCharacterGold(id: Long, gold: Int) {
        withContext(Dispatchers.IO) {
            characterDao.updateGold(id, gold)
        }
    }

    suspend fun deleteCharacter(characterId: Long) {
        withContext(Dispatchers.IO) {
            val char = characterDao.getCharacters().first().firstOrNull { it.id == characterId }
            if (char != null) {
                characterDao.delete(char)
                if (settingsDataStore.activeCharacterIdFlow.first() == characterId) {
                    settingsDataStore.setActiveCharacterId(null)
                }
            }
        }
    }

    suspend fun castSpell(characterId: Long, level: Int): Boolean {
        if (level == 0) return true // Cantrips are free
        return withContext(Dispatchers.IO) {
            val char = characterDao.getCharacters().first().firstOrNull { it.id == characterId } ?: return@withContext false
            val slots = char.currentSlots.split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()
            val index = level - 1
            if (index in slots.indices && slots[index] > 0) {
                slots[index] -= 1
                characterDao.updateCurrentSlots(characterId, slots.joinToString(","))
                true
            } else {
                false
            }
        }
    }

    suspend fun recoverSpellSlot(characterId: Long, level: Int) {
        withContext(Dispatchers.IO) {
            val char = characterDao.getCharacters().first().firstOrNull { it.id == characterId } ?: return@withContext
            val current = char.currentSlots.split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()
            val max = char.spellSlots.split(",").map { it.toIntOrNull() ?: 0 }
            val index = level - 1
            if (index in current.indices && index in max.indices) {
                if (current[index] < max[index]) {
                    current[index] += 1
                    characterDao.updateCurrentSlots(characterId, current.joinToString(","))
                }
            }
        }
    }

    suspend fun restCharacter(characterId: Long) {
        withContext(Dispatchers.IO) {
            val char = characterDao.getCharacters().first().firstOrNull { it.id == characterId } ?: return@withContext
            characterDao.updateCurrentSlots(characterId, char.spellSlots)
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

    suspend fun createSpell(spell: SpellEntity) {
        withContext(Dispatchers.IO) {
            spellDao.insert(spell)
        }
    }

    suspend fun deleteSpell(id: String) {
        withContext(Dispatchers.IO) {
            spellDao.deleteById(id)
        }
    }

    suspend fun deleteAllSpells() {
        withContext(Dispatchers.IO) {
            spellDao.deleteAll()
        }
    }

    suspend fun learnSpell(characterId: Long, spellId: String) {
        withContext(Dispatchers.IO) {
            userSpellDao.insert(UserSpellEntity(characterId = characterId, spellId = spellId))
        }
    }

    suspend fun updateSpellPrepared(userSpellId: Long, isPrepared: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            val userSpell = userSpellDao.getUserSpellById(userSpellId) ?: return@withContext false
            val spell = spellDao.getSpellById(userSpell.spellId) ?: return@withContext false
            val charId = userSpell.characterId
            val char = characterDao.getCharacters().first().firstOrNull { it.id == charId } ?: return@withContext false
            
            // Only count non-cantrips for prepared limit
            if (isPrepared && spell.level > 0) {
                val preparedCount = userSpellDao.getPreparedCount(charId)
                if (preparedCount >= char.maxPrepared) {
                    return@withContext false
                }
            }
            
            userSpellDao.updatePrepared(userSpellId, isPrepared)
            true
        }
    }

    suspend fun applyLoadout(characterId: Long, loadoutName: String) {
        withContext(Dispatchers.IO) {
            userSpellDao.applyLoadout(characterId, loadoutName)
        }
    }

    suspend fun updateSpellLoadouts(userSpellId: Long, loadouts: String) {
        withContext(Dispatchers.IO) {
            userSpellDao.updateLoadouts(userSpellId, loadouts)
        }
    }

    suspend fun forgetSpell(userSpellId: Long) {
        withContext(Dispatchers.IO) {
            userSpellDao.deleteById(userSpellId)
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

    private fun parseRange(detail: EquipmentDetailResponse): String? {
        return if (detail.throw_range != null) {
            if (detail.throw_range.long != null) "${detail.throw_range.normal}/${detail.throw_range.long} ft. (Thrown)"
            else "${detail.throw_range.normal} ft. (Thrown)"
        } else if (detail.range != null) {
            if (detail.range.long != null) "${detail.range.normal}/${detail.range.long} ft." 
            else "${detail.range.normal} ft."
        } else {
            detail.category_range
        }
    }

    suspend fun performInitialSync(force: Boolean = false) {
        withContext(Dispatchers.IO) {
            val dbCount = itemDao.count()
            // If DB is mostly empty, we should sync even if the flag is set (e.g. after a version update)
            val shouldSync = force || !initialSyncDone.first() || dbCount < 10
            
            if (shouldSync) {
                _syncStatus.value = SyncStatus("Starting initial sync...", 0, 0, true)
                try {
                    syncSrdItemsFromApi()
                    syncSpellsFromApi()
                    settingsDataStore.setInitialSyncDone(true)
                    _syncStatus.value = SyncStatus("Initial sync complete!", 0, 0, false)
                } catch (e: Exception) {
                    _syncStatus.value = SyncStatus("Sync failed: ${e.localizedMessage}", 0, 0, false)
                    Log.e("DndRepository", "Sync failed", e)
                }
            }
        }
    }

    suspend fun syncSrdItemsFromApi() {
        withContext(Dispatchers.IO) {
            _syncStatus.value = SyncStatus("Fetching SRD index...", 0, 0, true)
            val equipmentDef = async { try { api.getEquipmentIndex() } catch(e: Exception) { null } }
            val magicItemsDef = async { try { api.getMagicItemsIndex() } catch(e: Exception) { null } }
            
            val equipmentList = equipmentDef.await()?.results ?: emptyList()
            val magicItemsList = magicItemsDef.await()?.results ?: emptyList()
            
            val equipmentMap = mutableMapOf<String, ItemEntity>()
            var loadedCount = 0
            val totalCount = equipmentList.size + magicItemsList.size

            // Fetch details for regular equipment
            equipmentList.chunked(40).forEach { chunk ->
                val detailedItems = chunk.map { item ->
                    async {
                        try {
                            val detail = api.getEquipmentDetail(item.index)
                            val damageStr = detail.damage?.let { 
                                "${it.damage_dice} ${it.damage_type?.name ?: ""}".trim() 
                            }
                            val rangeStr = parseRange(detail)
                            
                            ItemEntity(
                                id = detail.index,
                                name = detail.name,
                                rarity = detail.rarity?.name ?: "Common",
                                sourcebook = "SRD",
                                description = detail.desc?.joinToString("\n") ?: "No description.",
                                weight = detail.weight?.let { "$it lb." } ?: "",
                                value = detail.cost?.let { "${it.quantity} ${it.unit}" } ?: "",
                                category = detail.equipment_category?.name ?: "",
                                type = detail.category_range ?: detail.weapon_category ?: "",
                                damage = damageStr,
                                range = rangeStr,
                                properties = detail.properties?.joinToString(", ") { it.name }
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                
                detailedItems.forEach { equipmentMap[it.id] = it }
                loadedCount += detailedItems.size
                _syncStatus.value = SyncStatus("Fetching equipment details...", loadedCount, totalCount, true)
            }
            
            // Batch insert regular items to reduce Flow emissions and DB overhead
            itemDao.insertAll(equipmentMap.values.toList())

            // Fetch details for magic items
            val magicItemEntities = mutableListOf<ItemEntity>()
            magicItemsList.chunked(40).forEach { chunk ->
                val detailedItems = chunk.map { item ->
                    async {
                        try {
                            val detail = api.getMagicItemDetail(item.index)
                            
                            var finalDamage = ""
                            var finalRange = ""
                            var finalType = ""
                            var finalProperties = ""

                            val firstDesc = detail.desc?.firstOrNull() ?: ""
                            if (detail.equipment_category?.name?.contains("weapon", ignoreCase = true) == true) {
                                val weaponRegex = Regex("""Weapon\s*\(([^)]+)\)""", RegexOption.IGNORE_CASE)
                                val match = weaponRegex.find(firstDesc)
                                val baseWeaponName = match?.groupValues?.get(1)?.lowercase()?.replace(" ", "-")
                                
                                if (baseWeaponName != null) {
                                    val baseWeapon = equipmentMap[baseWeaponName]
                                    if (baseWeapon != null) {
                                        finalDamage = baseWeapon.damage ?: ""
                                        finalRange = baseWeapon.range ?: ""
                                        finalType = baseWeapon.type
                                        finalProperties = baseWeapon.properties ?: ""
                                    }
                                }
                            }

                            ItemEntity(
                                id = detail.index,
                                name = detail.name,
                                rarity = detail.rarity?.name ?: "Varies",
                                sourcebook = "SRD Magic",
                                description = detail.desc?.joinToString("\n") ?: "Magic item.",
                                weight = detail.weight?.let { "$it lb." } ?: "",
                                value = "",
                                category = detail.equipment_category?.name ?: "Magic Item",
                                type = finalType,
                                damage = if (finalDamage.isNotEmpty()) finalDamage else null,
                                range = if (finalRange.isNotEmpty()) finalRange else null,
                                properties = if (finalProperties.isNotEmpty()) finalProperties else null
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                
                magicItemEntities.addAll(detailedItems)
                loadedCount += detailedItems.size
                _syncStatus.value = SyncStatus("Fetching magic items...", loadedCount, totalCount, true)
            }
            
            // Batch insert magic items
            itemDao.insertAll(magicItemEntities)
        }
    }

    suspend fun syncExtraItemsFromOpen5e() {
        withContext(Dispatchers.IO) {
            _syncStatus.value = SyncStatus("Fetching extra items from Open5e...", 0, 0, true)
            val processedNames = itemDao.getAllNames().map { it.lowercase() }.toMutableSet()
            val processedIds = itemDao.getAllIds().toMutableSet()

            var nextUrl: String? = "https://api.open5e.com/v1/magicitems/"
            var totalLoaded = 0
            while (nextUrl != null) {
                try {
                    val response = DndApiClient.open5eApi.getMagicItems(nextUrl)
                    val entities = response.results.mapNotNull { item ->
                        val isA5e = item.slug.contains("A5e", ignoreCase = true)
                        val cleanId = if (isA5e) item.slug.replace("a5e", "", ignoreCase = true).trim('-') else item.slug
                        val lowerName = item.name.lowercase()

                        if (processedNames.contains(lowerName) || processedIds.contains(cleanId)) {
                            return@mapNotNull null
                        }

                        processedNames.add(lowerName)
                        processedIds.add(cleanId)

                        val sourcebook = if (isA5e) "Advanced 5e" else (item.document__title ?: "Open5e")
                        
                        ItemEntity(
                            id = cleanId,
                            name = item.name,
                            rarity = item.rarity,
                            sourcebook = sourcebook,
                            description = item.desc,
                            weight = "",
                            value = "",
                            category = item.type,
                            type = item.type,
                            damage = null,
                            range = null,
                            properties = if (item.requires_attunement == "requires_attunement") "Requires Attunement" else null
                        )
                    }
                    itemDao.insertAllIgnore(entities)
                    totalLoaded += entities.size
                    _syncStatus.value = SyncStatus("Loading extra items...", totalLoaded, response.count, true)
                    nextUrl = response.next
                } catch (e: Exception) {
                    e.printStackTrace()
                    nextUrl = null
                }
            }
            _syncStatus.value = SyncStatus("Extra items loaded!", totalLoaded, totalLoaded, false)
        }
    }

    suspend fun syncAdvancedSpellsFromOpen5e() {
        withContext(Dispatchers.IO) {
            _syncStatus.value = SyncStatus("Starting Advanced Spell Sync...", 0, 0, true)
            
            spellDao.deleteAll()
            
            var nextUrl: String? = "https://api.open5e.com/v1/spells/"
            var totalLoaded = 0
            val processedIds = mutableSetOf<String>()

            while (nextUrl != null) {
                try {
                    val response = DndApiClient.open5eApi.getSpells(nextUrl)
                    val entities = response.results.mapNotNull { spell ->
                        if (processedIds.contains(spell.slug)) return@mapNotNull null
                        processedIds.add(spell.slug)

                        val components = spell.components ?: ""
                        
                        SpellEntity(
                            id = spell.slug,
                            name = spell.name,
                            level = spell.level_int ?: 0,
                            school = spell.school ?: "Evocation",
                            castingTime = spell.casting_time ?: "Action",
                            range = spell.range ?: "",
                            components = components,
                            duration = (spell.duration ?: "") + (if (spell.concentration == "yes") " (Concentration)" else ""),
                            description = spell.desc,
                            sourcebook = spell.document__title ?: "Open5e",
                            classes = listOfNotNull(spell.dnd_class, spell.archetype, spell.circles).joinToString(", "),
                            target = spell.target ?: "",
                            damageRoll = if (spell.damage_dice != null) "${spell.damage_dice} ${spell.damage_type ?: ""}" else "",
                            higherLevel = spell.higher_level ?: "",
                            verbal = components.contains("V", ignoreCase = true),
                            somatic = components.contains("S", ignoreCase = true),
                            material = spell.material ?: ""
                        )
                    }
                    spellDao.insertAll(entities)
                    totalLoaded += entities.size
                    _syncStatus.value = SyncStatus("Loading advanced spells...", totalLoaded, response.count, true)
                    nextUrl = response.next
                } catch (e: Exception) {
                    e.printStackTrace()
                    nextUrl = null
                }
            }
            _syncStatus.value = SyncStatus("Advanced spells loaded!", totalLoaded, totalLoaded, false)
        }
    }

    suspend fun syncExtraSpellsFromOpen5e() {
        withContext(Dispatchers.IO) {
            _syncStatus.value = SyncStatus("Fetching extra spells from Open5e...", 0, 0, true)
            val processedNames = spellDao.getAllNames().map { it.lowercase() }.toMutableSet()
            val processedIds = spellDao.getAllIds().toMutableSet()
            
            var nextUrl: String? = "https://api.open5e.com/v1/spells/"
            var totalLoaded = 0
            while (nextUrl != null) {
                try {
                    val response = DndApiClient.open5eApi.getSpells(nextUrl)
                    val entities = response.results.mapNotNull { spell ->
                        val lowerName = spell.name.lowercase()
                        if (processedNames.contains(lowerName) || processedIds.contains(spell.slug)) {
                            return@mapNotNull null
                        }

                        processedNames.add(lowerName)
                        processedIds.add(spell.slug)
                        
                        val componentsStr = spell.components ?: ""

                        SpellEntity(
                            id = spell.slug,
                            name = spell.name,
                            level = spell.level_int ?: 0,
                            school = spell.school ?: "Evocation",
                            castingTime = spell.casting_time ?: "Action",
                            range = spell.range ?: "",
                            components = componentsStr,
                            duration = (spell.duration ?: "") + (if (spell.concentration == "yes") " (Concentration)" else ""),
                            description = spell.desc,
                            sourcebook = spell.document__title ?: "Open5e",
                            classes = listOfNotNull(spell.dnd_class, spell.archetype, spell.circles).joinToString(", "),
                            target = spell.target ?: "",
                            damageRoll = if (spell.damage_dice != null) "${spell.damage_dice} ${spell.damage_type ?: ""}" else "",
                            higherLevel = spell.higher_level ?: "",
                            verbal = componentsStr.contains("V", ignoreCase = true),
                            somatic = componentsStr.contains("S", ignoreCase = true),
                            material = spell.material ?: ""
                        )
                    }
                    spellDao.insertAllIgnore(entities)
                    totalLoaded += entities.size
                    _syncStatus.value = SyncStatus("Loading extra spells...", totalLoaded, response.count, true)
                    nextUrl = response.next
                } catch (e: Exception) {
                    e.printStackTrace()
                    nextUrl = null
                }
            }
            _syncStatus.value = SyncStatus("Extra spells loaded!", totalLoaded, totalLoaded, false)
        }
    }

    suspend fun syncSpellsFromApi() {
        withContext(Dispatchers.IO) {
            _syncStatus.value = SyncStatus("Syncing spells...", 0, 0, true)
            try {
                val jsonString = context.assets.open("spells.json").bufferedReader().use { it.readText() }
                
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val spellListType = Types.newParameterizedType(List::class.java, GistSpell::class.java)
                val adapter = moshi.adapter<List<GistSpell>>(spellListType)
                val gistSpells = adapter.fromJson(jsonString) ?: emptyList()
                
                spellDao.deleteAll()
                
                val entities = gistSpells.map { gistSpell ->
                    val componentsStr = gistSpell.components?.joinToString(", ") ?: ""
                    
                    SpellEntity(
                        id = gistSpell.name.lowercase().replace(" ", "-").replace("'", ""),
                        name = gistSpell.name,
                        level = gistSpell.level,
                        school = gistSpell.school,
                        castingTime = gistSpell.castingTime ?: gistSpell.actionType ?: "Action",
                        range = gistSpell.range ?: "",
                        components = componentsStr,
                        duration = (gistSpell.duration ?: "") + (if (gistSpell.concentration == true) " (Concentration)" else ""),
                        description = gistSpell.description ?: "",
                        sourcebook = "SRD 5.2",
                        classes = gistSpell.classes.joinToString(", "),
                        higherLevel = gistSpell.higherLevelSlot ?: "",
                        verbal = componentsStr.contains("V", ignoreCase = true),
                        somatic = componentsStr.contains("S", ignoreCase = true),
                        material = gistSpell.material ?: ""
                    )
                }
                spellDao.insertAll(entities)
                _syncStatus.value = SyncStatus("Spells loaded!", entities.size, entities.size, false)
            } catch (e: Exception) {
                e.printStackTrace()
                val spellIndex = try { api.getSpellsIndex().results } catch(e: Exception) { emptyList() }
                var loadedCount = 0
                val allDetailedSpells = mutableListOf<SpellEntity>()
                
                spellIndex.chunked(40).forEach { chunk ->
                    val detailedSpells = chunk.map { item ->
                        async {
                            try {
                                val detail = api.getSpellDetail(item.index)
                                val componentsStr = detail.components.joinToString(", ")
                                SpellEntity(
                                    id = detail.index,
                                    name = detail.name,
                                    level = detail.level,
                                    school = detail.school.name,
                                    castingTime = detail.casting_time,
                                    range = detail.range,
                                    components = componentsStr,
                                    duration = detail.duration + (if (detail.concentration) " (Concentration)" else ""),
                                    description = detail.desc.joinToString("\n"),
                                    sourcebook = "SRD",
                                    classes = detail.classes.joinToString(", ") { it.name },
                                    higherLevel = detail.higher_level?.joinToString("\n") ?: "",
                                    verbal = componentsStr.contains("V", ignoreCase = true),
                                    somatic = componentsStr.contains("S", ignoreCase = true),
                                    material = detail.material ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                    
                    allDetailedSpells.addAll(detailedSpells)
                    loadedCount += detailedSpells.size
                    _syncStatus.value = SyncStatus("Fetching spell details...", loadedCount, spellIndex.size, true)
                }
                spellDao.insertAll(allDetailedSpells)
                _syncStatus.value = SyncStatus("Spells loaded!", allDetailedSpells.size, allDetailedSpells.size, false)
            }
        }
    }
}

data class SyncStatus(
    val message: String = "",
    val progress: Int = 0,
    val total: Int = 0,
    val isSyncing: Boolean = false
)

data class GistSpell(
    val name: String,
    val level: Int,
    val school: String,
    val classes: List<String> = emptyList(),
    val actionType: String? = null,
    val concentration: Boolean? = null,
    val ritual: Boolean? = null,
    val range: String? = null,
    val components: List<String>? = null,
    val material: String? = null,
    val duration: String? = null,
    val description: String? = null,
    val castingTime: String? = null,
    val higherLevelSlot: String? = null,
    val cantripUpgrade: String? = null
)

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

data class UserSpellDisplayItem(
    val id: Long,
    val spellId: String,
    val name: String,
    val level: Int,
    val school: String,
    val castingTime: String,
    val range: String,
    val components: String,
    val duration: String,
    val description: String,
    val isPrepared: Boolean,
    val loadouts: String,
    val target: String = "",
    val damageRoll: String = "",
    val higherLevel: String = "",
    val verbal: Boolean = false,
    val somatic: Boolean = false,
    val material: String = ""
)
