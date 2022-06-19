package com.mking1102.bluetooth_pairing

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor():ViewModel() {
    var hasLaunched = false
}