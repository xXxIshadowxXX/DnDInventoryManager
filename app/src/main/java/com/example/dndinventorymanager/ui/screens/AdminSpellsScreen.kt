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
import com.example.dndinventorymanager.data.entities.SpellEntity
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
fun AdminSpellsScreen(
    viewModel: DndViewModel,
    onNavigateItemsAdmin: () -> Unit
) {
    val spells by viewModel.spells.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showReloadConfirmation by remember { mutableStateOf(false) }
    var showAdvancedSyncConfirmation by remember { mutableStateOf(false) }

    var spellId by remember { mutableStateOf("") }
    var spellName by remember { mutableStateOf("") }
    var spellLevel by remember { mutableStateOf("0") }
    var spellSchool by remember { mutableStateOf("") }
    var spellCastingTime by remember { mutableStateOf("") }
    var spellRange by remember { mutableStateOf("") }
    var spellComponents by remember { mutableStateOf("") }
    var spellDuration by remember { mutableStateOf("") }
    var spellDescription by remember { mutableStateOf("") }
    var spellSourcebook by remember { mutableStateOf("Homebrew") }
    var spellClasses by remember { mutableStateOf("") }

    val filteredSpells = remember(spells, searchQuery) {
        if (searchQuery.isBlank()) spells
        else spells.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true) }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Are you sure?", color = DnDGold) },
            text = { Text("Permanently remove all spells from the database.", color = DnDLightText) },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllSpells()
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
            title = { Text("Reload Standard Spells?", color = DnDGold) },
            text = { Text("Do you want to reload all standard spells from the API now?", color = DnDLightText) },
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

    if (showAdvancedSyncConfirmation) {
        AlertDialog(
            onDismissRequest = { showAdvancedSyncConfirmation = false },
            title = { Text("Switch to Advanced Spells?", color = DnDGold) },
            text = { Text("This will DELETE ALL current spells and replace them with detailed spells from Open5e. This process may take a minute.", color = DnDLightText) },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.syncAdvancedSpells()
                    showAdvancedSyncConfirmation = false
                }) {
                    Text("PROCEED", color = DnDGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdvancedSyncConfirmation = false }) {
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
                onClick = onNavigateItemsAdmin,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DnDDarkBg,
                    contentColor = DnDGold
                )
            ) {
                Text("Items", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = { /* Already on Spells */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DnDGold,
                    contentColor = DnDDarkBg
                )
            ) {
                Text("Spells", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Text("🪄 Admin: Spells", style = MaterialTheme.typography.headlineSmall, color = DnDGold, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                DndCard(modifier = Modifier.fillMaxWidth()) {
                    DndCardHeader("Add or Update Spell")
                    DndDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndTextInput(value = spellId, onValueChange = { spellId = it }, label = "ID", modifier = Modifier.weight(1f))
                            DndTextInput(value = spellName, onValueChange = { spellName = it }, label = "Name", modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndTextInput(value = spellLevel, onValueChange = { spellLevel = it }, label = "Level (0-9)", modifier = Modifier.weight(0.5f))
                            DndTextInput(value = spellSchool, onValueChange = { spellSchool = it }, label = "School", modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndTextInput(value = spellCastingTime, onValueChange = { spellCastingTime = it }, label = "Casting Time", modifier = Modifier.weight(1f))
                            DndTextInput(value = spellRange, onValueChange = { spellRange = it }, label = "Range", modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndTextInput(value = spellComponents, onValueChange = { spellComponents = it }, label = "Components", modifier = Modifier.weight(1f))
                            DndTextInput(value = spellDuration, onValueChange = { spellDuration = it }, label = "Duration", modifier = Modifier.weight(1f))
                        }
                        DndTextInput(value = spellClasses, onValueChange = { spellClasses = it }, label = "Classes (CSV)")
                        DndTextInput(value = spellDescription, onValueChange = { spellDescription = it }, label = "Description")
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndButton(text = "➕ Add/Update", onClick = {
                                viewModel.createSpell(spellId, spellName, spellLevel.toIntOrNull() ?: 0, spellSchool, spellCastingTime, spellRange, spellComponents, spellDuration, spellDescription, spellSourcebook, spellClasses)
                            }, modifier = Modifier.weight(1f))
                            DndButton(text = "🗑 Delete", onClick = { viewModel.deleteSpell(spellId) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DndButton(text = "🔥 Advanced Menu", onClick = { showAdvancedSyncConfirmation = true }, modifier = Modifier.weight(1f))
                    DndButton(text = "🗑 Delete All", onClick = { showDeleteConfirmation = true }, modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "🪄 Base Spells (${filteredSpells.size}/${spells.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DnDGold
                    )
                    DndTextInput(value = searchQuery, onValueChange = { searchQuery = it }, label = "Search Spells")
                }
            }

            items(filteredSpells) { spell ->
                DndCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(spell.name, style = MaterialTheme.typography.titleMedium, color = DnDLightText, fontWeight = FontWeight.Bold)
                            Text("Level ${spell.level} • ${spell.school}", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                        }
                        DndButton(text = "📝 Edit", onClick = {
                            spellId = spell.id
                            spellName = spell.name
                            spellLevel = spell.level.toString()
                            spellSchool = spell.school
                            spellCastingTime = spell.castingTime
                            spellRange = spell.range
                            spellComponents = spell.components
                            spellDuration = spell.duration
                            spellDescription = spell.description
                            spellSourcebook = spell.sourcebook
                            spellClasses = spell.classes
                        }, modifier = Modifier.weight(0.3f))
                    }
                }
            }
        }
    }
}
