package io.ushakov.bike_workouts.ui.views

import android.os.ParcelUuid
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.components.BleListItem
import io.ushakov.bike_workouts.ui.components.ButtonStatus
import io.ushakov.bike_workouts.ui.components.SectionTitle
import io.ushakov.bike_workouts.ui.components.ThemedTopAppBar
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.myapplication.ui.theme.Typography
import java.util.*

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun BluetoothSettings(
    navController: NavController,
    isPairing: Boolean,
    pairingDeviceAddress: String?,
    pairedDevice: RxBleDevice?,
    onDevicePair: (deviceAddress: String) -> Unit,
    onUnpairClick: () -> Unit
) {
    Scaffold(
        topBar = { BluetoothSettingsAppBar(navController) }
    ) {
        View(onDevicePair, isPairing, pairingDeviceAddress, pairedDevice, onUnpairClick)
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
internal fun View(
    onDevicePair: (deviceAddress: String) -> Unit,
    isPairing: Boolean,
    pairingDeviceAddress: String?,
    pairedDevice: RxBleDevice?,
    onUnpairClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        if (pairedDevice != null) {
            Row {
                ConnectedDevice(device = pairedDevice, onUnpairClick = onUnpairClick)
            }
        } else {
            DeviceList(
                onDevicePair,
                isPairing,
                pairingDeviceAddress,
            )
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun ConnectedDevice(
    device: RxBleDevice?,
    modifier: Modifier = Modifier,
    onUnpairClick: () -> Unit,
) {
    val (showButton, setShowButton) = remember { mutableStateOf(false) }

    Column(modifier) {
        SectionTitle(text = "Connected device")
        Card(elevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
            onClick = { setShowButton(!showButton) }) {
            Column(Modifier.padding(all = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, "Icon", tint = Color.Red)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(device?.name ?: "DeviceName", style = Typography.h6)
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text((device?.macAddress ?: "Address").capitalize(),
                                style = Typography.caption)
                        }
                    }
                    Spacer(Modifier.weight(1f, true))
                    Icon(Icons.Default.Check, "Check icon", tint = Color.Green)
                }
                AnimatedVisibility(visible = showButton,
                    enter = expandVertically(),
                    exit = shrinkVertically()) {
                    Spacer(Modifier.height(16.dp))
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(visible = showButton,
                        enter = expandVertically(),
                        exit = shrinkVertically()) {
                        Button(onClick = onUnpairClick) {
                            Text("Unpair")
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun DeviceList(
    onDevicePair: (deviceAddress: String) -> Unit,
    isPairing: Boolean,
    pairingDeviceAddress: String?,
    modifier: Modifier = Modifier,
) {
    val deviceList = rememberDeviceList()

    val workoutsList =
        Column {
            SectionTitle(text = stringResource(R.string.bluetooth_device_search_list_title))

            LazyColumn(modifier = modifier) {
                items(items = deviceList) { item ->
                    BleListItem(
                        deviceName = item.bleDevice.name ?: "No name",
                        when {
                            item.bleDevice.macAddress == pairingDeviceAddress && isPairing -> ButtonStatus.PAIRING
                            isPairing -> ButtonStatus.DISABLED
                            else -> ButtonStatus.DEFAULT
                        }) {

                        onDevicePair(item.bleDevice.macAddress)
                    }
                }
            }
        }

}


@Composable
fun BluetoothSettingsAppBar(navController: NavController) {
    ThemedTopAppBar(
        title = { Text(stringResource(R.string.bluetooth_setup_title)) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.navigation_back))
            }
        },
    )
}

@Composable
fun rememberDeviceList(): List<ScanResult> {
    val context = LocalContext.current
    val rxBleClient = remember {
        derivedStateOf {
            RxBleClient.create(context)
        }
    }

    var deviceList by remember { mutableStateOf(listOf<ScanResult>()) }

    DisposableEffect(rxBleClient) {
        val settings = ScanSettings
            .Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filter = ScanFilter
            .Builder()
            .setServiceUuid(
                ParcelUuid(Constants.HEART_RATE_SERVICE_UUID)
            )
            .build()


        val disposable = rxBleClient.value.scanBleDevices(settings, filter)
            .subscribe({ scanResult ->
                if (deviceList.any { it.bleDevice.macAddress == scanResult.bleDevice.macAddress }) return@subscribe

                val newList = deviceList.toMutableList()
                newList.add(scanResult)
                deviceList = newList

            }
            ) { throwable ->
                Log.d("error", throwable.localizedMessage)
            }


        onDispose {
            disposable.dispose()
        }
    }

    return deviceList
}