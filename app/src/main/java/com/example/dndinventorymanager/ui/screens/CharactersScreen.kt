package com.example.dndinventorymanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dndinventorymanager.ui.DndViewModel
import com.example.dndinventorymanager.ui.components.CharacterCard
import com.example.dndinventorymanager.ui.components.DndButton
import com.example.dndinventorymanager.ui.components.DndCard
import com.example.dndinventorymanager.ui.components.DndCardHeader
import com.example.dndinventorymanager.ui.components.DndDivider
import com.example.dndinventorymanager.ui.components.DndTextInput
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText
import com.example.dndinventorymanager.ui.theme.DnDMutedText

@Composable
fun CharactersScreen(
    viewModel: DndViewModel,
    onNavigateInventory: () -> Unit
) {
    val characters by viewModel.characters.collectAsState()
    val activeCharacter by viewModel.activeCharacter.collectAsState()

    var name by remember { mutableStateOf("") }
    var clazz by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }
    var gold by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            "⚔ Character Management",
            style = MaterialTheme.typography.headlineSmall,
            color = DnDGold
        )

        // Create Character Card
        DndCard {
            DndCardHeader("Create New Character")
            DndDivider()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DndTextInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Character Name",
                    placeholder = "e.g., Aragorn, Gandalf"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DndTextInput(
                        value = clazz,
                        onValueChange = { clazz = it },
                        label = "Class",
                        placeholder = "e.g., Warrior, Mage",
                        modifier = Modifier.weight(1f)
                    )
                    DndTextInput(
                        value = level,
                        onValueChange = { level = it },
                        label = "Level",
                        placeholder = "1-20",
                        modifier = Modifier.weight(0.5f)
                    )
                }
                DndTextInput(
                    value = gold,
                    onValueChange = { gold = it },
                    label = "Starting Gold",
                    placeholder = "0"
                )
                DndButton(
                    text = "Create Character",
                    onClick = {
                        val lvl = level.toIntOrNull() ?: 1
                        val g = gold.toIntOrNull() ?: 0
                        if (name.isNotBlank() && clazz.isNotBlank()) {
                            viewModel.createCharacter(name.trim(), clazz.trim(), lvl, g)
                            name = ""
                            clazz = ""
                            level = "1"
                            gold = "0"
                        }
                    }
                )
            }
        }

        // Active Character Indicator
        activeCharacter?.let {
            DndCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "★ Active Character",
                    style = MaterialTheme.typography.labelSmall,
                    color = DnDGold
                )
                DndDivider()
                Text(
                    "${it.name} • ${it.clazz} • Level ${it.level}",
                    style = MaterialTheme.typography.titleMedium,
                    color = DnDLightText
                )
            }
        }

        // Characters List
        if (characters.isEmpty()) {
            Text(
                "No characters created yet. Create your first adventure companions above!",
                style = MaterialTheme.typography.bodyMedium,
                color = DnDMutedText
            )
        } else {
            Text(
                "Your Party (${characters.size})",
                style = MaterialTheme.typography.titleMedium,
                color = DnDGold
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(characters) { character ->
                    CharacterCard(
                        name = character.name,
                        clazz = character.clazz,
                        level = character.level,
                        gold = character.gold,
                        isActive = activeCharacter?.id == character.id,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DndDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DndButton(
                                text = if (activeCharacter?.id == character.id) "★ Active" else "Set Active",
                                onClick = { viewModel.setActiveCharacter(character.id) },
                                modifier = Modifier.weight(1f)
                            )
                            DndButton(
                                text = "📦 Inventory",
                                onClick = onNavigateInventory,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
