package com.mking1102.bluetooth_pairing.common.broadcasters

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.mking1102.bluetooth_pairing.common.utils.ScopeShared
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlueToothBroadcasters @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothAdapter: BluetoothAdapter
) {


    private val connectionState = MutableSharedFlow<Boolean>(replay = 0)
    val sharedFlow: SharedFlow<Boolean> = connectionState
    val scope = ScopeShared(this.javaClass, errorCallback = { throwable, error ->
        throwable.printStackTrace()
        Log.e(this.javaClass.name, error)

    })


    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("bluetooth state changed")

            if (intent?.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                when (intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        scope.scope.launch {
                            connectionState.emit(false)
                        }
                    }
                    BluetoothAdapter.STATE_ON -> {
                        scope.scope.launch {
                            connectionState.emit(true)
                        }
                    }
                    else -> Unit
                }

            }

        }
    }

    init {

        try {
            context.registerReceiver(
                broadCastReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
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


    fun checkBluetooth() {
        scope.scope.launch {
            connectionState.emit(bluetoothManager.adapter?.isEnabled == true)

        }
    }
}