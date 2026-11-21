package com.apps.portfelyx

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class CoinResponse(
    val id: String = "",
    val symbol: String = "",
    val name: String = "",
    val image: String? = null,
    val current_price: Double? = null,
    val price_change_percentage_24h: Double? = null,
    val market_cap: Double? = null,
    val total_volume: Double? = null,
    val high_24h: Double? = null,
    val low_24h: Double? = null
)

@Serializable
data class MarketDataResponse(
    val price_change_percentage_7d: Double? = null
)

@Serializable
data class CoinDetailResponse(
    val id: String = "",
    val genesis_date: String? = null,
    val hashing_algorithm: String? = null,
    val market_data: MarketDataResponse? = null
)

@Serializable
data class SearchCoinItem(
    val id: String = ""
)

@Serializable
data class CoinSearchResponse(
    val coins: List<SearchCoinItem> = emptyList()
)

fun CoinResponse.toCoin(): Coin {
    var cleanImageUrl = (image ?: "").trim()
    if (cleanImageUrl.startsWith("http://")) {
        cleanImageUrl = cleanImageUrl.replaceFirst("http://", "https://")
    }

    return Coin(
        id = id,
        symbol = symbol,
        name = name,
        image = cleanImageUrl,
        current_price = current_price ?: 0.0,
        price_change_percentage_24h = price_change_percentage_24h ?: 0.0,
        market_cap = market_cap ?: 0.0,
        total_volume = total_volume ?: 0.0,
        high_24h = high_24h ?: 0.0,
        low_24h = low_24h ?: 0.0
    )
}

interface CoinGeckoApi {
    @GET("coins/markets")
    suspend fun getCoins(
        @Header("x-cg-demo-api-key") apiKey: String,
        @Query("vs_currency") currency: String,
        @Query("ids") ids: String? = null,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): List<CoinResponse>

    @GET("search")
    suspend fun searchCoins(
        @Header("x-cg-demo-api-key") apiKey: String,
        @Query("query") query: String
    ): CoinSearchResponse

    @GET("coins/{id}")
    suspend fun getCoinDetail(
        @Header("x-cg-demo-api-key") apiKey: String,
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false
    ): CoinDetailResponse
}

private val coinJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

val coinGeckoApi: CoinGeckoApi = Retrofit.Builder()
    .baseUrl("https://api.coingecko.com/api/v3/")
    .addConverterFactory(coinJson.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(CoinGeckoApi::class.java)
