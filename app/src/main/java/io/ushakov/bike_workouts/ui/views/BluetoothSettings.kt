package io.ushakov.bike_workouts.ui.views

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.R
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme
import io.ushakov.myapplication.ui.theme.Typography

interface BluetoothSettingsViewModelInterface {
    val isScanning: LiveData<Boolean>
    val deviceList: LiveData<List<ScanResult>>
    fun startScan()
}

@Composable
fun BluetoothSettings(
    navController: NavController,
    viewModel: BluetoothSettingsViewModelInterface
) {
    Scaffold(
        topBar = { BluetoothSettingsAppBar(navController) }
    ) {
        View(viewModel)
    }
}

@Composable
internal fun View(viewModel: BluetoothSettingsViewModelInterface) {
    viewModel.startScan()

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
        }, viewModel.deviceList)
    }
}

@Composable
fun LiveDataDeviceList(modifier: Modifier, list: LiveData<List<ScanResult>>) {
    val resultList: List<ScanResult> by list.observeAsState(initial = listOf())

    DeviceList(modifier, list = resultList)
}

@Composable
fun DeviceList(modifier: Modifier, list: List<ScanResult>) {
    LazyColumn(modifier = modifier) {
        items(items = list) { item ->
            Text(item.device.name)
        }
    }
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
        BluetoothSettings(rememberNavController(), object : BluetoothSettingsViewModelInterface {
            override val isScanning: LiveData<Boolean> by lazy {
                MutableLiveData(true)
            }
            override val deviceList: LiveData<List<ScanResult>> = MutableLiveData(listOf())
            override fun startScan() {}
        })
    }
}