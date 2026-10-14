package com.apps.portfelyx

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@Serializable
data class CoinResponse(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String = "",
    val current_price: Double,
    val price_change_percentage_24h: Double? = null,
    val market_cap: Double = 0.0,
    val total_volume: Double = 0.0,
    val high_24h: Double = 0.0,
    val low_24h: Double = 0.0
)

fun CoinResponse.toCoin(): Coin {
    return Coin(
        id = id,
        symbol = symbol,
        name = name,
        image = image,
        current_price = current_price,
        price_change_percentage_24h = price_change_percentage_24h ?: 0.0,
        market_cap = market_cap,
        total_volume = total_volume,
        high_24h = high_24h,
        low_24h = low_24h
    )
}

interface CoinGeckoApi {
    @GET("coins/markets")
    suspend fun getCoins(
        @Header("x-cg-demo-api-key") apiKey: String,
        @Query("vs_currency") currency: String,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): List<CoinResponse>
}

private val coinJson = Json { ignoreUnknownKeys = true }

val coinGeckoApi: CoinGeckoApi = Retrofit.Builder()
    .baseUrl("https://api.coingecko.com/api/v3/")
    .addConverterFactory(coinJson.asConverterFactory(MediaType.parse("application/json")!!))
    .build()
    .create(CoinGeckoApi::class.java)