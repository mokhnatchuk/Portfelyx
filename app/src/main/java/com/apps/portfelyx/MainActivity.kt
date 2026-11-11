package com.apps.portfelyx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apps.portfelyx.ui.theme.PortfelyxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortfelyxTheme {
                val navController = rememberNavController()
                val settingsViewModel: SettingsViewModel = viewModel()
                val currency by settingsViewModel.currency.collectAsState()
                val coinListViewModel: CoinListViewModel = viewModel()

                LaunchedEffect(currency) {
                    coinListViewModel.loadCoins(currency, forceReload = false)
                }

                NavHost(navController = navController, startDestination = "coins") {
                    composable("coins") {
                        CoinListScreen(
                            viewModel = coinListViewModel,
                            currency = currency,
                            onItemClick = { coinId -> navController.navigate("coin/$coinId") },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("coin/{coinId}") { backStackEntry ->
                        val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
                        CoinDetailsScreen(
                            coinId = coinId,
                            currency = currency,
                            onBackClick = { navController.navigateUp() },
                            viewModel = coinListViewModel
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            currency = currency,
                            onCurrencyChange = { selectedCurrency ->
                                settingsViewModel.setCurrency(selectedCurrency)
                            },
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListScreen(
    modifier: Modifier = Modifier,
    viewModel: CoinListViewModel = viewModel(),
    currency: String,
    onItemClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState = viewModel.uiState
    val coins = uiState.coins
    val visibleCoins = ArrayList<Coin>(coins.size)

    for (coin in coins) {
        if (uiState.selectedFilter == "Зростають") {
            if (coin.price_change_percentage_24h >= 0.0) {
                visibleCoins.add(coin)
            }
        } else if (uiState.selectedFilter == "Падають") {
            if (coin.price_change_percentage_24h < 0.0) {
                visibleCoins.add(coin)
            }
        } else {
            visibleCoins.add(coin)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Portfelyx") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Налаштування")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ChangeFilterRow(
                options = changeFilters,
                selected = uiState.selectedFilter,
                onSelect = { newFilter -> viewModel.selectFilter(newFilter) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    Text(text = uiState.errorMessage)
                    Button(onClick = { viewModel.loadCoins(currency, forceReload = true) }) {
                        Text(text = "Спробувати ще")
                    }
                }
            } else if (visibleCoins.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (coins.isEmpty()) {
                        Text(text = "Сервер повернув порожній список монет.")
                    } else {
                        Text(text = "За обраним фільтром нічого не знайдено.")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = visibleCoins,
                        key = { coinItem -> coinItem.id }
                    ) { coinItem ->
                        CoinCard(
                            coin = coinItem,
                            priceText = formatPrice(coinItem.current_price, currency),
                            onClick = { onItemClick(coinItem.id) }
                        )
                    }
                }
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
        modifier = modifier.horizontalScroll(rememberScrollState()),
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
fun CoinCard(coin: Coin, priceText: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
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
                Text(text = priceText)
                Text(text = "${coin.price_change_percentage_24h}%")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinListScreenPreview() {
    PortfelyxTheme {
        CoinListScreen(currency = "usd", onItemClick = {}, onSettingsClick = {})
    }
}