package com.example.dndinventorymanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndinventorymanager.data.entities.ItemEntity
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

@Composable
fun AdminScreen(
    viewModel: DndViewModel,
    onNavigateSpellsAdmin: () -> Unit
) {
    val baseItems by viewModel.baseItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showReloadConfirmation by remember { mutableStateOf(false) }
    var showExtraSyncConfirmation by remember { mutableStateOf(false) }
    
    var itemId by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemRarity by remember { mutableStateOf("Common") }
    var itemSourcebook by remember { mutableStateOf("PHB") }
    var itemDescription by remember { mutableStateOf("") }
    var itemWeight by remember { mutableStateOf("") }
    var itemValue by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }
    var itemDamage by remember { mutableStateOf("") }
    var itemRange by remember { mutableStateOf("") }
    var itemProperties by remember { mutableStateOf("") }

    val filteredItems = remember(baseItems, searchQuery) {
        if (searchQuery.isBlank()) {
            baseItems
        } else {
            baseItems.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Are you sure?", color = DnDGold) },
            text = { Text("This will permanently remove all base items from the database.", color = DnDLightText) },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllBaseItems()
                    showDeleteConfirmation = false
                    showReloadConfirmation = true
                }) {
                    Text("DELETE ALL", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("CANCEL", color = DnDGold)
                }
            }
        )
    }

    if (showReloadConfirmation) {
        AlertDialog(
            onDismissRequest = { showReloadConfirmation = false },
            title = { Text("Reload Standard Items?", color = DnDGold) },
            text = { Text("Do you want to reload all standard items from the API now?", color = DnDLightText) },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.performInitialSync(force = true)
                    showReloadConfirmation = false
                }) {
                    Text("YES", color = DnDGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReloadConfirmation = false }) {
                    Text("NO", color = DnDMutedText)
                }
            }
        )
    }

    if (showExtraSyncConfirmation) {
        AlertDialog(
            onDismissRequest = { showExtraSyncConfirmation = false },
            title = { Text("Add Extra Items?", color = DnDGold) },
            text = { Text("This will add another 1000+ items, not all have been checked. do you still want to add this?", color = DnDLightText) },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.syncExtraItems()
                    showExtraSyncConfirmation = false
                }) {
                    Text("YES", color = DnDGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtraSyncConfirmation = false }) {
                    Text("CANCEL", color = DnDMutedText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Navigation
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { /* Already on Items */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DnDGold,
                    contentColor = DnDDarkBg
                )
            ) {
                Text("Items", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = onNavigateSpellsAdmin,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DnDDarkBg,
                    contentColor = DnDGold
                )
            ) {
                Text("Spells", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Header
        Text(
            "⚙ Admin: Items",
            style = MaterialTheme.typography.headlineSmall,
            color = DnDGold,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Item Management Card
            item {
                DndCard(modifier = Modifier.fillMaxWidth()) {
                    DndCardHeader("Add or Update Item")
                    DndDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndTextInput(
                                value = itemId,
                                onValueChange = { itemId = it },
                                label = "Item ID",
                                placeholder = "e.g., longsword",
                                modifier = Modifier.weight(1f)
                            )
                            DndTextInput(
                                value = itemName,
                                onValueChange = { itemName = it },
                                label = "Name",
                                placeholder = "Full item name",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DndTextInput(
                                value = itemRarity,
                                onValueChange = { itemRarity = it },
                                label = "Rarity",
                                placeholder = "Common",
                                modifier = Modifier.weight(1f)
                            )
                            DndTextInput(
                                value = itemSourcebook,
                                onValueChange = { itemSourcebook = it },
                                label = "Sourcebook",
                                placeholder = "PHB",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DndTextInput(
                                value = itemWeight,
                                onValueChange = { itemWeight = it },
                                label = "Weight",
                                placeholder = "3 lb.",
                                modifier = Modifier.weight(1f)
                            )
                            DndTextInput(
                                value = itemValue,
                                onValueChange = { itemValue = it },
                                label = "Value",
                                placeholder = "15 gp",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DndTextInput(
                                value = itemCategory,
                                onValueChange = { itemCategory = it },
                                label = "Category",
                                placeholder = "Weapon",
                                modifier = Modifier.weight(1f)
                            )
                            DndTextInput(
                                value = itemType,
                                onValueChange = { itemType = it },
                                label = "Type/Category Range",
                                placeholder = "Simple Melee",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DndTextInput(
                                value = itemDamage,
                                onValueChange = { itemDamage = it },
                                label = "Damage",
                                placeholder = "1d8 slashing",
                                modifier = Modifier.weight(1f)
                            )
                            DndTextInput(
                                value = itemRange,
                                onValueChange = { itemRange = it },
                                label = "Range",
                                placeholder = "Melee",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        DndTextInput(
                            value = itemProperties,
                            onValueChange = { itemProperties = it },
                            label = "Properties",
                            placeholder = "Versatile (1d10), Thrown (20/60)"
                        )

                        DndTextInput(
                            value = itemDescription,
                            onValueChange = { itemDescription = it },
                            label = "Description",
                            placeholder = "Item description"
                        )

                        // Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DndButton(
                                text = "➕ Add/Update",
                                onClick = {
                                    if (itemId.isNotBlank() && itemName.isNotBlank()) {
                                        viewModel.createBaseItem(
                                            id = itemId.trim(),
                                            name = itemName.trim(),
                                            rarity = itemRarity.trim(),
                                            sourcebook = itemSourcebook.trim(),
                                            description = itemDescription.trim(),
                                            weight = itemWeight.trim(),
                                            value = itemValue.trim(),
                                            category = itemCategory.trim(),
                                            type = itemType.trim(),
                                            damage = itemDamage.trim().ifBlank { null },
                                            range = itemRange.trim().ifBlank { null },
                                            properties = itemProperties.trim().ifBlank { null }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            DndButton(
                                text = "🗑 Remove",
                                onClick = {
                                    if (itemId.isNotBlank()) {
                                        viewModel.deleteBaseItem(itemId.trim())
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Sync and Delete Actions
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DndButton(
                        text = "🔄 Extra Items",
                        onClick = { showExtraSyncConfirmation = true },
                        modifier = Modifier.weight(1f)
                    )
                    DndButton(
                        text = "🗑 Delete All",
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search and Items List Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "📚 Base Items (${filteredItems.size}/${baseItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DnDGold
                    )
                    DndTextInput(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search Items",
                        placeholder = "Search by name, ID, or category..."
                    )
                }
            }

            // Items List
            items(filteredItems) { item ->
                AdminItemRow(item = item, onLoadForEdit = {
                    itemId = item.id
                    itemName = item.name
                    itemRarity = item.rarity
                    itemSourcebook = item.sourcebook
                    itemDescription = item.description
                    itemWeight = item.weight
                    itemValue = item.value
                    itemCategory = item.category
                    itemType = item.type
                    itemDamage = item.damage ?: ""
                    itemRange = item.range ?: ""
                    itemProperties = item.properties ?: ""
                })
            }
        }
    }
}

@Composable
fun AdminItemRow(
    item: ItemEntity,
    onLoadForEdit: () -> Unit
) {
    val rarityColor = when (item.rarity.lowercase()) {
        "common" -> Color(0xFF888888)
        "uncommon" -> Color(0xFF4CAF50)
        "rare" -> Color(0xFF2196F3)
        "very rare" -> Color(0xFF9C27B0)
        "legendary" -> Color(0xFFFF9800)
        "artifact" -> Color(0xFFFFB300)
        else -> DnDGold
    }

    DndCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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
                            color = rarityColor,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.weight.isNotBlank()) {
                            Text("• ${item.weight}", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                        }
                        if (item.value.isNotBlank()) {
                            Text("• ${item.value}", style = MaterialTheme.typography.bodySmall, color = DnDGold)
                        }
                    }
                }
                DndButton(
                    text = "📝 Edit",
                    onClick = onLoadForEdit,
                    modifier = Modifier.weight(0.3f)
                )
            }

            DndDivider()
            
            Text(
                "ID: ${item.id} • ${item.sourcebook}",
                style = MaterialTheme.typography.labelSmall,
                color = DnDMutedText
            )
            
            if (item.category.isNotBlank() || item.type.isNotBlank()) {
                Text(
                    "${item.category}${if (item.category.isNotBlank() && item.type.isNotBlank()) " • " else ""}${item.type}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DnDGold
                )
            }

            if (item.damage != null || item.range != null) {
                Text(
                    "${item.damage ?: ""} • ${item.range ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DnDLightText,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = DnDLightText
            )
        }
    }
}
