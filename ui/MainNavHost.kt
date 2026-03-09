package com.example.dndinventorymanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dndinventorymanager.ui.screens.*
import com.example.dndinventorymanager.ui.theme.DnDGold
import com.example.dndinventorymanager.ui.theme.DnDLightText

@Composable
fun MainNavHost(
    navController: NavHostController,
    viewModel: DndViewModel,
    modifier: Modifier = Modifier
) {
    val syncStatus by viewModel.syncStatus.collectAsState()

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateCharacters = { navController.navigate("characters") },
                    onNavigateInventory = { navController.navigate("inventory") },
                    onNavigateSpells = { navController.navigate("spells") },
                    onNavigateAdmin = { navController.navigate("admin") }
                )
            }
            composable("characters") {
                CharactersScreen(viewModel = viewModel)
            }
            composable("inventory") {
                InventoryScreen(viewModel = viewModel)
            }
            composable("spells") {
                SpellsScreen(viewModel = viewModel)
            }
            composable("admin") {
                AdminScreen(
                    viewModel = viewModel,
                    onNavigateSpellsAdmin = { navController.navigate("admin_spells") }
                )
            }
            composable("admin_spells") {
                AdminSpellsScreen(
                    viewModel = viewModel,
                    onNavigateItemsAdmin = { navController.navigate("admin") }
                )
            }
        }

        if (syncStatus.isSyncing) {
            SyncOverlay(syncStatus.message, syncStatus.progress, syncStatus.total)
        }
    }
}

@Composable
fun SyncOverlay(message: String, progress: Int, total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.material3.Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A).copy(alpha = 0.9f)
            ),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(8.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(message, color = DnDGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (total > 0) {
                    LinearProgressIndicator(
                        progress = progress.toFloat() / total.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        color = DnDGold,
                        trackColor = Color.Gray
                    )
                    Text("$progress / $total", color = DnDLightText, fontSize = 10.sp)
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = DnDGold
                    )
                }
            }
        }
    }
}
