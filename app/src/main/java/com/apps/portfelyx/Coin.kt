package com.apps.portfelyx

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val price_change_percentage_24h: Double,
    val market_cap: Double,
    val total_volume: Double,
    val high_24h: Double,
    val low_24h: Double
)

val sampleCoins = listOf(
    Coin("bitcoin", "btc", "Bitcoin", "", 77671.0, -0.95, 1_538_000_000_000.0, 45_000_000_000.0, 78_900.0, 76_100.0),
    Coin("ethereum", "eth", "Ethereum", "", 3420.5, 1.24, 412_000_000_000.0, 18_000_000_000.0, 3_480.0, 3_360.0),
    Coin("solana", "sol", "Solana", "", 172.8, 3.61, 83_000_000_000.0, 4_200_000_000.0, 176.4, 168.2),
    Coin("cardano", "ada", "Cardano", "", 0.234, -2.31, 8_200_000_000.0, 320_000_000.0, 0.241, 0.229),
    Coin("dogecoin", "doge", "Dogecoin", "", 0.092, 5.12, 13_600_000_000.0, 900_000_000.0, 0.095, 0.089),
    Coin("tether", "usdt", "Tether", "", 0.999, 0.01, 172_000_000_000.0, 60_000_000_000.0, 1.001, 0.998)
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