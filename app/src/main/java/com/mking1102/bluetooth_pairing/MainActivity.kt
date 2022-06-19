package com.mking1102.bluetooth_pairing

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mking1102.bluetooth_pairing.common.broadcasters.BlueToothBroadcasters
import com.mking1102.bluetooth_pairing.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


    @Inject
    lateinit var blueToothBroadcasters: BlueToothBroadcasters

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            when (it.resultCode) {
                RESULT_CANCELED -> {
                    this.finish()
                    viewModel.hasLaunched = false
                }
                RESULT_OK -> {
                    viewModel.hasLaunched = false
                }
            }

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(this.layoutInflater)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.main_navigation) as NavHostFragment

        navController = navHost.navController
        checkPermissions()

        lifecycleScope.launch {
            blueToothBroadcasters.checkBluetooth()
            blueToothBroadcasters.sharedFlow.collect {
                if (!it) {
                    checkIfBlueToothIsEnabled()
                }
            }
        }



        setContentView(binding.root)
    }

    private fun checkIfBlueToothIsEnabled() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        getContent.launch(enableBtIntent)
        viewModel.hasLaunched = true
    }

    private fun checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(permissions, 1)
        } else {
            if (!hasPermissions(this, *permissions)) {

                requestPermissions(permissions, 1)
            }
        }


    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isEmpty() || !hasPermissions(this, *permissions)) {
                checkPermissions()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        blueToothBroadcasters.closeBroadCasters()
    }
}