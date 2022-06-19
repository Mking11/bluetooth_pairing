package com.mking1102.bluetooth_pairing

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mking1102.bluetooth_pairing.databinding.PairedDeviceItemBinding

class PairedBluetoothListAdapter(private val customListeners: CustomListeners) :
    RecyclerView.Adapter<PairedBluetoothListAdapter.PairedBluetoothItem>() {

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<BluetoothDevice>() {

        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean {
            return oldItem == newItem
        }

    }

    private val listDiffer = AsyncListDiffer(this, diffUtilItemCallback)

    private lateinit var binding: PairedDeviceItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairedBluetoothItem {
        binding =
            PairedDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PairedBluetoothItem(binding, customListeners)
    }

    override fun onBindViewHolder(holder: PairedBluetoothItem, position: Int) {
        holder.bind(listDiffer.currentList[position])
    }

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }

    fun submitList(list: List<BluetoothDevice>) {
        listDiffer.submitList(list)
    }

    class PairedBluetoothItem
    constructor(
        private val binding: PairedDeviceItemBinding,
        private val customListeners: CustomListeners
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(item: BluetoothDevice) {

            binding.apply {
                bluetoothId.text = item.name ?: item.address
            }

            binding.root.setOnClickListener {
                    customListeners.onItemSelected(item)
            }

        }
    }

    interface CustomListeners {
        fun onItemSelected(position:BluetoothDevice)
    }
}
