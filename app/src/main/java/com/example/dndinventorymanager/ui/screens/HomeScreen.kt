package com.example.dndinventorymanager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndinventorymanager.R
import com.example.dndinventorymanager.ui.components.DndCard
import com.example.dndinventorymanager.ui.components.DndDivider
import com.example.dndinventorymanager.ui.theme.DnDCardBg
import com.example.dndinventorymanager.ui.theme.DnDDarkBg
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText
import com.example.dndinventorymanager.ui.theme.DnDMutedText

@Composable
fun HomeScreen(
    onNavigateCharacters: () -> Unit,
    onNavigateInventory: () -> Unit,
    onNavigateSpells: () -> Unit,
    onNavigateAdmin: () -> Unit
) {
    var isMusicPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DnDDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Bar with Music Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { isMusicPlaying = !isMusicPlaying },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DnDCardBg)
                    .border(1.dp, DnDGold, CircleShape)
            ) {
                Text(
                    text = if (isMusicPlaying) "🎵" else "🔇",
                    fontSize = 20.sp
                )
            }
        }

        // Thematic Header Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DnDGold.copy(alpha = 0.3f), DnDDarkBg)
                    )
                )
                .border(2.dp, DnDGold, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔", fontSize = 60.sp)
                Text(
                    "ADVENTURE AWAITS",
                    color = DnDGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    fontSize = 14.sp
                )
            }
            // Note: Replace this Box content with a real Image when you have one:
            // Image(painter = painterResource(id = R.drawable.your_dnd_art), contentDescription = null)
        }

        // Title Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "DUNGEONS & DRAGONS",
                style = MaterialTheme.typography.labelLarge,
                color = DnDGold,
                letterSpacing = 2.sp
            )
            Text(
                "Inventory Manager",
                style = MaterialTheme.typography.headlineLarge,
                color = DnDLightText,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(100.dp, 2.dp)
                    .background(DnDGold)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid-like Navigation
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuButton(
                    title = "Characters",
                    icon = "🛡️",
                    subtitle = "Manage Heroes",
                    onClick = onNavigateCharacters,
                    modifier = Modifier.weight(1f)
                )
                MenuButton(
                    title = "Inventory",
                    icon = "📦",
                    subtitle = "Loot & Gear",
                    onClick = onNavigateInventory,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuButton(
                    title = "Spellbook",
                    icon = "🪄",
                    subtitle = "Magic & Rites",
                    onClick = onNavigateSpells,
                    modifier = Modifier.weight(1f)
                )
                MenuButton(
                    title = "Settings",
                    icon = "⚙️",
                    subtitle = "App Options",
                    onClick = onNavigateAdmin,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Music Player UI (if playing)
        if (isMusicPlaying) {
            DndCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("📻", fontSize = 24.sp)
                    Column {
                        Text("Bardic Inspiration", color = DnDGold, fontWeight = FontWeight.Bold)
                        Text("Tavern Ambience - Loop", color = DnDMutedText, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Simplified Footer
        Text(
            "Version 1.2.0 • 2024",
            style = MaterialTheme.typography.labelSmall,
            color = DnDMutedText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
fun MenuButton(
    title: String,
    icon: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DnDCardBg)
            .border(1.dp, DnDGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                color = DnDGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                subtitle,
                color = DnDMutedText,
                fontSize = 11.sp
            )
        }
    }
}
