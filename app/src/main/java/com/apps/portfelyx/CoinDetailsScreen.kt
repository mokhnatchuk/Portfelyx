package com.apps.portfelyx

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

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
    onBackClick: () -> Unit,
    viewModel: CoinListViewModel = viewModel()
) {
    val context = LocalContext.current
    var notificationStatusText by remember { mutableStateOf("") }

    var selectedCoin: Coin? = null
    for (item in viewModel.uiState.coins) {
        if (item.id == coinId) {
            selectedCoin = item
            break
        }
    }
    val coin = selectedCoin

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && coin != null) {
            val formattedPrice = formatPrice(coin.current_price, currency)
            sendPriceNotification(context, coin.name, formattedPrice)
            notificationStatusText = "Сповіщення з ціною надіслано."
        } else {
            notificationStatusText = "Дозвіл на сповіщення відхилено."
        }
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Монету не знайдено")
            }
        } else {
            val currentPriceText = formatPrice(coin.current_price, currency)
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                Text("Ціна: $currentPriceText")
                Text("Зміна за 24 год: ${coin.price_change_percentage_24h}%")
                Text("Капіталізація: ${formatPrice(coin.market_cap / 1_000_000_000, currency)} млрд")
                Text("Обсяг торгів за 24 год: ${formatPrice(coin.total_volume / 1_000_000_000, currency)} млрд")
                Text("Максимум за 24 год: ${formatPrice(coin.high_24h, currency)}")
                Text("Мінімум за 24 год: ${formatPrice(coin.low_24h, currency)}")

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            val status = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            if (status == PackageManager.PERMISSION_GRANTED) {
                                sendPriceNotification(context, coin.name, currentPriceText)
                                notificationStatusText = "Сповіщення з ціною надіслано."
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            sendPriceNotification(context, coin.name, currentPriceText)
                            notificationStatusText = "Сповіщення з ціною надіслано."
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Надіслати ціну в сповіщення")
                }

                if (notificationStatusText.isNotEmpty()) {
                    Text(text = notificationStatusText)
                }
            }
        }
    }
}