package com.apps.portfelyx

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val price_change_percentage_24h: Double
)

val sampleCoins = listOf(
    Coin("bitcoin", "btc", "Bitcoin", "", 77671.0, -0.95),
    Coin("ethereum", "eth", "Ethereum", "", 3420.5, 1.24),
    Coin("solana", "sol", "Solana", "", 172.8, 3.61)
)

fun coinIcon(coinId: String): Int {
    return when (coinId) {
        "ethereum" -> R.drawable.coin_ethereum
        "solana" -> R.drawable.coin_solana
        else -> R.drawable.coin_bitcoin
    }
}