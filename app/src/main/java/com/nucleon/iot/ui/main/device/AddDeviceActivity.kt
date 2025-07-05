package com.nucleon.iot.ui.main.device

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.nucleon.iot.R
import com.nucleon.iot.data.model.Device
import com.nucleon.iot.data.model.Relay
import com.nucleon.iot.data.repository.DeviceRepository
import com.nucleon.iot.databinding.ActivityAddDeviceBinding
import com.nucleon.iot.ui.main.device.steps.Step1SelectDeviceFragment
import com.nucleon.iot.ui.main.device.steps.Step2ConfigureFragment
import com.nucleon.iot.ui.main.device.steps.Step3FinalizeFragment
import com.nucleon.iot.utils.PreferenceManager
import kotlinx.coroutines.launch

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDeviceBinding
    private val deviceRepository = DeviceRepository()

    var selectedDeviceType: DeviceType? = null
    var deviceName: String = ""
    var wifiSSID: String = ""
    var wifiPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(PreferenceManager.getTheme())

        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewPager() {
        val adapter = AddDevicePagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateStepIndicators(position)
                updateButtons(position)
            }
        })
    }

    private fun setupButtons() {
        binding.previousButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.currentItem = currentItem - 1
            }
        }

        binding.nextButton.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            when (currentItem) {
                0 -> {
                    if (selectedDeviceType != null) {
                        binding.viewPager.currentItem = 1
                    } else {
                        showError("Pilih jenis perangkat terlebih dahulu")
                    }
                }
                1 -> {
                    if (validateStep2()) {
                        binding.viewPager.currentItem = 2
                    }
                }
                2 -> {
                    completeDeviceSetup()
                }
            }
        }
    }

    private fun updateStepIndicators(position: Int) {
        val indicators = listOf(
            binding.step1Indicator,
            binding.step2Indicator,
            binding.step3Indicator
        )

        indicators.forEachIndexed { index, textView ->
            if (index <= position) {
                textView.setBackgroundResource(R.drawable.circle_primary)
                textView.setTextColor(getColor(R.color.white))
            } else {
                textView.setBackgroundResource(R.drawable.circle_grey)
                textView.setTextColor(getColor(R.color.text_secondary))
            }
        }
    }

    private fun updateButtons(position: Int) {
        binding.previousButton.visibility = if (position > 0) View.VISIBLE else View.GONE

        binding.nextButton.text = when (position) {
            2 -> "Selesai"
            else -> "Lanjut"
        }
    }

    private fun validateStep2(): Boolean {
        if (deviceName.isEmpty()) {
            showError("Nama perangkat tidak boleh kosong")
            return false
        }

        if (wifiSSID.isEmpty()) {
            showError("SSID WiFi tidak boleh kosong")
            return false
        }

        if (wifiPassword.isEmpty()) {
            showError("Password WiFi tidak boleh kosong")
            return false
        }

        return true
    }

    private fun completeDeviceSetup() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val deviceType = selectedDeviceType ?: return@launch

                // Create relay map based on device type
                val relays = mutableMapOf<String, Relay>()
                for (i in 1..deviceType.relayCount) {
                    relays["relay$i"] = Relay(
                        name = "Relay $i",
                        status = "off"
                    )
                }

                // Create device object
                val device = Device(
                    name = deviceName,
                    type = deviceType.name,
                    mode = if (deviceType.hasBluetooth) "bluetooth" else "wifi",
                    relays = relays,
                    online = true
                )

                // Save to Firebase
                deviceRepository.addDevice(device)
                    .onSuccess {
                        showSuccess("Perangkat berhasil ditambahkan")
                        finish()
                    }
                    .onFailure { exception ->
                        showError("Gagal menambahkan perangkat: ${exception.message}")
                    }

            } catch (e: Exception) {
                showError("Terjadi kesalahan: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        // TODO: Implement loading UI
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    inner class AddDevicePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 3

        override fun createFragment(position: Int) = when (position) {
            0 -> Step1SelectDeviceFragment()
            1 -> Step2ConfigureFragment()
            2 -> Step3FinalizeFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}