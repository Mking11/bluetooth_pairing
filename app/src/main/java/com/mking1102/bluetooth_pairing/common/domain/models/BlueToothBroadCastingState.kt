package com.mking1102.bluetooth_pairing.common.domain.models

import android.bluetooth.BluetoothDevice

sealed class BlueToothBroadCastingState() {

    object DiscoveryStarted : BlueToothBroadCastingState()
    object DiscoveryEnded : BlueToothBroadCastingState()
    data class DeviceFound(val device: BluetoothDevice?) : BlueToothBroadCastingState()

}
