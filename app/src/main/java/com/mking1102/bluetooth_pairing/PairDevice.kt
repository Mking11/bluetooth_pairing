package com.mking1102.bluetooth_pairing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mking1102.bluetooth_pairing.common.broadcasters.BlueToothPairedBroadcasters
import com.mking1102.bluetooth_pairing.common.broadcasters.BlueToothPairedRequestBroadcasters
import com.mking1102.bluetooth_pairing.common.domain.models.BlueToothBroadCastingState
import com.mking1102.bluetooth_pairing.databinding.FragmentPairDeviceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class PairDevice : Fragment() {

    private lateinit var binding: FragmentPairDeviceBinding

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter


    @Inject
    lateinit var blueToothPairedBroadcasters: BlueToothPairedBroadcasters

    @Inject
    lateinit var blueToothPairedRequestBroadcasters: BlueToothPairedRequestBroadcasters


    private lateinit var recyclerView: RecyclerView

    private lateinit var connectionThread: ConnectThread

    private val viewModel: BlueToothViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPairDeviceBinding.inflate(inflater, container, false)
        recyclerView = binding.deviceList

        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val adapter =
            PairedBluetoothListAdapter(object :
                PairedBluetoothListAdapter.CustomListeners {
                override fun onItemSelected(position: BluetoothDevice) {
                    println("clicked ${position}")


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {

                            println("uuids ${position.uuids}")
                            position.uuids.forEach {
                                println("uuids ${it}")
                            }
                        }
                    } else {

                        position.uuids?.forEach {
                            println("it${it}")
                        }
                    }


                    position.setPin("000000".toByteArray())
                    position.createBond().apply {
                        if (this) {
//                            println("create bond ${this}")
//
                        }
                    }


                }

            })

        lifecycleScope.launch {
            blueToothPairedRequestBroadcasters.sharedFlow.collect {
                println("blue tooth ${it}")

                connectionThread =
                    ConnectThread(it, requireContext())
                connectionThread.run()


            }
        }


        recyclerView.adapter = adapter

        viewModel.devices.observe(viewLifecycleOwner) {
            println("map ${it}")
            adapter.submitList(it.values.toList())

        }

        lifecycleScope.launch {
            blueToothPairedBroadcasters.sharedFlow.collect {

                when (it) {
                    is BlueToothBroadCastingState.DeviceFound -> {
                        val device: BluetoothDevice? =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(
                                        requireContext(),
                                        Manifest.permission.BLUETOOTH_SCAN
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    it.device
                                } else {

                                    null
                                }
                            } else {
                                it.device
                            }
                        println("device ${device?.name}")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            println("device ${device?.alias}")
                        }
                        println("device ${device?.address}")

                        viewModel.addDevice(it.device)
                    }

                    BlueToothBroadCastingState.DiscoveryEnded -> Unit
                    BlueToothBroadCastingState.DiscoveryStarted -> {
                        viewModel.clearDevices()
                    }
                }

            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.startDiscovery()
            }

        } else {

            bluetoothAdapter.startDiscovery()
        }





        return binding.root
    }


    override fun onDestroy() {
        super.onDestroy()
        if (this::connectionThread.isInitialized) {

        }

        blueToothPairedBroadcasters.closeBroadCasters()
    }


//    @SuppressLint("MissingPermission")
//    private inner class AcceptThread(firebaseCrashRepository: FirebaseCrashRepository) : Thread() {
//
//        private val UUID = "8ac9a3b0-eb0f-11ec-a084-290857f3ab76"
//        private val mmSocket: BluetoothServerSocket by lazy(LazyThreadSafetyMode.NONE) {
//            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
//                "Ubutix",
//                java.util.UUID.fromString(UUID)
//            )
//        }
//
//        override fun run() {
//            var shouldLoop = true
//            while (shouldLoop) {
//                val socket: BluetoothSocket? = try {
//                    println(mmSocket)
//                    mmSocket?.accept()
//
//                } catch (e: IOException) {
//                    shouldLoop = false
//                    null
//                }
//                socket?.also {
//                    println("socket ${it}")
//                    mmSocket?.close()
//                    shouldLoop = false
//                }
//            }
//        }
//
//        fun cancle() {
//            try {
//                mmSocket?.close()
//            } catch (e: IOException) {
//                firebaseCrashRepository.setErrorToFirebase(e, "cancle PairDevice.kt  181: ")
//            }
//        }
//    }
//}


    @SuppressLint("MissingPermission")
    private inner class ConnectThread(
        device: BluetoothDevice,
        context: android.content.Context
    ) :
        Thread() {


        private val UUID = "8ac9a3b0-eb0f-11ec-a084-290857f3ab76"
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        device.createInsecureRfcommSocketToServiceRecord(
                            java.util.UUID.fromString(
                                UUID
                            )
                        )

                    } else {
                        null
                    }
                } else {

                    device.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        }

        override fun run() {
            bluetoothAdapter.cancelDiscovery()
            try {
                mmSocket?.let {
                    it.connect()
                }
            } catch (e: Exception) {
                println(e)
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}




