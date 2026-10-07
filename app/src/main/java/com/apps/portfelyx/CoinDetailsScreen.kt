package com.apps.portfelyx

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailsScreen(coinId: String, onBackClick: () -> Unit) {
    val coin = sampleCoins.find { it.id == coinId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(coin?.name ?: "Монета") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (coin == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Монету не знайдено")
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = coinIcon(coin.id)),
                    contentDescription = coin.name,
                    modifier = Modifier.size(96.dp).align(Alignment.CenterHorizontally)
                )
                Text("Ціна: $${coin.current_price}")
                Text("Зміна за 24 год: ${coin.price_change_percentage_24h}%")
                Text("Капіталізація: $${coin.market_cap}")
                Text("Обсяг торгів за 24 год: $${coin.total_volume}")
                Text("Максимум за 24 год: $${coin.high_24h}")
                Text("Мінімум за 24 год: $${coin.low_24h}")
            }
        }
    }
}
