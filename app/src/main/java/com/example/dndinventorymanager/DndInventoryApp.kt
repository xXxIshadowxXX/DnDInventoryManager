package com.example.dndinventorymanager

import android.app.Application
import com.example.dndinventorymanager.data.AppDatabase
import com.example.dndinventorymanager.data.DndRepository
import com.example.dndinventorymanager.data.SettingsDataStore
import com.example.dndinventorymanager.data.network.DndApiClient

class DndInventoryApp : Application() {
    lateinit var repository: DndRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        val settingsDataStore = SettingsDataStore(this)
        repository = DndRepository(
            characterDao = database.characterDao(),
            itemDao = database.itemDao(),
            customItemDao = database.customItemDao(),
            inventoryDao = database.inventoryDao(),
            settingsDataStore = settingsDataStore,
            api = DndApiClient.api
        )
    }
}
