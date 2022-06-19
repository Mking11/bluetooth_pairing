package com.mking1102.bluetooth_pairing.common

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object BlueToothModule {

    @ActivityScoped
    @Provides
    fun provideBluetTooth(@ApplicationContext context: Context): BluetoothManager{
        return context.getSystemService(BluetoothManager::class.java)
    }

    @Provides
    @ActivityScoped
    fun provideAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter {
        return bluetoothManager.adapter
    }
}