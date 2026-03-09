package com.example.dndinventorymanager.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("dnd_settings")

class SettingsDataStore(private val context: Context) {
    private val activeCharacterKey = longPreferencesKey("active_character_id")
    private val initialSyncKey = booleanPreferencesKey("initial_sync_done")

    val activeCharacterIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[activeCharacterKey]
    }

    val initialSyncDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[initialSyncKey] ?: false
    }

    suspend fun setActiveCharacterId(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(activeCharacterKey)
            } else {
                prefs[activeCharacterKey] = id
            }
        }
    }

    suspend fun setInitialSyncDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[initialSyncKey] = done
        }
    }
}
