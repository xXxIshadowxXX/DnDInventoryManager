package com.example.dndinventorymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.dndinventorymanager.ui.DndViewModel
import com.example.dndinventorymanager.ui.DndViewModelFactory
import com.example.dndinventorymanager.ui.MainNavHost
import com.example.dndinventorymanager.ui.theme.DnDInventoryManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DnDInventoryManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppRoot(modifier = Modifier.padding(it))
                }
            }
        }
    }
}

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as DndInventoryApp
    val viewModel: DndViewModel = viewModel(factory = DndViewModelFactory(app.repository))
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.performInitialSync()
    }

    MainNavHost(navController = navController, viewModel = viewModel, modifier = modifier)
}