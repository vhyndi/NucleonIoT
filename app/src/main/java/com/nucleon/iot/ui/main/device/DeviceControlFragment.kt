package com.nucleon.iot.ui.main.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nucleon.iot.data.model.Device
import com.nucleon.iot.data.model.Relay
import com.nucleon.iot.databinding.FragmentDeviceControlBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DeviceControlFragment : Fragment() {

    private var _binding: FragmentDeviceControlBinding? = null
    private val binding get() = _binding!!

    private lateinit var relayAdapter: RelayAdapter
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var deviceId: String = ""
    private var device: Device? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceId = arguments?.getString("deviceId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDevice()
        setupRealtimeListener()
    }

    private fun setupRecyclerView() {
        relayAdapter = RelayAdapter { relay, position ->
            toggleRelay(relay, position)
        }

        binding.relaysRecyclerView.apply {
            val columns = when (device?.type) {
                "Nucleon Mini Lite", "Nucleon Mini Pro" -> 2
                "Nucleon Core Lite", "Nucleon Core Pro" -> 2
                "Nucleon Edge Pro" -> 4
                "Nucleon Ultra Pro" -> 4
                else -> 2
            }

            layoutManager = GridLayoutManager(context, columns)
            adapter = relayAdapter
        }
    }

    private fun loadDevice() {
        lifecycleScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val snapshot = database.reference
                    .child("users")
                    .child(userId)
                    .child("devices")
                    .child(deviceId)
                    .get()
                    .await()

                device = snapshot.getValue(Device::class.java)
                device?.let {
                    binding.deviceNameText.text = it.name
                    binding.deviceTypeText.text = it.type
                    binding.connectionModeText.text = "Mode: ${it.mode}"

                    setupRelays(it)
                }

            } catch (e: Exception) {
                showError("Gagal memuat perangkat: ${e.message}")
            }
        }
    }

    private fun setupRelays(device: Device) {
        val relayCount = when (device.type) {
            "Nucleon Mini Lite", "Nucleon Mini Pro" -> 2
            "Nucleon Core Lite", "Nucleon Core Pro" -> 4
            "Nucleon Edge Pro" -> 8
            "Nucleon Ultra Pro" -> 16
            else -> 2
        }

        val relays = mutableListOf<Relay>()
        for (i in 1..relayCount) {
            val relay = device.relays["relay$i"] ?: Relay(
                name = "Relay $i",
                status = "off"
            )
            relays.add(relay)
        }

        relayAdapter.submitList(relays)
    }

    private fun setupRealtimeListener() {
        val userId = auth.currentUser?.uid ?: return
        val deviceRef = database.reference
            .child("users")
            .child(userId)
            .child("devices")
            .child(deviceId)
            .child("relays")

        deviceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedRelays = mutableListOf<Relay>()
                snapshot.children.forEach { relaySnapshot ->
                    val relay = relaySnapshot.getValue(Relay::class.java)
                    relay?.let { updatedRelays.add(it) }
                }

                if (updatedRelays.isNotEmpty()) {
                    relayAdapter.submitList(updatedRelays)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Koneksi gagal: ${error.message}")
            }
        })
    }

    private fun toggleRelay(relay: Relay, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val newStatus = if (relay.status == "on") "off" else "on"

        lifecycleScope.launch {
            try {
                database.reference
                    .child("users")
                    .child(userId)
                    .child("devices")
                    .child(deviceId)
                    .child("relays")
                    .child("relay${position + 1}")
                    .child("status")
                    .setValue(newStatus)
                    .await()

            } catch (e: Exception) {
                showError("Gagal mengubah status relay: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        // Contoh implementasi sederhana:
        binding.deviceNameText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
