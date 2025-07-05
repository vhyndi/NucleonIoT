package com.nucleon.iot.ui.main.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nucleon.iot.R
import com.nucleon.iot.data.model.Device
import com.nucleon.iot.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onDeviceClick: (Device) -> Unit
) : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onDeviceClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onDeviceClick: (Device) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.apply {
                // Set device info
                deviceNameText.text = device.name

                // Set device type with relay count
                val relayCount = when (device.type) {
                    "Nucleon Mini Lite", "Nucleon Mini Pro" -> 2
                    "Nucleon Core Lite", "Nucleon Core Pro" -> 4
                    "Nucleon Edge Pro" -> 8
                    "Nucleon Ultra Pro" -> 16
                    else -> 0
                }

                deviceTypeText.text = "${device.type.split(" ").last()} • $relayCount Relay"

                // Set status indicator
                if (device.online) {
                    statusIndicator.setBackgroundResource(R.drawable.circle_green)
                } else {
                    statusIndicator.setBackgroundResource(R.drawable.circle_red)
                }

                // Count active relays
                val activeRelays = device.relays.values.count { it.status == "on" }
                activeRelaysText.text = "$activeRelays/$relayCount Aktif"

                // Set icon based on device type
                val iconRes = when {
                    device.type.contains("Mini") -> R.drawable.ic_device_mini
                    device.type.contains("Core") -> R.drawable.ic_device_core
                    device.type.contains("Edge") -> R.drawable.ic_device_edge
                    device.type.contains("Ultra") -> R.drawable.ic_device_ultra
                    else -> R.drawable.ic_device
                }
                deviceIcon.setImageResource(iconRes)

                // Click listeners
                root.setOnClickListener { onDeviceClick(device) }
                quickToggleButton.setOnClickListener { onDeviceClick(device) }
            }
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem == newItem
        }
    }
}