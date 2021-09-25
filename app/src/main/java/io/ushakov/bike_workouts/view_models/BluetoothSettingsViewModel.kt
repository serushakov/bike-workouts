package io.ushakov.bike_workouts.view_models

import android.app.Application
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.ushakov.bike_workouts.ui.views.BluetoothSettingsViewModelInterface

class BluetoothSettingsViewModel(application: Application) : AndroidViewModel(application),
    BluetoothSettingsViewModelInterface {
    override val isScanning: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    override val deviceList: MutableLiveData<List<ScanResult>> by lazy {
        MutableLiveData(listOf())
    }
}