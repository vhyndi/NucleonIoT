package com.nucleon.iot.ui.main.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nucleon.iot.R
import com.nucleon.iot.databinding.ItemDeviceTypeBinding

data class DeviceType(
    val name: String,
    val relayCount: Int,
    val hasWifi: Boolean,
    val hasBluetooth: Boolean,
    var isSelected: Boolean = false
)

class DeviceTypeAdapter(
    private val onDeviceTypeSelected: (DeviceType) -> Unit
) : RecyclerView.Adapter<DeviceTypeAdapter.DeviceTypeViewHolder>() {

    private val deviceTypes = listOf(
        DeviceType("Nucleon Mini Lite", 2, true, false),
        DeviceType("Nucleon Mini Pro", 2, true, true),
        DeviceType("Nucleon Core Lite", 4, true, false),
        DeviceType("Nucleon Core Pro", 4, true, true),
        DeviceType("Nucleon Edge Pro", 8, true, true),
        DeviceType("Nucleon Ultra Pro", 16, true, true)
    )

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceTypeViewHolder {
        val binding = ItemDeviceTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceTypeViewHolder, position: Int) {
        val deviceType = deviceTypes[position]
        holder.bind(deviceType, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val selectedDevice = deviceTypes[adapterPosition]
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                if (previousPosition != -1) notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onDeviceTypeSelected(selectedDevice)
            }
        }
    }

    override fun getItemCount(): Int = deviceTypes.size

    class DeviceTypeViewHolder(
        private val binding: ItemDeviceTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            deviceType: DeviceType,
            isSelected: Boolean
        ) {
            binding.apply {
                productNameText.text = deviceType.name

                val connectivityText = when {
                    deviceType.hasWifi && deviceType.hasBluetooth -> "WiFi + Bluetooth"
                    deviceType.hasWifi -> "WiFi only"
                    else -> ""
                }
                productSpecsText.text = "${deviceType.relayCount} Relay • $connectivityText"

                val iconRes = when {
                    deviceType.name.contains("Mini") -> R.drawable.ic_device_mini
                    deviceType.name.contains("Core") -> R.drawable.ic_device_core
                    deviceType.name.contains("Edge") -> R.drawable.ic_device_edge
                    deviceType.name.contains("Ultra") -> R.drawable.ic_device_ultra
                    else -> R.drawable.ic_device_type
                }
                productIcon.setImageResource(iconRes)

                radioButton.isChecked = isSelected
            }
        }
    }
}
