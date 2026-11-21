package com.apps.portfelyx

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.settingsDataStore

    private val currencyState = MutableStateFlow<String?>(null)
    val currency: StateFlow<String?> = currencyState

    private val favoriteIdsState = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = favoriteIdsState

    init {
        viewModelScope.launch {
            try {
                dataStore.data.collect { preferences ->
                    currencyState.value = preferences[CURRENCY_KEY] ?: "usd"
                    favoriteIdsState.value = preferences[FAVORITE_COINS_KEY] ?: emptySet()
                }
            } catch (cancelledError: CancellationException) {
                throw cancelledError
            } catch (storageError: IOException) {
                if (currencyState.value == null) {
                    currencyState.value = "usd"
                }
            }
        }
    }

    fun setCurrency(newCurrency: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[CURRENCY_KEY] = newCurrency
                }
            } catch (cancelledError: CancellationException) {
                throw cancelledError
            } catch (storageError: IOException) {
                currencyState.value = newCurrency
            }
        }
    }

    fun toggleFavorite(coinId: String) {
        viewModelScope.launch {
            try {
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
            } catch (cancelledError: CancellationException) {
                throw cancelledError
            } catch (storageError: IOException) {
                val updatedSet = HashSet<String>(favoriteIdsState.value)
                if (updatedSet.contains(coinId)) {
                    updatedSet.remove(coinId)
                } else {
                    updatedSet.add(coinId)
                }
                favoriteIdsState.value = updatedSet
            }
        }
    }
}
