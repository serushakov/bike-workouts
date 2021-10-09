package io.ushakov.bike_workouts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.Timeout
import io.reactivex.disposables.Disposable
import io.ushakov.bike_workouts.util.Constants
import java.util.concurrent.TimeUnit

class HeartRateDeviceManager(context: Context) {
    private var bleClient = RxBleClient.create(context)
    private var connection: RxBleConnection? = null
    private val callbacks: MutableSet<(value: Int) -> Unit> = mutableSetOf()
    private var notificationsDisposable: Disposable? = null
    private var connectionDisposable: Disposable? = null

    val isPairing by lazy { MutableLiveData(false) }
    val device by lazy { MutableLiveData<RxBleDevice?>(null) }
    val isConnected by lazy { MutableLiveData(false) }

    companion object {
        private var instance: HeartRateDeviceManager? = null

        fun initialize(context: Context) {
            instance = HeartRateDeviceManager(context)
        }

        fun getInstance(): HeartRateDeviceManager {
            return instance!!
        }
    }

    private fun decodeHeartRate(data: ByteArray): Int {
        val (flag, valueByte1, valueByte2) = data
        var value = -1

        // Format is 8bit, hence only first byte should be used
        if (flag.toInt() and 0x01 == 0) {
            value = valueByte1.toInt()
        } else {
            // Construct a 16bit value from 2 bytes, little-endian
            listOf(valueByte1, valueByte2).forEachIndexed { index, byte ->
                value = value or (byte.toInt() shl 8 * index)
            }
        }

        return value
    }

    private fun sendUpdates(heartRate: Int) {
        for (callback in callbacks) callback(heartRate)
    }

    private fun handleDeviceConnectionChange(state: RxBleConnection.RxBleConnectionState) {
        when (state) {
            RxBleConnection.RxBleConnectionState.CONNECTING -> isPairing.value = true
            RxBleConnection.RxBleConnectionState.CONNECTED -> {
                isPairing.value = false
                isConnected.value = true
            }
            RxBleConnection.RxBleConnectionState.DISCONNECTED -> isConnected.value = false
            else -> Unit
        }
    }

    fun setupDevice(
        address: String,
        error: (Throwable) -> Unit,
    ): Disposable {
        val device = bleClient.getBleDevice(address)

        connectionDisposable =
            device.observeConnectionStateChanges()?.subscribe {
                Handler(Looper.getMainLooper()).post {
                    handleDeviceConnectionChange(it)
                }
            }

        return device
            .establishConnection(false, Timeout(5, TimeUnit.SECONDS))
            .flatMap {
                Handler(Looper.getMainLooper()).post {
                    this.device.value = device
                }
                it.setupNotification(Constants.HEART_RATE_CHARACTERISTIC_UUID)
            }
            .flatMap { it }
            .subscribe({ value ->
                val decodedHeartRate = decodeHeartRate(value)

                sendUpdates(decodedHeartRate)
            }, error)
    }

    fun forgetDevice() {
        notificationsDisposable?.dispose()
        device.value = null
        isConnected.value = false
        isPairing.value = false
    }

    fun subscribe(callback: (value: Int) -> Unit): Disposable {
        callbacks += callback

        return object : Disposable {
            override fun dispose() {
                callbacks.remove(callback)
            }

            override fun isDisposed(): Boolean {
                return callbacks.contains(callback)
            }
        }
    }

    fun cleanup() {
        notificationsDisposable?.dispose()
        connectionDisposable?.dispose()
    }
}