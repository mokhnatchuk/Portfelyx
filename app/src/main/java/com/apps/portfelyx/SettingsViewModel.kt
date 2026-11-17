package com.apps.portfelyx

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.settingsDataStore

    val currency = dataStore.data
        .map { preferences -> preferences[CURRENCY_KEY] ?: "usd" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "usd")

    val favoriteIds = dataStore.data
        .map { preferences -> preferences[FAVORITE_COINS_KEY] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setCurrency(newCurrency: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[CURRENCY_KEY] = newCurrency
            }
        }
    }

    fun toggleFavorite(coinId: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentSet = preferences[FAVORITE_COINS_KEY] ?: emptySet()
                val updatedSet = HashSet<String>(currentSet)
                if (updatedSet.contains(coinId)) {
                    updatedSet.remove(coinId)
                } else {
                    updatedSet.add(coinId)
                }
                preferences[FAVORITE_COINS_KEY] = updatedSet
            }
        }
    }
}