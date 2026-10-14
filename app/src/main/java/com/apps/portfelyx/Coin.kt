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

val currencySymbols = mapOf("usd" to "$", "eur" to "€", "uah" to "₴")

fun formatPrice(price: Double, currency: String): String {
    val symbol = currencySymbols[currency] ?: "$"
    val rounded = if (price >= 100) {
        "%.2f".format(price)
    } else {
        "%.4f".format(price)
    }
    return "$symbol$rounded"
}
