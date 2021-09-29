package io.ushakov.bike_workouts

import android.content.Context
import android.util.Log
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.disposables.Disposable
import io.ushakov.bike_workouts.util.Constants

class HeartRateDeviceManager(context: Context) {
    private var bleClient = RxBleClient.create(context)
    private var device: RxBleDevice? = null
    private val callbacks: MutableSet<(value: Int) -> Unit> = mutableSetOf()
    private var notificationsDisposable: Disposable? = null

    var deviceAddress: String = ""
        set(value) {
            device = bleClient.getBleDevice(value)
        }

    class DeviceNotInitializedError : Throwable() {
        override fun getLocalizedMessage(): String {
            return "Device has not been initialized"
        }
    }

    companion object {
        private var instance: HeartRateDeviceManager? = null

        fun initialize(context: Context) {
            instance = HeartRateDeviceManager(context)
        }

        fun getInstance(): HeartRateDeviceManager {
            return instance!!
        }
    }

    private fun setupNotifications() {
        notificationsDisposable = device!!.establishConnection(false)
            .flatMap { it.setupNotification(Constants.HEART_RATE_CHARACTERISTIC_UUID) }
            .flatMap { it }
            .subscribe({ value ->
                val decodedHeartRate = decodeHeartRate(value)

                sendUpdates(decodedHeartRate)
            }) { throwable ->
                Log.d("HeartRateDeviceManager", throwable.localizedMessage.toString())
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


    fun hasDevice(): Boolean {
        return device != null
    }


    fun subscribe(callback: (value: Int) -> Unit): Disposable {
        if (!hasDevice()) throw DeviceNotInitializedError()

        if (notificationsDisposable == null) {
            setupNotifications()
        }

        callbacks += callback

        return object : Disposable {
            override fun dispose() {
                callbacks.remove(callback)

                if (callbacks.size == 0 && notificationsDisposable != null) {
                    notificationsDisposable!!.dispose()
                }
            }

            override fun isDisposed(): Boolean {
                return callbacks.contains(callback)
            }
        }
    }
}