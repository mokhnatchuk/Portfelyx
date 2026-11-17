package com.apps.portfelyx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteCoinsScreen(
    viewModel: CoinListViewModel,
    favoriteIds: Set<String>,
    currency: String,
    onItemClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onCatalogClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState = viewModel.uiState

    LaunchedEffect(favoriteIds, currency) {
        viewModel.loadFavoriteCoins(favoriteIds, currency)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Моя добірка") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Налаштування")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onCatalogClick,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Каталог") },
                    label = { Text("Каталог") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Добірка") },
                    label = { Text("Добірка") }
                )
            }
        }
    ) { innerPadding ->
        if (favoriteIds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "У вашій добірці ще немає збережених монет. Натисніть на сердечко біля монети, щоб додати її сюди.")
            }
        } else if (uiState.isFavoritesLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.favoritesErrorMessage.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                Text(text = uiState.favoritesErrorMessage)
                Button(onClick = { viewModel.loadFavoriteCoins(favoriteIds, currency) }) {
                    Text(text = "Спробувати ще")
                }
            }
        } else if (uiState.favoriteCoins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "У вашій добірці ще немає збережених монет.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.favoriteCoins,
                    key = { coinItem -> coinItem.id }
                ) { coinItem ->
                    val isCoinFavorite = favoriteIds.contains(coinItem.id)
                    CoinCard(
                        coin = coinItem,
                        priceText = formatPrice(coinItem.current_price, currency),
                        isFavorite = isCoinFavorite,
                        onFavoriteClick = { onToggleFavorite(coinItem.id) },
                        onClick = { onItemClick(coinItem.id) }
                    )
                }
            }
        }
    }
}