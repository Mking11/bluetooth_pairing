package com.mking1102.bluetooth_pairing

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BlueToothViewModel @Inject constructor(
) : ViewModel() {
    val devices = MutableLiveData<HashMap<String, BluetoothDevice>>(hashMapOf())
    fun addDevice(it: BluetoothDevice?) {
        val current = devices.value
        it?.address?.let { it1 -> current?.put(it1, it) }

        current?.let {
            devices.postValue(it)
        }

    }

    fun clearDevices() {
        devices.postValue(hashMapOf())
    }

}

