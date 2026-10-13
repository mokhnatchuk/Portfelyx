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

    fun setCurrency(newCurrency: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[CURRENCY_KEY] = newCurrency
            }
        }
    }
}