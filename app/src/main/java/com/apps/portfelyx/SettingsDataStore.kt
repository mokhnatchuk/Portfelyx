package com.apps.portfelyx

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")
val CURRENCY_KEY = stringPreferencesKey("currency")
val FAVORITE_COINS_KEY = stringSetPreferencesKey("favorite_coins")