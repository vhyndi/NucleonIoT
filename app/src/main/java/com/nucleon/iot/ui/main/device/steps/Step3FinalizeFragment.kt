package com.nucleon.iot.ui.main.device.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nucleon.iot.databinding.FragmentStep3FinalizeBinding
import com.nucleon.iot.ui.main.device.AddDeviceActivity

class Step3FinalizeFragment : Fragment() {

    private var _binding: FragmentStep3FinalizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep3FinalizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displaySummary()
    }

    private fun displaySummary() {
        val activity = activity as? AddDeviceActivity ?: return

        binding.apply {
            deviceNameText.text = activity.deviceName
            deviceTypeText.text = activity.selectedDeviceType?.name ?: ""
            wifiSSIDText.text = activity.wifiSSID

            val connectionType = when {
                activity.selectedDeviceType?.hasWifi == true &&
                        activity.selectedDeviceType?.hasBluetooth == true -> "WiFi + Bluetooth"
                activity.selectedDeviceType?.hasWifi == true -> "WiFi"
                else -> ""
            }
            connectionTypeText.text = connectionType
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}