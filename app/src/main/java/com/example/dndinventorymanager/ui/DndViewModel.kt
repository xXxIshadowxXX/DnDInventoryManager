package com.example.dndinventorymanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dndinventorymanager.data.DndRepository
import com.example.dndinventorymanager.data.InventoryDisplayItem
import com.example.dndinventorymanager.data.InventoryType
import com.example.dndinventorymanager.data.SyncStatus
import com.example.dndinventorymanager.data.UserSpellDisplayItem
import com.example.dndinventorymanager.data.entities.CharacterEntity
import com.example.dndinventorymanager.data.entities.CustomItemEntity
import com.example.dndinventorymanager.data.entities.ItemEntity
import com.example.dndinventorymanager.data.entities.SpellEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DndViewModel(private val repository: DndRepository) : ViewModel() {
    val characters: StateFlow<List<CharacterEntity>> = repository.characters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val baseItems: StateFlow<List<ItemEntity>> = repository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customItems: StateFlow<List<CustomItemEntity>> = repository.customItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val spells: StateFlow<List<SpellEntity>> = repository.spells
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeCharacterId: StateFlow<Long?> = repository.activeCharacterId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val activeCharacter = repository.activeCharacter()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncStatus())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeInventory: StateFlow<List<InventoryDisplayItem>> = activeCharacterId
        .flatMapLatest { id ->
            if (id == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.inventoryForCharacter(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSpells: StateFlow<List<UserSpellDisplayItem>> = activeCharacterId
        .flatMapLatest { id ->
            if (id == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.userSpellsForCharacter(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedBaseItemsIfEmpty()
        }
    }

    fun performInitialSync(force: Boolean = false) {
        viewModelScope.launch {
            repository.performInitialSync(force)
        }
    }

    fun setActiveCharacter(id: Long?) {
        viewModelScope.launch {
            repository.setActiveCharacter(id)
        }
    }

    fun createCharacter(name: String, clazz: String, level: Int, gold: Int, spellSlots: String = "0,0,0,0,0,0,0,0,0", maxPrepared: Int = 0) {
        viewModelScope.launch {
            repository.createCharacter(name, clazz, level, gold, spellSlots, maxPrepared)
        }
    }

    fun updateCharacterSpellcasting(id: Long, slots: String, maxPrepared: Int) {
        viewModelScope.launch {
            repository.updateCharacterSpellcasting(id, slots, maxPrepared)
        }
    }

    fun updateCharacterGold(id: Long, gold: Int) {
        viewModelScope.launch {
            repository.updateCharacterGold(id, gold)
        }
    }

    fun deleteCharacter(id: Long) {
        viewModelScope.launch {
            repository.deleteCharacter(id)
        }
    }

    fun addInventoryItem(
        characterId: Long,
        itemType: InventoryType,
        itemId: String,
        quantity: Int,
        equipped: Boolean,
        notes: String,
        containerName: String?
    ) {
        viewModelScope.launch {
            repository.addInventoryItem(characterId, itemType, itemId, quantity, equipped, notes, containerName)
        }
    }

    fun updateInventoryQuantity(id: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateInventoryQuantity(id, quantity)
        }
    }

    fun updateInventoryEquipped(id: Long, equipped: Boolean) {
        viewModelScope.launch {
            repository.updateInventoryEquipped(id, equipped)
        }
    }

    fun updateInventoryNotes(id: Long, notes: String) {
        viewModelScope.launch {
            repository.updateInventoryNotes(id, notes)
        }
    }

    fun updateInventoryContainer(id: Long, containerName: String?) {
        viewModelScope.launch {
            repository.updateInventoryContainer(id, containerName)
        }
    }

    fun createCustomItem(
        name: String,
        rarity: String,
        description: String,
        weight: String = "",
        value: String = "",
        category: String = "",
        damage: String? = null,
        range: String? = null
    ) {
        viewModelScope.launch {
            repository.createCustomItem(
                CustomItemEntity(
                    name = name,
                    rarity = rarity,
                    sourcebook = "Homebrew",
                    description = description,
                    weight = weight,
                    value = value,
                    category = category,
                    damage = damage,
                    range = range
                )
            )
        }
    }

    fun createBaseItem(
        id: String,
        name: String,
        rarity: String,
        sourcebook: String,
        description: String,
        weight: String = "",
        value: String = "",
        category: String = "",
        type: String = "",
        damage: String? = null,
        range: String? = null,
        properties: String? = null
    ) {
        viewModelScope.launch {
            repository.createBaseItem(
                ItemEntity(
                    id = id,
                    name = name,
                    rarity = rarity,
                    sourcebook = sourcebook,
                    description = description,
                    weight = weight,
                    value = value,
                    category = category,
                    type = type,
                    damage = damage,
                    range = range,
                    properties = properties
                )
            )
        }
    }

    fun updateBaseItem(
        id: String,
        name: String,
        rarity: String,
        sourcebook: String,
        description: String,
        weight: String = "",
        value: String = "",
        category: String = "",
        type: String = "",
        damage: String? = null,
        range: String? = null,
        properties: String? = null
    ) {
        viewModelScope.launch {
            repository.updateBaseItem(
                ItemEntity(
                    id = id,
                    name = name,
                    rarity = rarity,
                    sourcebook = sourcebook,
                    description = description,
                    weight = weight,
                    value = value,
                    category = category,
                    type = type,
                    damage = damage,
                    range = range,
                    properties = properties
                )
            )
        }
    }

    fun deleteBaseItem(id: String) {
        viewModelScope.launch {
            repository.deleteBaseItem(id)
        }
    }

    fun deleteAllBaseItems() {
        viewModelScope.launch {
            repository.deleteAllBaseItems()
        }
    }

    fun syncExtraItems() {
        viewModelScope.launch {
            repository.syncExtraItemsFromOpen5e()
        }
    }

    fun syncExtraSpells() {
        viewModelScope.launch {
            repository.syncExtraSpellsFromOpen5e()
        }
    }

    fun syncAdvancedSpells() {
        viewModelScope.launch {
            repository.syncAdvancedSpellsFromOpen5e()
        }
    }

    // Spells
    fun syncSpells() {
        viewModelScope.launch {
            repository.syncSpellsFromApi()
        }
    }

    fun createSpell(
        id: String,
        name: String,
        level: Int,
        school: String,
        castingTime: String,
        range: String,
        components: String,
        duration: String,
        description: String,
        sourcebook: String,
        classes: String
    ) {
        viewModelScope.launch {
            repository.createSpell(
                SpellEntity(
                    id = id,
                    name = name,
                    level = level,
                    school = school,
                    castingTime = castingTime,
                    range = range,
                    components = components,
                    duration = duration,
                    description = description,
                    sourcebook = sourcebook,
                    classes = classes
                )
            )
        }
    }

    fun deleteSpell(id: String) {
        viewModelScope.launch {
            repository.deleteSpell(id)
        }
    }

    fun deleteAllSpells() {
        viewModelScope.launch {
            repository.deleteAllSpells()
        }
    }

    fun learnSpell(characterId: Long, spellId: String) {
        viewModelScope.launch {
            repository.learnSpell(characterId, spellId)
        }
    }

    fun updateSpellPrepared(userSpellId: Long, isPrepared: Boolean, onPreparedLimitReached: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateSpellPrepared(userSpellId, isPrepared)
            if (!success && isPrepared) {
                onPreparedLimitReached()
            }
        }
    }

    fun updateSpellLoadouts(userSpellId: Long, loadouts: String) {
        viewModelScope.launch {
            repository.updateSpellLoadouts(userSpellId, loadouts)
        }
    }

    fun applyLoadout(characterId: Long, loadoutName: String) {
        viewModelScope.launch {
            repository.applyLoadout(characterId, loadoutName)
        }
    }

    fun forgetSpell(userSpellId: Long) {
        viewModelScope.launch {
            repository.forgetSpell(userSpellId)
        }
    }

    fun castSpell(characterId: Long, level: Int) {
        viewModelScope.launch {
            repository.castSpell(characterId, level)
        }
    }

    fun recoverSpellSlot(characterId: Long, level: Int) {
        viewModelScope.launch {
            repository.recoverSpellSlot(characterId, level)
        }
    }

    fun restCharacter(characterId: Long) {
        viewModelScope.launch {
            repository.restCharacter(characterId)
        }
    }
}

class DndViewModelFactory(private val repository: DndRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DndViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DndViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
