package com.apps.portfelyx

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")
val CURRENCY_KEY = stringPreferencesKey("currency")
