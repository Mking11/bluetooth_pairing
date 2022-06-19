package com.mking1102.bluetooth_pairing.common.broadcasters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.mking1102.bluetooth_pairing.common.domain.models.BlueToothBroadCastingState
import com.mking1102.bluetooth_pairing.common.utils.ScopeShared
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlueToothPairedBroadcasters @Inject constructor(
    @ApplicationContext private val context: Context
) {


    private val connectionState = MutableSharedFlow<BlueToothBroadCastingState>(replay = 0)
    val sharedFlow: SharedFlow<BlueToothBroadCastingState> = connectionState
    val scope = ScopeShared(this.javaClass, errorCallback = { throwable, error ->
        throwable.printStackTrace()
        Log.e(this.javaClass.name, error)

    })


    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    scope.scope.launch {
                        connectionState.emit(BlueToothBroadCastingState.DeviceFound(device))
                    }

                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    scope.scope.launch {
                        connectionState.emit(BlueToothBroadCastingState.DiscoveryStarted)
                    }

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    scope.scope.launch {
                        connectionState.emit(BlueToothBroadCastingState.DiscoveryEnded)
                    }

                }
            }

        }

        override fun peekService(myContext: Context?, service: Intent?): IBinder {
            println(service)
            return super.peekService(myContext, service)
        }
    }

    init {

        try {
            context.registerReceiver(
                broadCastReceiver,
                IntentFilter(BluetoothDevice.ACTION_FOUND)
            )

        } catch (e: Exception) {

           e.printStackTrace()
        }
    }

    fun closeBroadCasters() {
        try {
            context.unregisterReceiver(broadCastReceiver)
        } catch (e: Exception) {
          e.printStackTrace()
        }
    }


}