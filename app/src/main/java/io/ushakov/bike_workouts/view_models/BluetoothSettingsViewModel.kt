package io.ushakov.bike_workouts.view_models

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.ushakov.bike_workouts.ui.views.BluetoothSettingsViewModelInterface
import java.util.*

const val BLUETOOTH_SERVICE_UUID = "000180D-0000-1000-8000-00805F9B34FB"

class BluetoothSettingsViewModel(private val bluetoothAdapter: BluetoothAdapter) :
    ViewModel(),
    BluetoothSettingsViewModelInterface {

    private var scanner: BluetoothLeScanner? = null
    private val bleScanCallback by lazy {
        BleScanCallback()
    }

    override val isScanning: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    override val deviceList: MutableLiveData<List<ScanResult>> by lazy {
        MutableLiveData(listOf())
    }

    private val deviceMap = mutableMapOf<String, ScanResult>()

    init {
        Log.d("debug", "init")
    }


    private inner class BleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            val device = result?.device ?: return

            if (!deviceMap.containsKey(device.address)) {
                deviceMap[device.address] = result
                deviceList.value = deviceList.value?.plus(result)
            }
        }
    }

    override fun startScan() {
        scanner = bluetoothAdapter.bluetoothLeScanner
        val settings = ScanSettings
            .Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val filter: List<ScanFilter> = listOf(
            ScanFilter
                .Builder()
                .setServiceUuid(
                    ParcelUuid(UUID.fromString(BLUETOOTH_SERVICE_UUID))
                ).build()
        )

        isScanning.value = true

        scanner!!.startScan(filter, settings, bleScanCallback)
        Log.d("BluetoothViewModel", "startScan")
    }

    override fun stopScan() {
        scanner?.stopScan(bleScanCallback)
        Log.d("BluetoothViewModel", "stopScan")
    }
}

class BluetoothSettingsViewModelFactory(private val bluetoothAdapter: BluetoothAdapter) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothSettingsViewModel::class.java)) {
            return BluetoothSettingsViewModel(bluetoothAdapter) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }


}