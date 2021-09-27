package io.ushakov.bike_workouts.ui.views

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.ushakov.bike_workouts.BluetoothService
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.components.BleListItem
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme
import io.ushakov.myapplication.ui.theme.Typography
import java.util.*


interface BluetoothSettingsViewModelInterface {
    val isScanning: LiveData<Boolean>
    val deviceList: LiveData<List<ScanResult>>
    fun startScan()
    fun stopScan()
}

@Composable
fun BluetoothSettings(
    navController: NavController,
) {
    Scaffold(
        topBar = { BluetoothSettingsAppBar(navController) }
    ) {
        View()
    }
}

@Composable
internal fun View() {

    ConstraintLayout(
        modifier = Modifier.padding(16.dp)
    ) {
        val (listTitle, list) = createRefs()
        Text(
            text = stringResource(R.string.bluetooth_device_search_list_title).uppercase(),
            modifier = Modifier.constrainAs(listTitle) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
            style = Typography.overline
        )

        LiveDataDeviceList(modifier = Modifier.constrainAs(list) {
            top.linkTo(listTitle.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        })
    }
}

@Composable
fun LiveDataDeviceList(modifier: Modifier) {
    val context = LocalContext.current
    val rxBleClient = remember {
        derivedStateOf {
            RxBleClient.create(context)
        }
    }

    var deviceList by
    remember { mutableStateOf(listOf<com.polidea.rxandroidble2.scan.ScanResult>()) }

    DisposableEffect(rxBleClient) {
        val settings = ScanSettings
            .Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filter = ScanFilter
            .Builder()
            .setServiceUuid(
                ParcelUuid(BluetoothService.uuidHeartRateMeasurement)
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


    DeviceList(modifier, list = deviceList)
}

@Composable
fun DeviceList(modifier: Modifier, list: List<com.polidea.rxandroidble2.scan.ScanResult>) {
    val (deviceAddress, setDeviceAddress) = remember { mutableStateOf<String?>(null) }
    val rxBleClient = RxBleClient.create(LocalContext.current)

    val logOptions = LogOptions.Builder().setLogLevel(LogConstants.DEBUG).build()
//    RxBleClient.updateLogOptions(logOptions)

    DisposableEffect(key1 = deviceAddress) {
        if (deviceAddress != null) {
            val device = rxBleClient.getBleDevice(deviceAddress)
            val disposable = device.establishConnection(false)
                .flatMap {
                    it.setupNotification(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"))
                }
                .flatMap {
                    it
                }
                .subscribe({ data ->
                    val (flag, value) = data


                    Log.d("data", value.toInt().toString())
                }) { throwable ->
                    Log.d("error", throwable.localizedMessage)
                }
            onDispose {
                disposable.dispose()
            }
        } else {
            onDispose { }
        }
    }

    LazyColumn(modifier = modifier) {
        items(items = list) { item ->
            BleListItem(deviceName = item.bleDevice.name ?: "No name") {
                setDeviceAddress(item.bleDevice.macAddress)
            }
        }
    }
}

fun ByteArray.toHex() = joinToString("") { String.format("%02X", (it.toInt() and 0xff)) }


fun ByteArray.littleEndianConversion(): Int {
    var result = 0
    for (i in this.indices) {
        result = result or (this[i].toInt() shl 8 * i)
    }
    return result
}

@Composable
fun BluetoothSettingsAppBar(navController: NavController) {
    TopAppBar(
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

@Preview(widthDp = 480, heightDp = 840)
@Composable
internal fun BluetoothSettingsPreview() {
    BikeWorkoutsTheme {
        BluetoothSettings(rememberNavController())
    }
}