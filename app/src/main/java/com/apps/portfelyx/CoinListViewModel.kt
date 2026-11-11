package com.apps.portfelyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

data class CoinListUiState(
    val selectedFilter: String = "Всі",
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

class CoinListViewModel : ViewModel() {
    var uiState by mutableStateOf(CoinListUiState())
        private set

    fun selectFilter(newFilter: String) {
        uiState = uiState.copy(selectedFilter = newFilter)
    }

    fun loadCoins(currency: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = "")
            try {
                val responseList = coinGeckoApi.getCoins(BuildConfig.COINGECKO_API_KEY, currency)
                val loadedCoins = ArrayList<Coin>(responseList.size)
                for (coinResponse in responseList) {
                    loadedCoins.add(coinResponse.toCoin())
                }
                uiState = uiState.copy(
                    coins = loadedCoins,
                    isLoading = false,
                    errorMessage = ""
                )
            } catch (serializationError: SerializationException) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Помилка читання даних: відповідь сервера має неправильний формат."
                )
            } catch (httpError: HttpException) {
                val statusCode = httpError.code()
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Сервер повернув помилку (код $statusCode). Спробуйте пізніше."
                )
            } catch (networkError: IOException) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Немає підключення до інтернету. Перевірте мережу."
                )
            } catch (unknownError: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Сталася невідома помилка під час завантаження."
                )
            }
        }
    }
}