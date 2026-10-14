package com.apps.portfelyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class CoinListUiState(
    val selectedFilter: String = "Всі",
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: Boolean = false
)

class CoinListViewModel : ViewModel() {
    var uiState by mutableStateOf(CoinListUiState())
        private set

    fun selectFilter(newFilter: String) {
        uiState = uiState.copy(selectedFilter = newFilter)
    }

    fun loadCoins(currency: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, loadError = false)
            try {
                val coins = coinGeckoApi
                    .getCoins(BuildConfig.COINGECKO_API_KEY, currency)
                    .map { it.toCoin() }
                uiState = uiState.copy(coins = coins, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, loadError = true)
            }
        }
    }
}