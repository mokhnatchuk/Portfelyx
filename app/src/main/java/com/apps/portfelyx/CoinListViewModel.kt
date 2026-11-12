package com.apps.portfelyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

data class CoinListUiState(
    val selectedFilter: String = "Всі",
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String = "",
    val loadedCurrency: String = ""
)

class CoinListViewModel : ViewModel() {
    var uiState by mutableStateOf(CoinListUiState())
        private set

    fun selectFilter(newFilter: String) {
        uiState = uiState.copy(selectedFilter = newFilter)
    }

    fun refreshCoins(currency: String) {
        loadCoins(currency = currency, forceReload = true, isPullRefresh = true)
    }

    fun loadCoins(currency: String, forceReload: Boolean = false, isPullRefresh: Boolean = false) {
        if (!forceReload && uiState.coins.isNotEmpty() && uiState.loadedCurrency == currency) {
            return
        }

        viewModelScope.launch {
            if (isPullRefresh) {
                uiState = uiState.copy(isRefreshing = true, errorMessage = "")
            } else {
                uiState = uiState.copy(isLoading = true, errorMessage = "")
            }

            try {
                val responseList = coinGeckoApi.getCoins(BuildConfig.COINGECKO_API_KEY, currency)
                val loadedCoins = ArrayList<Coin>(responseList.size)
                val seenCoinIds = HashSet<String>(responseList.size)

                for (coinResponse in responseList) {
                    if (seenCoinIds.add(coinResponse.id)) {
                        loadedCoins.add(coinResponse.toCoin())
                    }
                }

                val maxConcurrentRequests = 4
                var requestCount = loadedCoins.size
                if (requestCount > maxConcurrentRequests) {
                    requestCount = maxConcurrentRequests
                }

                coroutineScope {
                    val deferredDetails = ArrayList<Deferred<CoinDetailResponse?>>(requestCount)
                    for (index in 0 until requestCount) {
                        val coinId = loadedCoins[index].id
                        val deferred = async {
                            try {
                                coinGeckoApi.getCoinDetail(BuildConfig.COINGECKO_API_KEY, coinId)
                            } catch (detailError: Exception) {
                                null
                            }
                        }
                        deferredDetails.add(deferred)
                    }

                    for (index in 0 until requestCount) {
                        val detail = deferredDetails[index].await()
                        if (detail != null) {
                            val currentCoin = loadedCoins[index]
                            loadedCoins[index] = currentCoin.copy(
                                price_change_percentage_7d = detail.market_data?.price_change_percentage_7d,
                                genesis_date = detail.genesis_date ?: "",
                                hashing_algorithm = detail.hashing_algorithm ?: ""
                            )
                        }
                    }
                }

                uiState = uiState.copy(
                    coins = loadedCoins,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "",
                    loadedCurrency = currency
                )
            } catch (serializationError: SerializationException) {
                uiState = uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Помилка читання даних: відповідь сервера має неправильний формат."
                )
            } catch (httpError: HttpException) {
                val statusCode = httpError.code()
                uiState = uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Сервер повернув помилку (код $statusCode). Спробуйте пізніше."
                )
            } catch (networkError: IOException) {
                uiState = uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Немає підключення до інтернету. Перевірте мережу."
                )
            } catch (unknownError: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Сталася невідома помилка під час завантаження."
                )
            }
        }
    }
}