package com.mking1102.bluetooth_pairing.common.broadcasters

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
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

class BlueToothPairedRequestBroadcasters @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager:BluetoothManager
) {


    private val connectionState = MutableSharedFlow<BluetoothDevice>(replay = 0)
    val sharedFlow: SharedFlow<BluetoothDevice> = connectionState
    val scope = ScopeShared(this.javaClass, errorCallback = { throwable, error ->
        throwable.printStackTrace()
        Log.e(this.javaClass.name, error)

    })


    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED ->{
                    println("blue tooth paired chang${intent}")

                    val device:BluetoothDevice?  =intent.getParcelableExtra(EXTRA_DEVICE)

                    println("bonded ${device} ${device?.uuids}")
                 scope.scope.launch {
                     device?.let { connectionState.emit(it) }
                 }

                }



            }

        }


    }

    init {

        try {
            context.registerReceiver(
                broadCastReceiver,
                IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
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