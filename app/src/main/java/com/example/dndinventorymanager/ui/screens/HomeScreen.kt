package com.example.dndinventorymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndinventorymanager.ui.components.DndCard
import com.example.dndinventorymanager.ui.components.DndCardHeader
import com.example.dndinventorymanager.ui.components.DndDivider
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText
import com.example.dndinventorymanager.ui.theme.DnDMutedText

@Composable
fun HomeScreen(
    onNavigateCharacters: () -> Unit,
    onNavigateInventory: () -> Unit,
    onNavigateAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Main Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "⚔ DUNGEONS & DRAGONS ⚔",
                style = MaterialTheme.typography.labelMedium,
                color = DnDGold,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Inventory",
                style = MaterialTheme.typography.displayMedium,
                color = DnDGold,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Manager",
                style = MaterialTheme.typography.displaySmall,
                color = DnDGold,
                fontWeight = FontWeight.Light,
                modifier = Modifier.offset(y = (-8).dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp, 2.dp)
                    .background(DnDGold)
            )
        }

        // Quick Navigation Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Quick Navigation",
                style = MaterialTheme.typography.titleMedium,
                color = DnDLightText,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NavigationHex(
                    title = "Characters",
                    icon = "⚔",
                    onClick = onNavigateCharacters,
                    modifier = Modifier.weight(1f)
                )
                NavigationHex(
                    title = "Inventory",
                    icon = "👜",
                    onClick = onNavigateInventory,
                    modifier = Modifier.weight(1f)
                )
                NavigationHex(
                    title = "Admin",
                    icon = "⚙",
                    onClick = onNavigateAdmin,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Features/Status Card
        DndCard(modifier = Modifier.fillMaxWidth()) {
            DndCardHeader(
                "Adventurer's Toolkit",
                "Your companion for item tracking"
            )
            DndDivider()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureRow("🛡", "Character & Level tracking")
                FeatureRow("📦", "Container-based organization")
                FeatureRow("✨", "Magic item & rarity support")
                FeatureRow("📖", "Full 5e SRD integration")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Powered by 5th Edition SRD API",
                style = MaterialTheme.typography.labelSmall,
                color = DnDMutedText
            )
            Text(
                "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = DnDMutedText
            )
        }
    }
}

@Composable
fun NavigationHex(
    title: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DndCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = DnDGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FeatureRow(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DnDGold.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 12.sp)
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = DnDLightText
        )
    }
}