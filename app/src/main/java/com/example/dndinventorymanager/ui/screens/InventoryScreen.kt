package com.example.dndinventorymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.dndinventorymanager.data.InventoryDisplayItem
import com.example.dndinventorymanager.data.InventoryType
import com.example.dndinventorymanager.ui.DndViewModel
import com.example.dndinventorymanager.ui.components.DndButton
import com.example.dndinventorymanager.ui.components.DndCard
import com.example.dndinventorymanager.ui.components.DndCardHeader
import com.example.dndinventorymanager.ui.components.DndDivider
import com.example.dndinventorymanager.ui.components.DndTextInput
import com.example.dndinventorymanager.ui.theme.DnDCardBg
import com.example.dndinventorymanager.ui.theme.DnDDarkBg
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText
import com.example.dndinventorymanager.ui.theme.DnDMutedText
import java.util.Locale

@Composable
fun InventoryScreen(
    viewModel: DndViewModel,
    onNavigateCharacters: () -> Unit
) {
    val activeCharacter by viewModel.activeCharacter.collectAsState()
    val baseItems by viewModel.baseItems.collectAsState()
    val customItems by viewModel.customItems.collectAsState()
    val inventory by viewModel.activeInventory.collectAsState()

    var selectedType by remember { mutableStateOf(InventoryType.BASE) }
    var selectedItemId by remember { mutableStateOf("") }
    var itemSearchQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var selectedContainerId by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var searchMenuExpanded by remember { mutableStateOf(false) }
    var containerMenuExpanded by remember { mutableStateOf(false) }
    var isItemContainer by remember { mutableStateOf(false) }

    var customName by remember { mutableStateOf("") }
    var customRarity by remember { mutableStateOf("Homebrew") }
    var customDescription by remember { mutableStateOf("") }
    var customWeight by remember { mutableStateOf("") }
    var customValue by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    var customDamage by remember { mutableStateOf("") }
    var customRange by remember { mutableStateOf("") }
    var customIsContainer by remember { mutableStateOf(false) }

    // View mode: "items" or "inventory"
    var viewMode by remember { mutableStateOf("inventory") }

    // Track expanded containers in inventory view
    var expandedContainers by remember { mutableStateOf(setOf<String>()) }
    // Track expanded items
    var expandedItems by remember { mutableStateOf(setOf<String>()) }

    if (activeCharacter == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DndCard(modifier = Modifier.fillMaxWidth()) {
                DndCardHeader("No Character Selected")
                DndDivider()
                Text(
                    "Please select an active character to manage their inventory.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DnDLightText
                )
                DndButton(text = "⚔ Go to Characters", onClick = onNavigateCharacters)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with character name and top navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "📦 Inventory",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DnDGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${activeCharacter!!.name} • Level ${activeCharacter!!.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DnDMutedText
                )
            }
            // Top-right buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewMode = "items" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewMode == "items") DnDGold else DnDCardBg,
                        contentColor = if (viewMode == "items") DnDDarkBg else DnDGold
                    )
                ) {
                    Text("📝 Items", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewMode = "inventory" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewMode == "inventory") DnDGold else DnDCardBg,
                        contentColor = if (viewMode == "inventory") DnDDarkBg else DnDGold
                    )
                ) {
                    Text("👜 Inventory", fontWeight = FontWeight.Bold)
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (viewMode == "items") {
                // Items Management View - Add items
                item {
                    DndCard(modifier = Modifier.fillMaxWidth()) {
                        DndCardHeader("Add Item to Inventory")
                        DndDivider()

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Item Type Selector
                            Column {
                                Text(
                                    "Item Source",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DnDMutedText
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { typeMenuExpanded = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DnDGold,
                                            contentColor = DnDDarkBg
                                        )
                                    ) {
                                        Text(if (selectedType == InventoryType.BASE) "📗 Official" else "✍ Homebrew", fontWeight = FontWeight.Bold)
                                    }
                                    DropdownMenu(
                                        expanded = typeMenuExpanded,
                                        onDismissRequest = { typeMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("📗 Official Items") },
                                            onClick = {
                                                selectedType = InventoryType.BASE
                                                typeMenuExpanded = false
                                                selectedItemId = ""
                                                itemSearchQuery = ""
                                                isItemContainer = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("✍ Homebrew Items") },
                                            onClick = {
                                                selectedType = InventoryType.CUSTOM
                                                typeMenuExpanded = false
                                                selectedItemId = ""
                                                itemSearchQuery = ""
                                                isItemContainer = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Item Search/Selector
                            Column {
                                Text(
                                    "Search Item",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DnDMutedText
                                )
                                Box {
                                    DndTextInput(
                                        value = itemSearchQuery,
                                        onValueChange = {
                                            itemSearchQuery = it
                                            searchMenuExpanded = it.isNotBlank() && selectedItemId.isEmpty()
                                            if (it.isEmpty()) selectedItemId = ""
                                        },
                                        label = if (selectedItemId.isNotBlank()) "Item Selected" else "Type to search...",
                                        placeholder = "e.g., Longsword, Potion",
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (searchMenuExpanded) {
                                        val filteredItems = when (selectedType) {
                                            InventoryType.BASE -> baseItems.filter { it.name.contains(itemSearchQuery, ignoreCase = true) }
                                            InventoryType.CUSTOM -> customItems.filter { it.name.contains(itemSearchQuery, ignoreCase = true) }
                                        }.take(10)

                                        if (filteredItems.isNotEmpty()) {
                                            Popup(
                                                alignment = Alignment.TopStart,
                                                onDismissRequest = { searchMenuExpanded = false }
                                            ) {
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.9f)
                                                        .heightIn(max = 300.dp)
                                                        .padding(horizontal = 16.dp),
                                                    color = DnDCardBg,
                                                    tonalElevation = 8.dp,
                                                    shadowElevation = 8.dp,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, DnDGold)
                                                ) {
                                                    LazyColumn {
                                                        items(filteredItems) { item ->
                                                            val itemName = when (item) {
                                                                is com.example.dndinventorymanager.data.entities.ItemEntity -> item.name
                                                                is com.example.dndinventorymanager.data.entities.CustomItemEntity -> item.name
                                                                else -> ""
                                                            }
                                                            val itemId = when (item) {
                                                                is com.example.dndinventorymanager.data.entities.ItemEntity -> item.id
                                                                is com.example.dndinventorymanager.data.entities.CustomItemEntity -> item.id.toString()
                                                                else -> ""
                                                            }
                                                            val itemCategory = when (item) {
                                                                is com.example.dndinventorymanager.data.entities.ItemEntity -> item.category
                                                                is com.example.dndinventorymanager.data.entities.CustomItemEntity -> item.category
                                                                else -> ""
                                                            }
                                                            
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        itemSearchQuery = itemName
                                                                        selectedItemId = itemId
                                                                        searchMenuExpanded = false
                                                                    }
                                                                    .padding(12.dp)
                                                            ) {
                                                                Text(itemName, color = DnDLightText, fontWeight = FontWeight.Bold)
                                                                if (itemCategory.isNotBlank()) {
                                                                    Text(itemCategory, color = DnDMutedText, style = MaterialTheme.typography.labelSmall)
                                                                }
                                                            }
                                                            DndDivider()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Is Container Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = isItemContainer,
                                    onCheckedChange = { isItemContainer = it },
                                    colors = CheckboxDefaults.colors(checkedColor = DnDGold, uncheckedColor = DnDMutedText)
                                )
                                Text(
                                    "This item is a container",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DnDLightText
                                )
                            }

                            // Item Details
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DndTextInput(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    label = "Qty",
                                    placeholder = "1",
                                    modifier = Modifier.weight(0.4f)
                                )
                            }

                            // Container Selector (only show if not marking as container)
                            if (!isItemContainer && inventory.isNotEmpty()) {
                                val containers = inventory.filter { 
                                    it.notes.contains("🗂 CONTAINER", ignoreCase = true) ||
                                    it.containerName?.startsWith("🗂") == true
                                }
                                
                                if (containers.isNotEmpty()) {
                                    Column {
                                        Text(
                                            "Place in Container (Optional)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DnDMutedText
                                        )
                                        Button(
                                            onClick = { containerMenuExpanded = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DnDCardBg,
                                                contentColor = DnDGold
                                            )
                                        ) {
                                            val containerLabel = if (selectedContainerId.isNotEmpty()) {
                                                inventory.firstOrNull { it.id == selectedContainerId.toLongOrNull() }?.name ?: "Choose container..."
                                            } else "Choose container..."
                                            Text(containerLabel, fontWeight = FontWeight.Bold)
                                        }
                                        DropdownMenu(
                                            expanded = containerMenuExpanded,
                                            onDismissRequest = { containerMenuExpanded = false }
                                        ) {
                                            containers.forEach { container ->
                                                DropdownMenuItem(
                                                    text = { Text(container.name) },
                                                    onClick = {
                                                        selectedContainerId = container.id.toString()
                                                        containerMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            DndTextInput(
                                value = notes,
                                onValueChange = { notes = it },
                                label = "Notes",
                                placeholder = "Any special notes..."
                            )

                            DndButton(
                                text = "Add to Inventory",
                                onClick = {
                                    val qty = quantity.toIntOrNull() ?: 1
                                    val containerName = if (isItemContainer) "🗂 CONTAINER" else if (selectedContainerId.isNotEmpty()) {
                                        inventory.firstOrNull { it.id == selectedContainerId.toLongOrNull() }?.name
                                    } else null
                                    
                                    if (selectedItemId.isNotBlank()) {
                                        viewModel.addInventoryItem(
                                            characterId = activeCharacter!!.id,
                                            itemType = selectedType,
                                            itemId = selectedItemId,
                                            quantity = qty,
                                            equipped = false,
                                            notes = notes,
                                            containerName = containerName
                                        )
                                        quantity = "1"
                                        notes = ""
                                        selectedItemId = ""
                                        itemSearchQuery = ""
                                        selectedContainerId = ""
                                        isItemContainer = false
                                    }
                                },
                                enabled = selectedItemId.isNotBlank()
                            )
                        }
                    }
                }

                // Create Custom Item Section
                item {
                    DndCard(modifier = Modifier.fillMaxWidth()) {
                        DndCardHeader("Create Homebrew Item")
                        DndDivider()

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DndTextInput(
                                value = customName,
                                onValueChange = { customName = it },
                                label = "Item Name",
                                placeholder = "e.g., Magic Sword"
                            )
                            
                            DndTextInput(
                                value = customCategory,
                                onValueChange = { customCategory = it },
                                label = "Category (Weapon, Potion, etc.)",
                                placeholder = "e.g., Weapon"
                            )

                            if (customCategory.contains("weapon", ignoreCase = true)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    DndTextInput(
                                        value = customDamage,
                                        onValueChange = { customDamage = it },
                                        label = "Damage Dice",
                                        placeholder = "1d8 slashing",
                                        modifier = Modifier.weight(1f)
                                    )
                                    DndTextInput(
                                        value = customRange,
                                        onValueChange = { customRange = it },
                                        label = "Range",
                                        placeholder = "Melee or 20/60",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DndTextInput(
                                    value = customRarity,
                                    onValueChange = { customRarity = it },
                                    label = "Rarity",
                                    placeholder = "Homebrew",
                                    modifier = Modifier.weight(1f)
                                )
                                DndTextInput(
                                    value = customWeight,
                                    onValueChange = { customWeight = it },
                                    label = "Weight (lb.)",
                                    placeholder = "0.0",
                                    modifier = Modifier.weight(0.5f)
                                )
                                DndTextInput(
                                    value = customValue,
                                    onValueChange = { customValue = it },
                                    label = "Value (gp)",
                                    placeholder = "0",
                                    modifier = Modifier.weight(0.5f)
                                )
                            }

                            // Is Container Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = customIsContainer,
                                    onCheckedChange = { customIsContainer = it },
                                    colors = CheckboxDefaults.colors(checkedColor = DnDGold, uncheckedColor = DnDMutedText)
                                )
                                Text(
                                    "This is a container",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DnDLightText
                                )
                            }

                            DndTextInput(
                                value = customDescription,
                                onValueChange = { customDescription = it },
                                label = "Description",
                                placeholder = "Detailed description..."
                            )
                            DndButton(
                                text = "Create Custom Item",
                                onClick = {
                                    if (customName.isNotBlank()) {
                                        viewModel.createCustomItem(
                                            name = customName.trim(),
                                            rarity = if (customRarity.isBlank()) "Homebrew" else customRarity.trim(),
                                            description = if (customIsContainer) "🗂 CONTAINER\n${customDescription.trim()}" else customDescription.trim(),
                                            weight = if (customWeight.isNotBlank()) "${customWeight.trim()} lb." else "",
                                            value = if (customValue.isNotBlank()) "${customValue.trim()} gp" else "",
                                            category = customCategory.trim(),
                                            damage = customDamage.trim(),
                                            range = customRange.trim()
                                        )
                                        customName = ""
                                        customRarity = "Homebrew"
                                        customDescription = ""
                                        customWeight = ""
                                        customValue = ""
                                        customIsContainer = false
                                        customCategory = ""
                                        customDamage = ""
                                        customRange = ""
                                    }
                                },
                                enabled = customName.isNotBlank()
                            )
                        }
                    }
                }
            } else {
                // Inventory View
                if (inventory.isEmpty()) {
                    item {
                        DndCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Inventory is empty",
                                style = MaterialTheme.typography.titleMedium,
                                color = DnDMutedText,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            "📦 Inventory Items (${inventory.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = DnDGold
                        )
                    }

                    // Separate containers from regular items
                    val containers = inventory.filter { it.notes.contains("🗂 CONTAINER", ignoreCase = true) || it.containerName?.startsWith("🗂") == true }
                    val nonContainerItems = inventory.filter { it.notes.isEmpty() || !it.notes.contains("🗂 CONTAINER", ignoreCase = true) }
                    val itemsInContainers = nonContainerItems.groupBy { it.containerName }

                    // Display items in containers
                    containers.forEach { container ->
                        item {
                            val isExpanded = expandedContainers.contains(container.id.toString())
                            val itemsInThisContainer = itemsInContainers[container.name] ?: emptyList()
                            
                            val weightVal = container.weight.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
                            val containerWeightTotal = weightVal * container.quantity
                            
                            val contentWeight = itemsInThisContainer.sumOf { item ->
                                val w = item.weight.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
                                w * item.quantity
                            }
                            val totalWeight = containerWeightTotal + contentWeight

                            val valueVal = container.value.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
                            val containerValueTotal = valueVal * container.quantity
                            val contentValue = itemsInThisContainer.sumOf { item ->
                                val v = item.value.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
                                v * item.quantity
                            }
                            val totalValue = containerValueTotal + contentValue

                            DndCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Button(
                                                onClick = {
                                                    expandedContainers = if (isExpanded) {
                                                        expandedContainers - container.id.toString()
                                                    } else {
                                                        expandedContainers + container.id.toString()
                                                    }
                                                },
                                                modifier = Modifier.weight(0.1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = DnDGold,
                                                    contentColor = DnDDarkBg
                                                )
                                            ) {
                                                Text(if (isExpanded) "▼" else "▶", fontWeight = FontWeight.Bold)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "🗂 ${container.name}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = DnDGold,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        "${itemsInThisContainer.size} items",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = DnDMutedText
                                                    )
                                                    if (totalWeight > 0) {
                                                        val weightDisplay = if (container.quantity > 1) {
                                                            "${String.format(Locale.US, "%.1f", totalWeight)} lb. (${String.format(Locale.US, "%.1f", totalWeight / container.quantity)} ea.)"
                                                        } else {
                                                            "${String.format(Locale.US, "%.1f", totalWeight)} lb."
                                                        }
                                                        Text("• $weightDisplay", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                                                    }
                                                    if (totalValue > 0) {
                                                        val valueDisplay = if (container.quantity > 1) {
                                                            "${String.format(Locale.US, "%.1f", totalValue)} gp (${String.format(Locale.US, "%.1f", totalValue / container.quantity)} ea.)"
                                                        } else {
                                                            "${String.format(Locale.US, "%.1f", totalValue)} gp"
                                                        }
                                                        Text("• $valueDisplay", style = MaterialTheme.typography.bodySmall, color = DnDGold)
                                                    }
                                                }
                                            }
                                        }
                                        Text(
                                            "Qty: ${container.quantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DnDGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (isExpanded) {
                                        DndDivider()
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            if (itemsInThisContainer.isEmpty()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Container is empty", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                                                    Button(
                                                        onClick = { viewModel.updateInventoryQuantity(container.id, 0) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                                                    ) {
                                                        Text("Delete", color = Color.White, fontSize = 10.sp)
                                                    }
                                                }
                                            } else {
                                                itemsInThisContainer.forEach { itemInContainer ->
                                                    InventoryItemRow(
                                                        item = itemInContainer,
                                                        viewModel = viewModel,
                                                        inventory = inventory,
                                                        isExpanded = expandedItems.contains("cont_${itemInContainer.id}"),
                                                        onToggleExpand = {
                                                            expandedItems = if (expandedItems.contains("cont_${itemInContainer.id}")) {
                                                                expandedItems - "cont_${itemInContainer.id}"
                                                            } else {
                                                                expandedItems + "cont_${itemInContainer.id}"
                                                            }
                                                        },
                                                        modifier = Modifier.padding(start = 16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Display regular items (not in containers)
                    items(nonContainerItems.filter { it.containerName == null || it.containerName.isNullOrBlank() }) { item ->
                        InventoryItemRow(
                            item = item,
                            viewModel = viewModel,
                            inventory = inventory,
                            isExpanded = expandedItems.contains("reg_${item.id}"),
                            onToggleExpand = {
                                expandedItems = if (expandedItems.contains("reg_${item.id}")) {
                                    expandedItems - "reg_${item.id}"
                                } else {
                                    expandedItems + "reg_${item.id}"
                                }
                            }
                        )
                    }
                    
                    // Total Weight and Value Summary
                    item {
                        val totalWeight = inventory.sumOf { item ->
                            val weightVal = item.weight.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
                            weightVal * item.quantity
                        }
                        val totalValue = inventory.sumOf { item ->
                            val valueVal = item.value.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
                            valueVal * item.quantity
                        }
                        
                        DndCard(modifier = Modifier.fillMaxWidth()) {
                            DndCardHeader("📊 Inventory Summary")
                            DndDivider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Total Weight",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DnDMutedText
                                    )
                                    Text(
                                        "${String.format(Locale.US, "%.1f", totalWeight)} lb.",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = DnDGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Total Value",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DnDMutedText
                                    )
                                    Text(
                                        "${String.format(Locale.US, "%.1f", totalValue)} gp",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = DnDGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryDisplayItem,
    viewModel: DndViewModel,
    inventory: List<InventoryDisplayItem>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weightVal = item.weight.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
    val totalWeight = weightVal * item.quantity
    val valueVal = item.value.split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
    val totalValue = valueVal * item.quantity

    var editableNotes by remember(item.id, isExpanded) { mutableStateOf(item.notes) }
    var containerMenuExpanded by remember { mutableStateOf(false) }

    DndCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DnDLightText,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            item.rarity,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (item.rarity.lowercase()) {
                                "common" -> Color(0xFF888888)
                                "uncommon" -> Color(0xFF4CAF50)
                                "rare" -> Color(0xFF2196F3)
                                "very rare" -> Color(0xFF9C27B0)
                                "legendary" -> Color(0xFFFF9800)
                                "artifact" -> Color(0xFFFFB300)
                                else -> DnDGold
                            },
                            fontWeight = FontWeight.Bold
                        )
                        if (totalWeight > 0) {
                            val weightDisplay = if (item.quantity > 1) {
                                "${String.format(Locale.US, "%.1f", totalWeight)} lb. (${item.weight} ea.)"
                            } else {
                                item.weight
                            }
                            Text(
                                "• $weightDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = DnDMutedText
                            )
                        }
                        if (totalValue > 0) {
                            val valueDisplay = if (item.quantity > 1) {
                                "${String.format(Locale.US, "%.1f", totalValue)} gp (${item.value} ea.)"
                            } else {
                                item.value
                            }
                            Text(
                                "• $valueDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = DnDGold
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text(
                        "Qty: ${item.quantity}",
                        style = MaterialTheme.typography.titleSmall,
                        color = DnDGold,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onToggleExpand,
                        modifier = Modifier.padding(top = 4.dp),
                        contentPadding = PaddingValues(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DnDGold.copy(alpha = 0.7f),
                            contentColor = DnDDarkBg
                        )
                    ) {
                        Text(if (isExpanded) "▼" else "▶", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            // Expanded Details
            if (isExpanded) {
                DndDivider()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    
                    // Weapon Properties
                    if (item.category.contains("weapon", ignoreCase = true) || item.damage != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item.damage?.let {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Damage:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                    Text(it, style = MaterialTheme.typography.bodyMedium, color = DnDGold, fontWeight = FontWeight.Bold)
                                }
                            }
                            item.range?.let {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Range:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                    Text(it, style = MaterialTheme.typography.bodyMedium, color = DnDLightText)
                                }
                            }
                        }
                        item.properties?.let {
                            Column {
                                Text("Properties:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                Text(it, style = MaterialTheme.typography.bodySmall, color = DnDLightText)
                            }
                        }
                        DndDivider()
                    }

                    // Description Section
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Description:",
                            style = MaterialTheme.typography.labelSmall,
                            color = DnDMutedText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (item.description.isNotBlank()) item.description else "No description available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DnDLightText
                        )
                    }

                    // Metadata Row (Rarity and Sourcebook)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Rarity:",
                                style = MaterialTheme.typography.labelSmall,
                                color = DnDMutedText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                item.rarity,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DnDLightText
                            )
                            
                            Spacer(modifier = Modifier.padding(top = 8.dp))
                            
                            Text(
                                "Value (ea):",
                                style = MaterialTheme.typography.labelSmall,
                                color = DnDMutedText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (item.value.isNotBlank()) item.value else "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DnDGold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Sourcebook:",
                                style = MaterialTheme.typography.labelSmall,
                                color = DnDMutedText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                item.sourcebook,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DnDLightText
                            )

                            Spacer(modifier = Modifier.padding(top = 8.dp))

                            Text(
                                "Weight (ea):",
                                style = MaterialTheme.typography.labelSmall,
                                color = DnDMutedText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (item.weight.isNotBlank()) item.weight else "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DnDLightText
                            )
                        }
                    }

                    // Properties Grid (Total Weight and Value)
                    if (item.quantity > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (item.weight.isNotBlank()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Weight (Total):", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                    Text("${String.format(Locale.US, "%.1f", totalWeight)} lb.", style = MaterialTheme.typography.bodyMedium, color = DnDLightText)
                                }
                            }
                            if (item.value.isNotBlank()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Value (Total):", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                    Text("${String.format(Locale.US, "%.1f", totalValue)} gp", style = MaterialTheme.typography.bodyMedium, color = DnDGold)
                                }
                            }
                        }
                    }

                    // Other metadata
                    if (item.category.isNotBlank() || item.type.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (item.category.isNotBlank()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Category:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                    Text(item.category, style = MaterialTheme.typography.bodyMedium, color = DnDLightText)
                                }
                            }
                            if (item.type.isNotBlank()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Type:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                                    Text(item.type, style = MaterialTheme.typography.bodyMedium, color = DnDLightText)
                                }
                            }
                        }
                    }

                    DndDivider()

                    // Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val newQty = (item.quantity - 1).coerceAtLeast(0)
                                viewModel.updateInventoryQuantity(item.id, newQty)
                            },
                            modifier = Modifier.weight(0.15f),
                            colors = ButtonDefaults.buttonColors(containerColor = DnDGold, contentColor = DnDDarkBg)
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DnDLightText,
                            modifier = Modifier.weight(0.15f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.updateInventoryQuantity(item.id, item.quantity + 1) },
                            modifier = Modifier.weight(0.15f),
                            colors = ButtonDefaults.buttonColors(containerColor = DnDGold, contentColor = DnDDarkBg)
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(0.55f)
                        ) {
                            Checkbox(
                                checked = item.equipped,
                                onCheckedChange = { viewModel.updateInventoryEquipped(item.id, it) },
                                colors = CheckboxDefaults.colors(checkedColor = DnDGold, uncheckedColor = DnDMutedText)
                            )
                            Text("Equipped", style = MaterialTheme.typography.bodySmall, color = DnDLightText)
                        }
                    }

                    // Container Assignment
                    val availableContainers = inventory.filter { 
                        (it.notes.contains("🗂 CONTAINER", ignoreCase = true) || it.containerName?.startsWith("🗂") == true) &&
                        it.id != item.id 
                    }
                    if (availableContainers.isNotEmpty()) {
                        Column {
                            Text("Assigned to Container:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                            Box {
                                Button(
                                    onClick = { containerMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DnDCardBg, contentColor = DnDGold)
                                ) {
                                    Text(item.containerName ?: "No Container", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                DropdownMenu(
                                    expanded = containerMenuExpanded,
                                    onDismissRequest = { containerMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = {
                                            viewModel.updateInventoryContainer(item.id, null)
                                            containerMenuExpanded = false
                                        }
                                    )
                                    availableContainers.forEach { container ->
                                        DropdownMenuItem(
                                            text = { Text(container.name) },
                                            onClick = {
                                                viewModel.updateInventoryContainer(item.id, container.name)
                                                containerMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    DndDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Notes:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, fontWeight = FontWeight.Bold)
                        DndTextInput(
                            value = editableNotes,
                            onValueChange = { 
                                editableNotes = it
                                viewModel.updateInventoryNotes(item.id, it)
                            },
                            label = "Item Notes",
                            placeholder = "Add notes here...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
