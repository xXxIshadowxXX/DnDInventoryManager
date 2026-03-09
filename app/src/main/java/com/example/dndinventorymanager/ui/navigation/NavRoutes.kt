package com.example.dndinventorymanager.ui.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Characters : NavRoutes("characters")
    object Inventory : NavRoutes("inventory")
    object Spells : NavRoutes("spells")
    object Admin : NavRoutes("admin")
}
