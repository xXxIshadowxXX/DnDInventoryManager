package com.example.dndinventorymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndinventorymanager.data.UserSpellDisplayItem
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
fun SpellsScreen(
    viewModel: DndViewModel,
    onNavigateCharacters: () -> Unit
) {
    val activeCharacter by viewModel.activeCharacter.collectAsState()
    val allSpells by viewModel.spells.collectAsState()
    val userSpells by viewModel.activeSpells.collectAsState()

    var tabIndex by remember { mutableStateOf(0) } // 0: All, 1: Learned, 2: Prepared
    var searchQuery by remember { mutableStateOf("") }
    
    // New Filters
    var selectedSchool by remember { mutableStateOf<String?>(null) }
    var selectedClass by remember { mutableStateOf<String?>(null) }
    
    var newLoadoutName by remember { mutableStateOf("") }
    var showRestMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var spellToLearnConfirm by remember { mutableStateOf<com.example.dndinventorymanager.data.entities.SpellEntity?>(null) }

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
                    "Please select an active character to manage their spellbook.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DnDLightText
                )
                DndButton(text = "⚔ Go to Characters", onClick = onNavigateCharacters)
            }
        }
        return
    }

    val character = activeCharacter!!
    val currentPrepared = userSpells.count { it.isPrepared && it.level > 0 }
    val maxPrepared = character.maxPrepared
    val maxSlotLevel = character.spellSlots.split(",")
        .mapIndexedNotNull { index, s -> if ((s.toIntOrNull() ?: 0) > 0) index + 1 else null }
        .maxOrNull() ?: 0

    if (showRestMenu) {
        RestMenuDialog(
            currentSlots = character.currentSlots,
            maxSlots = character.spellSlots,
            onDismiss = { showRestMenu = false },
            onFullRest = {
                viewModel.restCharacter(character.id)
                showRestMenu = false
            },
            onRecoverSlot = { level ->
                viewModel.recoverSpellSlot(character.id, level)
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Cast Error", color = Color.Red) },
            text = { Text(errorMessage!!, color = DnDLightText) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK", color = DnDGold)
                }
            },
            containerColor = DnDCardBg
        )
    }

    if (spellToLearnConfirm != null) {
        AlertDialog(
            onDismissRequest = { spellToLearnConfirm = null },
            title = { Text("High Level Spell", color = DnDGold) },
            text = { Text("This spell is level ${spellToLearnConfirm!!.level}, but your highest spell slot is level $maxSlotLevel. Learn it anyway?", color = DnDLightText) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.learnSpell(character.id, spellToLearnConfirm!!.id)
                    spellToLearnConfirm = null
                }) {
                    Text("LEARN", color = DnDGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { spellToLearnConfirm = null }) {
                    Text("CANCEL", color = DnDMutedText)
                }
            },
            containerColor = DnDCardBg
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("🪄 Spells", style = MaterialTheme.typography.headlineSmall, color = DnDGold, fontWeight = FontWeight.Bold)
                Text("${character.name} • Prepared: $currentPrepared/$maxPrepared", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
            }
            DndButton(
                text = "💤 Rest", 
                onClick = { showRestMenu = true },
                modifier = Modifier.heightIn(min = 32.dp).fillMaxWidth(0.3f)
            )
        }

        // Spell Slots Display
        SpellSlotsDisplay(character.currentSlots, character.spellSlots)

        // Tabs: All, Learned, Prepared
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DndTabButton("All", tabIndex == 0, modifier = Modifier.weight(1f)) { tabIndex = 0 }
            DndTabButton("Learned", tabIndex == 1, modifier = Modifier.weight(1f)) { tabIndex = 1 }
            DndTabButton("Prepared", tabIndex == 2, modifier = Modifier.weight(1f)) { tabIndex = 2 }
        }

        when (tabIndex) {
            0 -> AllSpellsList(
                allSpells = allSpells, 
                userSpells = userSpells, 
                searchQuery = searchQuery, 
                selectedSchool = selectedSchool,
                selectedClass = selectedClass,
                onSearchChange = { searchQuery = it }, 
                onSchoolChange = { selectedSchool = it },
                onClassChange = { selectedClass = it },
                viewModel = viewModel, 
                characterId = character.id, 
                maxSlotLevel = maxSlotLevel,
                onConfirmLearn = { spellToLearnConfirm = it }
            )
            1 -> LearnedSpellsList(userSpells, viewModel, character.id, character.currentSlots) { errorMessage = it }
            2 -> PreparedSpellsView(userSpells, viewModel, character.id, character.currentSlots, newLoadoutName, onLoadoutNameChange = { newLoadoutName = it }) { errorMessage = it }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DnDCardBg, contentColor = DnDGold),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (selected == null) label else "$label: $selected",
                fontSize = 10.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DnDCardBg)
        ) {
            DropdownMenuItem(
                text = { Text("Any $label", color = DnDLightText) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = DnDLightText) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AllSpellsList(
    allSpells: List<com.example.dndinventorymanager.data.entities.SpellEntity>, 
    userSpells: List<UserSpellDisplayItem>, 
    searchQuery: String, 
    selectedSchool: String?,
    selectedClass: String?,
    onSearchChange: (String) -> Unit, 
    onSchoolChange: (String?) -> Unit,
    onClassChange: (String?) -> Unit,
    viewModel: DndViewModel, 
    characterId: Long,
    maxSlotLevel: Int,
    onConfirmLearn: (com.example.dndinventorymanager.data.entities.SpellEntity) -> Unit
) {
    val learnedMap = userSpells.associateBy { it.spellId }
    
    // Extrapolate schools and classes for filters
    val schools = remember(allSpells) { allSpells.map { it.school }.distinct().sorted() }
    val classes = remember(allSpells) { 
        allSpells.flatMap { it.classes.split(",").map { c -> c.trim() } }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted() 
    }

    val filtered = allSpells.filter { spell ->
        val matchesName = spell.name.contains(searchQuery, ignoreCase = true)
        val matchesSchool = selectedSchool == null || spell.school == selectedSchool
        val matchesClass = selectedClass == null || spell.classes.contains(selectedClass, ignoreCase = true)
        matchesName && matchesSchool && matchesClass
    }
    
    val spellsByLevel = filtered.groupBy { it.level }
    val collapsedStates = remember { mutableStateMapOf<Int, Boolean>() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DndTextInput(value = searchQuery, onValueChange = onSearchChange, label = "Search Spells")
        
        // Filters Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterDropdown(
                label = "School",
                options = schools,
                selected = selectedSchool,
                onSelected = onSchoolChange,
                modifier = Modifier.weight(1f)
            )
            FilterDropdown(
                label = "Class",
                options = classes,
                selected = selectedClass,
                onSelected = onClassChange,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            spellsByLevel.toSortedMap().forEach { (level, levelSpells) ->
                val isCollapsed = collapsedStates[level] ?: true
                val isFiltering = searchQuery.isNotBlank() || selectedSchool != null || selectedClass != null

                item(key = "header_$level") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!isFiltering) collapsedStates[level] = !isCollapsed }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (level == 0) "Cantrips" else "Level $level",
                            style = MaterialTheme.typography.titleSmall,
                            color = DnDGold,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isFiltering) {
                            Text(if (isCollapsed) "▶" else "▼", color = DnDGold, fontSize = 12.sp)
                        }
                    }
                }

                if (!isCollapsed || isFiltering) {
                    items(levelSpells) { spell ->
                        var expanded by remember { mutableStateOf(false) }
                        DndCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(spell.name, style = MaterialTheme.typography.titleMedium, color = DnDLightText)
                                        Text("${spell.school} • ${spell.classes}", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                                    }
                                    val userSpell = learnedMap[spell.id]
                                    if (userSpell != null) {
                                        DndButton(
                                            text = "Unlearn", 
                                            onClick = { viewModel.forgetSpell(userSpell.id) }, 
                                            modifier = Modifier.weight(0.45f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                                        )
                                    } else {
                                        DndButton(
                                            text = "Learn", 
                                            onClick = { 
                                                if (spell.level > maxSlotLevel) {
                                                    onConfirmLearn(spell)
                                                } else {
                                                    viewModel.learnSpell(characterId, spell.id)
                                                }
                                            }, 
                                            modifier = Modifier.weight(0.45f)
                                        )
                                    }
                                    Button(onClick = { expanded = !expanded }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = DnDGold)) {
                                        Text(if (expanded) "▼" else "▶")
                                    }
                                }
                                if (expanded) {
                                    DndDivider()
                                    Text("Time: ${spell.castingTime} • Range: ${spell.range}", style = MaterialTheme.typography.labelSmall, color = DnDGold)
                                    Text("Duration: ${spell.duration} • Components: ${spell.components}", style = MaterialTheme.typography.labelSmall, color = DnDMutedText)
                                    if (spell.target.isNotEmpty()) {
                                        Text("Target: ${spell.target}", style = MaterialTheme.typography.labelSmall, color = DnDMutedText)
                                    }
                                    Text(spell.description, style = MaterialTheme.typography.bodySmall, color = DnDLightText)
                                    if (spell.higherLevel.isNotEmpty()) {
                                        Text("\nAt Higher Levels:", style = MaterialTheme.typography.labelSmall, color = DnDGold)
                                        Text(spell.higherLevel, style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                                    }
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
fun UpcastDialog(
    spell: UserSpellDisplayItem,
    currentSlots: String,
    onDismiss: () -> Unit,
    onCast: (Int) -> Unit
) {
    val slots = currentSlots.split(",").map { it.toIntOrNull() ?: 0 }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cast ${spell.name}", color = DnDGold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Requirements
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RequirementTag("V", spell.verbal)
                    RequirementTag("S", spell.somatic)
                    RequirementTag("M", spell.material.isNotEmpty())
                }
                
                Text("Range: ${spell.range}", color = DnDLightText, fontSize = 12.sp)
                if (spell.target.isNotEmpty()) {
                    Text("Target: ${spell.target}", color = DnDLightText, fontSize = 12.sp)
                }
                if (spell.damageRoll.isNotEmpty()) {
                    Text("Damage: ${spell.damageRoll}", color = Color(0xFFE57373), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text("Duration: ${spell.duration}", color = DnDLightText, fontSize = 12.sp)
                
                if (spell.higherLevel.isNotEmpty()) {
                    DndDivider()
                    Text("Higher Level:", color = DnDGold, style = MaterialTheme.typography.labelSmall)
                    Text(spell.higherLevel, color = DnDMutedText, style = MaterialTheme.typography.bodySmall)
                }
                
                DndDivider()
                Text("Select Level to Cast:", style = MaterialTheme.typography.labelSmall, color = DnDGold)
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    for (level in spell.level..9) {
                        val available = slots.getOrElse(level - 1) { 0 }
                        if (available > 0) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Level $level ($available slots)", color = DnDLightText)
                                    DndButton(
                                        text = "Cast",
                                        onClick = { onCast(level) },
                                        modifier = Modifier.heightIn(min = 24.dp).fillMaxWidth(0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DnDCardBg,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = DnDGold)
            }
        }
    )
}

@Composable
fun RequirementTag(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(if (active) DnDGold else DnDDarkBg, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (active) DnDDarkBg else DnDMutedText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun RestMenuDialog(
    currentSlots: String,
    maxSlots: String,
    onDismiss: () -> Unit,
    onFullRest: () -> Unit,
    onRecoverSlot: (Int) -> Unit
) {
    val currentList = currentSlots.split(",").map { it.toIntOrNull() ?: 0 }
    val maxList = maxSlots.split(",").map { it.toIntOrNull() ?: 0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rest Options", color = DnDGold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DndButton(text = "🏠 Full Rest (Restore All)", onClick = onFullRest)
                
                DndDivider()
                Text("Recover Individual Slots:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText)
                
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    for (i in 0 until 9) {
                        val max = maxList.getOrElse(i) { 0 }
                        if (max > 0) {
                            val current = currentList.getOrElse(i) { 0 }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Level ${i + 1} ($current/$max)", color = DnDLightText)
                                    DndButton(
                                        text = "+", 
                                        onClick = { onRecoverSlot(i + 1) },
                                        enabled = current < max,
                                        modifier = Modifier.heightIn(min = 24.dp).fillMaxWidth(0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DnDCardBg,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = DnDGold)
            }
        }
    )
}

@Composable
fun SpellSlotsDisplay(current: String, max: String) {
    val currentList = current.split(",").map { it.toIntOrNull() ?: 0 }
    val maxList = max.split(",").map { it.toIntOrNull() ?: 0 }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Spell Slots", style = MaterialTheme.typography.labelSmall, color = DnDMutedText)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until 9) {
                if (maxList.getOrElse(i) { 0 } > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("${i + 1}", fontSize = 10.sp, color = DnDGold)
                        Text("${currentList.getOrElse(i) { 0 }}/${maxList[i]}", fontSize = 10.sp, color = DnDLightText)
                    }
                }
            }
        }
    }
}

@Composable
fun DndTabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DnDGold else DnDDarkBg,
            contentColor = if (isSelected) DnDDarkBg else DnDGold
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
fun LearnedSpellsList(
    spells: List<UserSpellDisplayItem>, 
    viewModel: DndViewModel, 
    characterId: Long, 
    currentSlots: String,
    onError: (String) -> Unit
) {
    var spellToUpcast by remember { mutableStateOf<UserSpellDisplayItem?>(null) }

    if (spellToUpcast != null) {
        UpcastDialog(
            spell = spellToUpcast!!,
            currentSlots = currentSlots,
            onDismiss = { spellToUpcast = null },
            onCast = { level ->
                viewModel.castSpell(characterId, level)
                spellToUpcast = null
            }
        )
    }

    if (spells.isEmpty()) {
        Text("No spells learned yet. Go to 'All' to learn some!", color = DnDMutedText, modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(spells) { spell ->
                SpellRow(
                    spell = spell, 
                    onPrepareToggle = { viewModel.updateSpellPrepared(spell.id, it) },
                    onCast = { 
                        if (spell.level == 0) viewModel.castSpell(characterId, 0)
                        else {
                            val slots = currentSlots.split(",").map { it.toIntOrNull() ?: 0 }
                            val hasAnySlot = (spell.level..9).any { slots.getOrElse(it - 1) { 0 } > 0 }
                            if (hasAnySlot) {
                                spellToUpcast = spell
                            } else {
                                onError("No spell slots available of level ${spell.level} or higher!")
                            }
                        }
                    },
                    onForget = { viewModel.forgetSpell(spell.id) },
                    showUnlearn = true
                )
            }
        }
    }
}

@Composable
fun PreparedSpellsView(
    userSpells: List<UserSpellDisplayItem>, 
    viewModel: DndViewModel, 
    characterId: Long,
    currentSlots: String,
    loadoutName: String,
    onLoadoutNameChange: (String) -> Unit,
    onError: (String) -> Unit
) {
    val preparedSpells = userSpells.filter { it.isPrepared }
    val distinctLoadouts = userSpells.flatMap { it.loadouts.split(",").filter { s -> s.isNotBlank() } }.distinct()
    var spellToUpcast by remember { mutableStateOf<UserSpellDisplayItem?>(null) }

    if (spellToUpcast != null) {
        UpcastDialog(
            spell = spellToUpcast!!,
            currentSlots = currentSlots,
            onDismiss = { spellToUpcast = null },
            onCast = { level ->
                viewModel.castSpell(characterId, level)
                spellToUpcast = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Prepared Spells (${preparedSpells.size})", color = DnDGold, fontWeight = FontWeight.Bold)
        }
        
        if (preparedSpells.isEmpty()) {
            item {
                Text("No spells prepared.", color = DnDMutedText)
            }
        } else {
            items(preparedSpells) { spell ->
                SpellRow(
                    spell = spell, 
                    onPrepareToggle = { viewModel.updateSpellPrepared(spell.id, it) },
                    onCast = { 
                        if (spell.level == 0) viewModel.castSpell(characterId, 0)
                        else {
                            val slots = currentSlots.split(",").map { it.toIntOrNull() ?: 0 }
                            val hasAnySlot = (spell.level..9).any { slots.getOrElse(it - 1) { 0 } > 0 }
                            if (hasAnySlot) {
                                spellToUpcast = spell
                            } else {
                                onError("No spell slots available of level ${spell.level} or higher!")
                            }
                        }
                    },
                    onForget = { viewModel.forgetSpell(spell.id) },
                    showUnlearn = false
                )
            }
        }

        item {
            DndDivider()
        }
        
        item {
            DndCard(modifier = Modifier.fillMaxWidth()) {
                DndCardHeader("Loadouts")
                DndTextInput(value = loadoutName, onValueChange = onLoadoutNameChange, label = "New Loadout Name")
                DndButton(
                    text = "💾 Save Current Loadout", 
                    onClick = {
                        if (loadoutName.isNotBlank()) {
                            preparedSpells.forEach { spell ->
                                val list = spell.loadouts.split(",").filter { it.isNotBlank() }.toMutableList()
                                if (!list.contains(loadoutName)) {
                                    list.add(loadoutName)
                                    viewModel.updateSpellLoadouts(spell.id, list.joinToString(","))
                                }
                            }
                            onLoadoutNameChange("")
                        }
                    },
                    enabled = preparedSpells.isNotEmpty() && loadoutName.isNotBlank()
                )

                if (distinctLoadouts.isNotEmpty()) {
                    Text("Apply Loadout:", style = MaterialTheme.typography.labelSmall, color = DnDMutedText, modifier = Modifier.padding(top = 8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        distinctLoadouts.forEach { name ->
                            DndButton(text = "Apply: $name", onClick = { viewModel.applyLoadout(characterId, name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpellRow(
    spell: UserSpellDisplayItem, 
    onPrepareToggle: (Boolean) -> Unit, 
    onCast: () -> Unit,
    onForget: () -> Unit,
    showUnlearn: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    DndCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spell.name, style = MaterialTheme.typography.titleMedium, color = DnDLightText, fontWeight = FontWeight.Bold)
                    Text("Level ${spell.level} • ${spell.school}", style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                }
                
                Checkbox(checked = spell.isPrepared, onCheckedChange = onPrepareToggle, colors = CheckboxDefaults.colors(checkedColor = DnDGold))
                Button(onClick = { expanded = !expanded }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = DnDGold)) {
                    Text(if (expanded) "▼" else "▶")
                }
            }
            if (expanded) {
                DndDivider()
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RequirementTag("V", spell.verbal)
                    RequirementTag("S", spell.somatic)
                    RequirementTag("M", spell.material.isNotEmpty())
                    
                    if (spell.damageRoll.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8B0000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(spell.damageRoll, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }

                Text("Time: ${spell.castingTime} • Range: ${spell.range}", style = MaterialTheme.typography.labelSmall, color = DnDGold)
                Text("Duration: ${spell.duration}", style = MaterialTheme.typography.labelSmall, color = DnDMutedText)
                if (spell.target.isNotEmpty()) {
                    Text("Target: ${spell.target}", style = MaterialTheme.typography.labelSmall, color = DnDMutedText)
                }
                
                Text(spell.description, style = MaterialTheme.typography.bodySmall, color = DnDLightText)
                
                if (spell.higherLevel.isNotEmpty()) {
                    Text("\nAt Higher Levels:", style = MaterialTheme.typography.labelSmall, color = DnDGold)
                    Text(spell.higherLevel, style = MaterialTheme.typography.bodySmall, color = DnDMutedText)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DndButton(
                        text = if (spell.level == 0) "🪄 Cast" else "🔥 Cast", 
                        onClick = onCast,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (spell.level == 0) DnDGold else Color(0xFF8B0000), 
                            contentColor = if (spell.level == 0) DnDDarkBg else Color.White
                        )
                    )
                    if (showUnlearn) {
                        DndButton(
                            text = "🗑 Unlearn", 
                            onClick = onForget, 
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DndButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors(containerColor = DnDGold, contentColor = DnDDarkBg)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}
