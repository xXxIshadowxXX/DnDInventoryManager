package com.example.dndinventorymanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndinventorymanager.ui.theme.DnDCardBg
import com.example.dndinventorymanager.ui.theme.DnDDarkBg
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText
import com.example.dndinventorymanager.ui.theme.DnDMutedText

/**
 * D&D themed card with gold border and dark background
 */
@Composable
fun DndCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.5.dp, DnDGold)
            .background(DnDCardBg),
        colors = CardDefaults.cardColors(containerColor = DnDCardBg),
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    )
}

/**
 * D&D themed card header with gold text
 */
@Composable
fun DndCardHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = DnDGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = DnDMutedText
            )
        }
    }
}

/**
 * D&D themed button with gold styling
 */
@Composable
fun DndButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = DnDGold,
        contentColor = DnDDarkBg,
        disabledContainerColor = DnDMutedText,
        disabledContentColor = DnDDarkBg
    )
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
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Section divider with gold highlighting
 */
@Composable
fun DndDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 8.dp),
        color = DnDGold,
        thickness = 2.dp
    )
}

/**
 * Item card for inventory items with rarity coloring
 */
@Composable
fun InventoryItemCard(
    name: String,
    rarity: String,
    sourcebook: String,
    description: String,
    quantity: Int,
    equipped: Boolean,
    weight: String = "",
    value: String = "",
    category: String = "",
    type: String = "",
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val rarityColor = when (rarity.lowercase()) {
        "common" -> Color(0xFF888888)
        "uncommon" -> Color(0xFF4CAF50)
        "rare" -> Color(0xFF2196F3)
        "very rare" -> Color(0xFF9C27B0)
        "legendary" -> Color(0xFFFF9800)
        "artifact" -> Color(0xFFFFB300)
        else -> DnDGold
    }

    DndCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    color = DnDLightText,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        rarity,
                        style = MaterialTheme.typography.bodySmall,
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDMutedText
                    )
                    Text(
                        sourcebook,
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDMutedText
                    )
                }
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    "Qty: $quantity",
                    style = MaterialTheme.typography.titleMedium,
                    color = DnDGold,
                    fontWeight = FontWeight.Bold
                )
                if (equipped) {
                    Text(
                        "⚔ Equipped",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        DndDivider()

        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = DnDLightText
        )

        // Item Properties
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            if (category.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Category:",
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDMutedText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        category,
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDLightText
                    )
                }
            }
            if (type.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Type:",
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDMutedText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        type,
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDLightText
                    )
                }
            }
            if (weight.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Weight:",
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDMutedText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        weight,
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDLightText
                    )
                }
            }
            if (value.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Value:",
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDMutedText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall,
                        color = DnDGold
                    )
                }
            }
        }

        content()
    }
}

/**
 * Character card with stats
 */
@Composable
fun CharacterCard(
    name: String,
    clazz: String,
    level: Int,
    gold: Int,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    DndCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DnDGold,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column {
                        Text(
                            "Class",
                            style = MaterialTheme.typography.labelSmall,
                            color = DnDMutedText
                        )
                        Text(
                            clazz,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DnDLightText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            "Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = DnDMutedText
                        )
                        Text(
                            "$level",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DnDLightText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            "Gold",
                            style = MaterialTheme.typography.labelSmall,
                            color = DnDMutedText
                        )
                        Text(
                            gold.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DnDGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isActive) {
                Text(
                    "★",
                    fontSize = 28.sp,
                    color = DnDGold
                )
            }
        }
        content()
    }
}

@Composable
fun DndTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = DnDMutedText) },
        placeholder = { Text(placeholder, color = DnDMutedText) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DnDGold,
            unfocusedBorderColor = DnDMutedText,
            focusedTextColor = DnDLightText,
            unfocusedTextColor = DnDLightText,
            cursorColor = DnDGold
        )
    )
}
