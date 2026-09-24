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
    Coin("solana", "sol", "Solana", "", 172.8, 3.61),
    Coin("cardano", "ada", "Cardano", "", 0.234, -2.31),
    Coin("dogecoin", "doge", "Dogecoin", "", 0.092, 5.12),
    Coin("tether", "usdt", "Tether", "", 0.999, 0.01)
)

fun coinIcon(coinId: String): Int {
    return when (coinId) {
        "ethereum" -> R.drawable.coin_ethereum
        "solana" -> R.drawable.coin_solana
        "cardano" -> R.drawable.coin_cardano
        "dogecoin" -> R.drawable.coin_dogecoin
        "tether" -> R.drawable.coin_tether
        else -> R.drawable.coin_bitcoin
    }
}