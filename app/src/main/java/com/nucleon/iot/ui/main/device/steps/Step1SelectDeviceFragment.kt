package com.nucleon.iot.ui.main.device.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nucleon.iot.databinding.FragmentStep1SelectDeviceBinding
import com.nucleon.iot.ui.main.device.AddDeviceActivity
import com.nucleon.iot.ui.main.device.DeviceTypeAdapter

class Step1SelectDeviceFragment : Fragment() {

    private var _binding: FragmentStep1SelectDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep1SelectDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = DeviceTypeAdapter { deviceType ->
            (activity as? AddDeviceActivity)?.selectedDeviceType = deviceType
        }

        binding.devicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}