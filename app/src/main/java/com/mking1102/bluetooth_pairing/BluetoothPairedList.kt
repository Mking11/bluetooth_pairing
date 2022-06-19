package com.mking1102.bluetooth_pairing

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mking1102.bluetooth_pairing.databinding.FragmentBlueToothPairedListBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothPairedList : Fragment() {


    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter


    private lateinit var binding: FragmentBlueToothPairedListBinding

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBlueToothPairedListBinding.inflate(inflater, container, false)
        recyclerView = binding.deviceList

        binding.pairDevice.setOnClickListener {

            requireView().findNavController()
                .navigate(BluetoothPairedListDirections.actionBluetoothlistToPairDevice())
        }




        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val bondedDevices: MutableSet<BluetoothDevice>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter.bondedDevices
                } else {
                    null
                }

            } else {

                bluetoothAdapter.bondedDevices
            }
        val adapter =
            PairedBluetoothListAdapter(object : PairedBluetoothListAdapter.CustomListeners {
                override fun onItemSelected(position: BluetoothDevice) {
                    position.createBond()
                }

            })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        adapter.submitList(bondedDevices?.toList() ?: listOf())
    }
}