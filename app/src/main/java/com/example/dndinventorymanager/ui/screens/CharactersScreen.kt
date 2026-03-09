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
import com.example.dndinventorymanager.data.entities.CharacterEntity
import com.example.dndinventorymanager.ui.DndViewModel
import com.example.dndinventorymanager.ui.components.CharacterCard
import com.example.dndinventorymanager.ui.components.DndButton
import com.example.dndinventorymanager.ui.components.DndCard
import com.example.dndinventorymanager.ui.components.DndCardHeader
import com.example.dndinventorymanager.ui.components.DndDivider
import com.example.dndinventorymanager.ui.components.DndTextInput
import com.example.dndinventorymanager.ui.theme.DnDCardBg
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText
import com.example.dndinventorymanager.ui.theme.DnDMutedText

@Composable
fun CharactersScreen(viewModel: DndViewModel) {
    val characters by viewModel.characters.collectAsState()
    val activeId by viewModel.activeCharacterId.collectAsState()

    var name by remember { mutableStateOf("") }
    var clazz by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }
    var gold by remember { mutableStateOf("0") }
    var spellSlots by remember { mutableStateOf("0,0,0,0,0,0,0,0,0") }
    var maxPrepared by remember { mutableStateOf("0") }

    var characterToDelete by remember { mutableStateOf<Long?>(null) }
    var characterToEdit by remember { mutableStateOf<CharacterEntity?>(null) }

    if (characterToDelete != null) {
        AlertDialog(
            onDismissRequest = { characterToDelete = null },
            title = { Text("Delete Character?", color = DnDGold) },
            text = { Text("Are you sure you want to permanently delete this character and all their data?", color = DnDLightText) },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    characterToDelete?.let { viewModel.deleteCharacter(it) }
                    characterToDelete = null
                }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { characterToDelete = null }) {
                    Text("CANCEL", color = DnDGold)
                }
            }
        )
    }

    if (characterToEdit != null) {
        var editSlots by remember { mutableStateOf(characterToEdit!!.spellSlots) }
        var editPrepared by remember { mutableStateOf(characterToEdit!!.maxPrepared.toString()) }
        var editGold by remember { mutableStateOf(characterToEdit!!.gold.toString()) }

        AlertDialog(
            onDismissRequest = { characterToEdit = null },
            title = { Text("Edit Character: ${characterToEdit!!.name}", color = DnDGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DndTextInput(value = editGold, onValueChange = { editGold = it }, label = "Gold")
                    DndTextInput(value = editSlots, onValueChange = { editSlots = it }, label = "Max Spell Slots (CSV)")
                    DndTextInput(value = editPrepared, onValueChange = { editPrepared = it }, label = "Max Prepared Spells")
                }
            },
            containerColor = DnDCardBg,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateCharacterSpellcasting(characterToEdit!!.id, editSlots, editPrepared.toIntOrNull() ?: 0)
                    viewModel.updateCharacterGold(characterToEdit!!.id, editGold.toIntOrNull() ?: 0)
                    characterToEdit = null
                }) {
                    Text("SAVE", color = DnDGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { characterToEdit = null }) {
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
        Text(
            "⚔ Characters",
            style = MaterialTheme.typography.headlineSmall,
            color = DnDGold,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                DndCard(modifier = Modifier.fillMaxWidth()) {
                    DndCardHeader("Create New Character")
                    DndDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DndTextInput(value = name, onValueChange = { name = it }, label = "Name")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DndTextInput(value = clazz, onValueChange = { clazz = it }, label = "Class", modifier = Modifier.weight(1f))
                            DndTextInput(value = level, onValueChange = { level = it }, label = "Level", modifier = Modifier.weight(0.5f))
                        }
                        DndTextInput(value = gold, onValueChange = { gold = it }, label = "Gold")
                        
                        Text("Spellcasting (Optional)", style = MaterialTheme.typography.labelSmall, color = DnDGold)
                        DndTextInput(value = spellSlots, onValueChange = { spellSlots = it }, label = "Slots (Lvl 1-9 CSV)", placeholder = "e.g. 4,3,2,0,0,0,0,0,0")
                        DndTextInput(value = maxPrepared, onValueChange = { maxPrepared = it }, label = "Max Prepared Spells")

                        DndButton(text = "Create Character", onClick = {
                            if (name.isNotBlank()) {
                                viewModel.createCharacter(
                                    name, clazz, level.toIntOrNull() ?: 1, gold.toIntOrNull() ?: 0,
                                    spellSlots, maxPrepared.toIntOrNull() ?: 0
                                )
                                name = ""
                                clazz = ""
                                level = "1"
                                gold = "0"
                                spellSlots = "0,0,0,0,0,0,0,0,0"
                                maxPrepared = "0"
                            }
                        })
                    }
                }
            }

            item {
                Text(
                    "Your Adventurers",
                    style = MaterialTheme.typography.titleMedium,
                    color = DnDGold
                )
            }

            items(characters) { character ->
                CharacterCard(
                    name = character.name,
                    clazz = character.clazz,
                    level = character.level,
                    gold = character.gold,
                    isActive = character.id == activeId
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (character.id != activeId) {
                            DndButton(
                                text = "Set Active",
                                onClick = { viewModel.setActiveCharacter(character.id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            DndButton(
                                text = "📝 Edit",
                                onClick = { characterToEdit = character },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        DndButton(
                            text = "🗑 Delete",
                            onClick = { characterToDelete = character.id },
                            modifier = Modifier.weight(0.4f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f), contentColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}
