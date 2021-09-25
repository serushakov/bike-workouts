package io.ushakov.bike_workouts.view_models

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.ushakov.bike_workouts.ui.views.BluetoothSettingsViewModelInterface
import java.util.*

class BluetoothSettingsViewModel(application: Application, val bluetoothAdapter: BluetoothAdapter) :
    AndroidViewModel(application),
    BluetoothSettingsViewModelInterface {
    private val BLUETOOTH_SERVICE_UUID = UUID.fromString("000180D-0000-1000-8000-00805F9B34FB")

    override val isScanning: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    override val deviceList: MutableLiveData<List<ScanResult>> by lazy {
        MutableLiveData(listOf())
    }

    private val deviceMap = mutableMapOf<String, ScanResult>()

    private inner class BleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            val device = result?.device ?: return

            if(!deviceMap.containsKey(device.address)) {
                deviceMap[device.address] = result
                deviceList.value = deviceList.value?.plus(result)
            }
        }
    }

    override fun startScan() {
        val callback = BleScanCallback()
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val settings = ScanSettings
            .Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val filter: List<ScanFilter> = listOf(
            ScanFilter
                .Builder()
                .setServiceUuid(
                    ParcelUuid(BLUETOOTH_SERVICE_UUID)
                ).build()
        )

        isScanning.value = true

        scanner.startScan(filter, settings, callback)
    }
}