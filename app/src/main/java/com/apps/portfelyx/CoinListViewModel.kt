package com.apps.portfelyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class CoinListUiState(
    val selectedFilter: String = "Всі"
)

class CoinListViewModel : ViewModel() {
    var uiState by mutableStateOf(CoinListUiState())
        private set

    fun selectFilter(newFilter: String) {
        uiState = uiState.copy(selectedFilter = newFilter)
    }
}