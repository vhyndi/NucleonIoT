package com.nucleon.iot.ui.main.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nucleon.iot.data.model.Device
import com.nucleon.iot.databinding.FragmentDashboardBinding
import com.nucleon.iot.ui.main.device.AddDeviceActivity
import com.nucleon.iot.ui.main.device.DeviceControlFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var deviceAdapter: DeviceAdapter
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFAB()
        loadDevices()

        // Set welcome message
        binding.welcomeText.text = "Selamat datang, ${auth.currentUser?.displayName ?: "User"}"
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            navigateToDeviceControl(device)
        }

        binding.devicesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = deviceAdapter
        }
    }

    private fun setupFAB() {
        binding.addDeviceFab.setOnClickListener {
            startActivity(Intent(requireContext(), AddDeviceActivity::class.java))
        }
    }

    private fun loadDevices() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val userId = auth.currentUser?.uid ?: return@launch
                val snapshot = database.reference
                    .child("users")
                    .child(userId)
                    .child("devices")
                    .get()
                    .await()

                val devices = mutableListOf<Device>()
                snapshot.children.forEach { deviceSnapshot ->
                    val device = deviceSnapshot.getValue(Device::class.java)
                    device?.let {
                        it.id = deviceSnapshot.key ?: ""
                        devices.add(it)
                    }
                }

                deviceAdapter.submitList(devices)
                showEmptyState(devices.isEmpty())

            } catch (e: Exception) {
                showError("Gagal memuat perangkat: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun navigateToDeviceControl(device: Device) {
        // Navigate to device control fragment
        val bundle = Bundle().apply {
            putString("deviceId", device.id)
        }

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, DeviceControlFragment().apply {
                arguments = bundle
            })
            .addToBackStack(null)
            .commit()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.devicesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        // Show snackbar or toast
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}