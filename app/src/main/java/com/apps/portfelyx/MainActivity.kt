package com.apps.portfelyx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import com.apps.portfelyx.ui.theme.PortfelyxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortfelyxTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "coins") {
                    composable("coins") {
                        CoinListScreen(
                            onItemClick = { coinId ->
                                navController.navigate("coin/$coinId")
                            }
                        )
                    }
                    composable("coin/{coinId}") { backStackEntry ->
                        val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
                        CoinDetailsScreen(
                            coinId = coinId,
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
fun CoinListScreen(modifier: Modifier = Modifier, viewModel: CoinListViewModel = viewModel(), onItemClick: (String) -> Unit) {
    val uiState = viewModel.uiState
    val visibleCoins = when (uiState.selectedFilter) {
        "Зростають" -> sampleCoins.filter { coin -> coin.price_change_percentage_24h >= 0 }
        "Падають" -> sampleCoins.filter { coin -> coin.price_change_percentage_24h < 0 }
        else -> sampleCoins
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Portfelyx") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChangeFilterRow(
                options = changeFilters,
                selected = uiState.selectedFilter,
                onSelect = { newFilter -> viewModel.selectFilter(newFilter) }
            )

            if (visibleCoins.isEmpty()) {
                Text("Нічого не знайдено")
            } else {
                for (coin in visibleCoins) {
                    CoinCard(coin = coin, onClick = { onItemClick(coin.id) })
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
fun CoinCard(coin: Coin, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
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
        CoinListScreen(onItemClick = {})
    }
}