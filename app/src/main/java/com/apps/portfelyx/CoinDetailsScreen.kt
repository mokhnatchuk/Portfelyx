package com.apps.portfelyx

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

fun sendPriceNotification(context: Context, coinName: String, priceText: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "price_alerts"
    val channel = NotificationChannel(
        channelId,
        "Сповіщення про курс",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    notificationManager.createNotificationChannel(channel)

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.coin_bitcoin)
        .setContentTitle(coinName)
        .setContentText("Поточна ціна: $priceText")
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1, notification)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailsScreen(
    coinId: String,
    currency: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CoinListViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var notificationState by remember { mutableStateOf("idle") }

    var selectedCoin: Coin? = null
    var displayCurrency = currency

    if (uiState.loadedCurrency == currency) {
        for (item in uiState.coins) {
            if (item.id == coinId) {
                selectedCoin = item
                displayCurrency = uiState.loadedCurrency
                break
            }
        }
    }
    if (selectedCoin == null && uiState.loadedFavoritesCurrency == currency) {
        for (item in uiState.favoriteCoins) {
            if (item.id == coinId) {
                selectedCoin = item
                displayCurrency = uiState.loadedFavoritesCurrency
                break
            }
        }
    }
    if (selectedCoin == null) {
        for (item in uiState.coins) {
            if (item.id == coinId) {
                selectedCoin = item
                if (uiState.loadedCurrency.isNotEmpty()) {
                    displayCurrency = uiState.loadedCurrency
                }
                break
            }
        }
    }
    if (selectedCoin == null) {
        for (item in uiState.favoriteCoins) {
            if (item.id == coinId) {
                selectedCoin = item
                if (uiState.loadedFavoritesCurrency.isNotEmpty()) {
                    displayCurrency = uiState.loadedFavoritesCurrency
                }
                break
            }
        }
    }
    val coin = selectedCoin

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && coin != null) {
            val formattedPrice = formatPrice(coin.current_price, displayCurrency)
            sendPriceNotification(context, coin.name, formattedPrice)
            notificationState = "sent"
        } else {
            notificationState = "denied"
        }
    }

    val cutoutInsets = WindowInsets.displayCutout
    val scaffoldInsets = WindowInsets.systemBars.union(cutoutInsets)
    val topBarInsets = TopAppBarDefaults.windowInsets.union(
        cutoutInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = scaffoldInsets,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = coin?.name ?: "Монета",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    windowInsets = topBarInsets,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        if (coin != null) {
                            IconButton(onClick = onToggleFavorite) {
                                if (isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Прибрати з добірки",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "Додати в добірку",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }
        }
    ) { innerPadding ->
        if (coin == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Монету не знайдено",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val currentPriceText = formatPrice(coin.current_price, displayCurrency)
            val fallbackPainter = painterResource(id = coinIcon(coin.id))
            val change24hColor = if (coin.price_change_percentage_24h >= 0.0) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.error
            }
            val heroTintColor = if (coin.price_change_percentage_24h >= 0.0) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
            }
            val heroCardBrush = Brush.verticalGradient(
                colors = listOf(
                    heroTintColor,
                    MaterialTheme.colorScheme.surface
                )
            )
            val backgroundColor = MaterialTheme.colorScheme.background
            val transparentBackgroundColor = backgroundColor.copy(alpha = 0f)
            val topFadeBrush = Brush.verticalGradient(
                colors = listOf(backgroundColor, transparentBackgroundColor)
            )
            val bottomFadeBrush = Brush.verticalGradient(
                colors = listOf(transparentBackgroundColor, backgroundColor)
            )
            val fadeHeight = if (isLandscape) 16.dp else 24.dp
            val verticalContentPadding = if (isLandscape) 16.dp else 20.dp

            val marketCapText = if (coin.market_cap > 0.0) {
                "${formatPrice(coin.market_cap / 1_000_000_000, displayCurrency)} млрд"
            } else {
                "—"
            }
            val totalVolumeText = if (coin.total_volume > 0.0) {
                "${formatPrice(coin.total_volume / 1_000_000_000, displayCurrency)} млрд"
            } else {
                "—"
            }
            val high24hText = if (coin.high_24h > 0.0) {
                formatPrice(coin.high_24h, displayCurrency)
            } else {
                "—"
            }
            val low24hText = if (coin.low_24h > 0.0) {
                formatPrice(coin.low_24h, displayCurrency)
            } else {
                "—"
            }

            val onSendNotificationClick = {
                if (Build.VERSION.SDK_INT >= 33) {
                    val status = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (status == PackageManager.PERMISSION_GRANTED) {
                        sendPriceNotification(context, coin.name, currentPriceText)
                        notificationState = "sent"
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    sendPriceNotification(context, coin.name, currentPriceText)
                    notificationState = "sent"
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = verticalContentPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(brush = heroCardBrush)
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = coin.image,
                                    contentDescription = coin.name,
                                    placeholder = fallbackPainter,
                                    error = fallbackPainter,
                                    fallback = fallbackPainter,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Text(
                                text = "${coin.name} (${coin.symbol.uppercase()})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = currentPriceText,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, change24hColor)
                            ) {
                                Text(
                                    text = "24 год: ${formatChangePercentage(coin.price_change_percentage_24h)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = change24hColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 3.dp, height = 18.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = "Token info",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CoinMetricCard(
                            label = "Капіталізація",
                            value = marketCapText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        CoinMetricCard(
                            label = "Обсяг (24 год)",
                            value = totalVolumeText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CoinMetricCard(
                            label = "Максимум (24 год)",
                            value = high24hText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        CoinMetricCard(
                            label = "Мінімум (24 год)",
                            value = low24hText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    val change7d = coin.price_change_percentage_7d
                    if (change7d != null) {
                        val change7dColor = if (change7d >= 0.0) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        CoinMetricRowCard(
                            label = "Зміна за 7 днів",
                            value = formatChangePercentage(change7d),
                            valueColor = change7dColor
                        )
                    }

                    if (coin.hashing_algorithm.isNotEmpty()) {
                        CoinMetricRowCard(
                            label = "Алгоритм хешування",
                            value = coin.hashing_algorithm,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (coin.genesis_date.isNotEmpty()) {
                        CoinMetricRowCard(
                            label = "Дата створення",
                            value = coin.genesis_date,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val favoriteActionColor = if (isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    OutlinedButton(
                        onClick = onToggleFavorite,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, favoriteActionColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = favoriteActionColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        if (isFavorite) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Прибрати з добірки",
                                style = MaterialTheme.typography.labelLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Додати в добірку",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (notificationState == "sent") {
                        OutlinedButton(
                            onClick = onSendNotificationClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Сповіщення надіслано",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else if (notificationState == "denied") {
                        OutlinedButton(
                            onClick = onSendNotificationClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Дозвіл на сповіщення вимкнено",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Button(
                            onClick = onSendNotificationClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Надіслати ціну в сповіщення",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fadeHeight)
                        .align(Alignment.TopCenter)
                        .background(brush = topFadeBrush)
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fadeHeight)
                        .align(Alignment.BottomCenter)
                        .background(brush = bottomFadeBrush)
                )
            }
        }
    }
}

@Composable
fun CoinMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CoinMetricRowCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = valueColor,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}
