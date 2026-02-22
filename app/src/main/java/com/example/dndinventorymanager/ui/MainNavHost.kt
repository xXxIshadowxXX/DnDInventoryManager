package com.example.dndinventorymanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dndinventorymanager.ui.navigation.NavRoutes
import com.example.dndinventorymanager.ui.screens.AdminScreen
import com.example.dndinventorymanager.ui.screens.CharactersScreen
import com.example.dndinventorymanager.ui.screens.HomeScreen
import com.example.dndinventorymanager.ui.screens.InventoryScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    viewModel: DndViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateCharacters = { navController.navigate(NavRoutes.CHARACTERS) },
                onNavigateInventory = { navController.navigate(NavRoutes.INVENTORY) },
                onNavigateAdmin = { navController.navigate(NavRoutes.ADMIN) }
            )
        }
        composable(NavRoutes.CHARACTERS) {
            CharactersScreen(
                viewModel = viewModel,
                onNavigateInventory = { navController.navigate(NavRoutes.INVENTORY) }
            )
        }
        composable(NavRoutes.INVENTORY) {
            InventoryScreen(
                viewModel = viewModel,
                onNavigateCharacters = { navController.navigate(NavRoutes.CHARACTERS) }
            )
        }
        composable(NavRoutes.ADMIN) {
            AdminScreen(viewModel = viewModel)
        }
    }
}
