package com.example.dndinventorymanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dndinventorymanager.data.DndRepository
import com.example.dndinventorymanager.data.InventoryDisplayItem
import com.example.dndinventorymanager.data.InventoryType
import com.example.dndinventorymanager.data.entities.CharacterEntity
import com.example.dndinventorymanager.data.entities.CustomItemEntity
import com.example.dndinventorymanager.data.entities.ItemEntity
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

    val activeCharacterId: StateFlow<Long?> = repository.activeCharacterId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val activeCharacter = repository.activeCharacter()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val activeInventory: StateFlow<List<InventoryDisplayItem>> = activeCharacterId
        .flatMapLatest { id ->
            if (id == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                repository.inventoryForCharacter(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedBaseItemsIfEmpty()
        }
    }

    fun setActiveCharacter(id: Long?) {
        viewModelScope.launch {
            repository.setActiveCharacter(id)
        }
    }

    fun createCharacter(name: String, clazz: String, level: Int, gold: Int) {
        viewModelScope.launch {
            repository.createCharacter(name, clazz, level, gold)
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

    fun syncSrdItems() {
        viewModelScope.launch {
            repository.syncSrdItemsFromApi()
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
