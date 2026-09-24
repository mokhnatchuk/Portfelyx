package com.apps.portfelyx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.portfelyx.ui.theme.PortfelyxTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortfelyxTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {TopAppBar(title = {Text("Portfelyx")}) }
                ) { innerPadding ->
                    CoinListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CoinListScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var selectedFilter by remember { mutableStateOf("Всі") }
        val visibleCoins = when (selectedFilter) {
            "Зростають" -> sampleCoins.filter { it.price_change_percentage_24h >= 0 }
            "Падають" -> sampleCoins.filter { it.price_change_percentage_24h < 0 }
            else -> sampleCoins
        }

        ChangeFilterRow(
            options = changeFilters,
            selected = selectedFilter,
            onSelect = { selectedFilter = it }
        )

        if (visibleCoins.isEmpty()) {
            Text("Нічого не знайдено")
        } else {
            for (coin in visibleCoins) {
                CoinCard(coin = coin)
            }
        }
    }
}

val changeFilters = listOf("Всі", "Зростають", "Падають")

@Composable
fun ChangeFilterRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (option in options) {
            FilterChip(
                selected = (option == selected),
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
fun CoinCard(coin: Coin, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = coinIcon(coin.id)),
                    contentDescription = coin.name,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = coin.symbol.uppercase(),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
            Column {
                Text(text = coin.name)
                Text(text = "$${coin.current_price}")
                Text(text = "${coin.price_change_percentage_24h}%")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinListScreenPreview() {
    PortfelyxTheme {
        CoinListScreen()
    }
}