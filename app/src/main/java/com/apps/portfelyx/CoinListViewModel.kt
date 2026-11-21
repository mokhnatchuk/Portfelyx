package com.apps.portfelyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

data class CoinListUiState(
    val selectedFilter: String = "Всі",
    val searchQuery: String = "",
    val coins: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String = "",
    val loadedCurrency: String = "",
    val loadedQuery: String = "",
    val favoriteCoins: List<Coin> = emptyList(),
    val loadedFavoritesCurrency: String = "",
    val isFavoritesLoading: Boolean = false,
    val favoritesErrorMessage: String = ""
)

class CoinListViewModel : ViewModel() {
    var uiState by mutableStateOf(CoinListUiState())
        private set

    private var searchJob: Job? = null
    private var catalogJob: Job? = null
    private var favoritesJob: Job? = null
    private var savedCatalogCoins: List<Coin> = emptyList()
    private var savedCatalogCurrency: String = ""
    private var loadedFavoriteIds: Set<String> = emptySet()

    fun selectFilter(newFilter: String) {
        uiState = uiState.copy(selectedFilter = newFilter)
    }

    fun updateSearchQuery(newQuery: String, currency: String) {
        uiState = uiState.copy(searchQuery = newQuery)
        searchJob?.cancel()

        val trimmedQuery = newQuery.trim()
        if (trimmedQuery.isEmpty()) {
            if (savedCatalogCoins.isNotEmpty() && savedCatalogCurrency == currency) {
                catalogJob?.cancel()
                uiState = uiState.copy(
                    coins = savedCatalogCoins,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "",
                    loadedCurrency = savedCatalogCurrency,
                    loadedQuery = ""
                )
            } else {
                loadCoins(currency = currency, forceReload = true)
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            loadCoins(currency = currency, forceReload = true, cancelPendingSearch = false)
        }
    }

    fun clearSearch(currency: String) {
        updateSearchQuery("", currency)
    }

    fun refreshCoins(currency: String) {
        loadCoins(currency = currency, forceReload = true, isPullRefresh = true)
    }

    fun loadFavoriteCoins(
        favoriteIds: Set<String>,
        currency: String,
        forceReload: Boolean = false
    ) {
        if (favoriteIds.isEmpty()) {
            favoritesJob?.cancel()
            loadedFavoriteIds = emptySet()
            uiState = uiState.copy(
                favoriteCoins = emptyList(),
                loadedFavoritesCurrency = currency,
                isFavoritesLoading = false,
                favoritesErrorMessage = ""
            )
            return
        }

        if (!forceReload &&
            uiState.loadedFavoritesCurrency == currency &&
            uiState.favoritesErrorMessage.isEmpty() &&
            uiState.favoriteCoins.isNotEmpty()
        ) {
            if (loadedFavoriteIds == favoriteIds) {
                return
            }
            if (loadedFavoriteIds.containsAll(favoriteIds)) {
                favoritesJob?.cancel()
                val remainingCoins = ArrayList<Coin>(favoriteIds.size)
                for (coinItem in uiState.favoriteCoins) {
                    if (favoriteIds.contains(coinItem.id)) {
                        remainingCoins.add(coinItem)
                    }
                }
                loadedFavoriteIds = HashSet<String>(favoriteIds)
                uiState = uiState.copy(
                    favoriteCoins = remainingCoins,
                    isFavoritesLoading = false,
                    favoritesErrorMessage = ""
                )
                return
            }
        }

        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            uiState = uiState.copy(isFavoritesLoading = true, favoritesErrorMessage = "")
            try {
                val idsBuilder = StringBuilder()
                for (coinId in favoriteIds) {
                    if (coinId.isNotEmpty()) {
                        if (idsBuilder.isNotEmpty()) {
                            idsBuilder.append(",")
                        }
                        idsBuilder.append(coinId)
                    }
                }

                val idsString = idsBuilder.toString()
                val loadedFavorites = ArrayList<Coin>()

                if (idsString.isNotEmpty()) {
                    val responseList = coinGeckoApi.getCoins(
                        apiKey = BuildConfig.COINGECKO_API_KEY,
                        currency = currency,
                        ids = idsString
                    )
                    val seenCoinIds = HashSet<String>(responseList.size)
                    for (coinResponse in responseList) {
                        if (coinResponse.id.isNotEmpty() && seenCoinIds.add(coinResponse.id)) {
                            loadedFavorites.add(coinResponse.toCoin())
                        }
                    }
                }

                loadedFavoriteIds = HashSet<String>(favoriteIds)
                uiState = uiState.copy(
                    favoriteCoins = loadedFavorites,
                    loadedFavoritesCurrency = currency,
                    isFavoritesLoading = false,
                    favoritesErrorMessage = ""
                )
            } catch (cancelledError: CancellationException) {
                throw cancelledError
            } catch (serializationError: SerializationException) {
                uiState = uiState.copy(
                    isFavoritesLoading = false,
                    favoritesErrorMessage = "Помилка читання даних: відповідь сервера має неправильний формат."
                )
            } catch (httpError: HttpException) {
                val statusCode = httpError.code()
                uiState = uiState.copy(
                    isFavoritesLoading = false,
                    favoritesErrorMessage = "Сервер повернув помилку (код $statusCode). Спробуйте пізніше."
                )
            } catch (networkError: IOException) {
                uiState = uiState.copy(
                    isFavoritesLoading = false,
                    favoritesErrorMessage = "Немає підключення до інтернету. Перевірте мережу."
                )
            } catch (unknownError: Exception) {
                uiState = uiState.copy(
                    isFavoritesLoading = false,
                    favoritesErrorMessage = "Сталася невідома помилка під час завантаження добірки."
                )
            }
        }
    }

    fun loadCoins(
        currency: String,
        forceReload: Boolean = false,
        isPullRefresh: Boolean = false,
        cancelPendingSearch: Boolean = true
    ) {
        val trimmedQuery = uiState.searchQuery.trim()

        if (!forceReload &&
            uiState.loadedCurrency == currency &&
            uiState.loadedQuery == trimmedQuery &&
            uiState.errorMessage.isEmpty()
        ) {
            return
        }

        if (cancelPendingSearch) {
            searchJob?.cancel()
        }
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            if (isPullRefresh) {
                uiState = uiState.copy(isLoading = false, isRefreshing = true, errorMessage = "")
            } else {
                uiState = uiState.copy(isLoading = true, isRefreshing = false, errorMessage = "")
            }

            try {
                val loadedCoins = ArrayList<Coin>()

                if (trimmedQuery.isEmpty()) {
                    val responseList = coinGeckoApi.getCoins(
                        apiKey = BuildConfig.COINGECKO_API_KEY,
                        currency = currency
                    )
                    val seenCoinIds = HashSet<String>(responseList.size)

                    for (coinResponse in responseList) {
                        if (coinResponse.id.isNotEmpty() && seenCoinIds.add(coinResponse.id)) {
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
                        for (coinIndex in 0 until requestCount) {
                            val coinId = loadedCoins[coinIndex].id
                            val deferred = async {
                                try {
                                    coinGeckoApi.getCoinDetail(BuildConfig.COINGECKO_API_KEY, coinId)
                                } catch (cancelledError: CancellationException) {
                                    throw cancelledError
                                } catch (detailError: Exception) {
                                    null
                                }
                            }
                            deferredDetails.add(deferred)
                        }

                        for (coinIndex in 0 until requestCount) {
                            val detail = deferredDetails[coinIndex].await()
                            if (detail != null) {
                                val currentCoin = loadedCoins[coinIndex]
                                loadedCoins[coinIndex] = currentCoin.copy(
                                    price_change_percentage_7d = detail.market_data?.price_change_percentage_7d,
                                    genesis_date = detail.genesis_date ?: "",
                                    hashing_algorithm = detail.hashing_algorithm ?: ""
                                )
                            }
                        }
                    }

                    savedCatalogCoins = loadedCoins
                    savedCatalogCurrency = currency
                } else {
                    val searchResponse = coinGeckoApi.searchCoins(
                        apiKey = BuildConfig.COINGECKO_API_KEY,
                        query = trimmedQuery
                    )
                    val searchItems = searchResponse.coins
                    var maxSearchCount = searchItems.size
                    if (maxSearchCount > 20) {
                        maxSearchCount = 20
                    }

                    if (maxSearchCount > 0) {
                        val idsBuilder = StringBuilder()
                        for (searchIndex in 0 until maxSearchCount) {
                            val coinId = searchItems[searchIndex].id
                            if (coinId.isNotEmpty()) {
                                if (idsBuilder.isNotEmpty()) {
                                    idsBuilder.append(",")
                                }
                                idsBuilder.append(coinId)
                            }
                        }

                        val idsString = idsBuilder.toString()
                        if (idsString.isNotEmpty()) {
                            val responseList = coinGeckoApi.getCoins(
                                apiKey = BuildConfig.COINGECKO_API_KEY,
                                currency = currency,
                                ids = idsString
                            )
                            val seenCoinIds = HashSet<String>(responseList.size)
                            for (coinResponse in responseList) {
                                if (coinResponse.id.isNotEmpty() && seenCoinIds.add(coinResponse.id)) {
                                    loadedCoins.add(coinResponse.toCoin())
                                }
                            }
                        }
                    }
                }

                uiState = uiState.copy(
                    coins = loadedCoins,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "",
                    loadedCurrency = currency,
                    loadedQuery = trimmedQuery
                )
            } catch (cancelledError: CancellationException) {
                throw cancelledError
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
