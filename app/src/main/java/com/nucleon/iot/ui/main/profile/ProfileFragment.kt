package com.nucleon.iot.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.nucleon.iot.R
import com.nucleon.iot.data.repository.AuthRepository
import com.nucleon.iot.databinding.FragmentProfileBinding
import com.nucleon.iot.ui.auth.LoginActivity
import com.nucleon.iot.utils.PreferenceManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupSettings()
        setupAccountActions()
    }

    private fun setupUserInfo() {
        auth.currentUser?.let { user ->
            binding.nameText.text = user.displayName ?: "User"
            binding.emailText.text = user.email
        }
    }

    private fun setupSettings() {
        // Theme toggle
        val isDarkMode = PreferenceManager.getTheme() == R.style.Theme_Nucleon_Dark
        binding.themeSwitch.isChecked = isDarkMode

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.setDarkMode(isChecked)
            requireActivity().recreate()
        }

        // Language setting
        binding.languageLayout.setOnClickListener {
            showLanguageDialog()
        }

        // Update current language display
        val currentLanguage = PreferenceManager.getLanguage()
        binding.currentLanguageText.text = if (currentLanguage == "id") {
            "Bahasa Indonesia"
        } else {
            "English"
        }

        // Notifications toggle
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implement notification settings
        }
    }

    private fun setupAccountActions() {
        // Change password
        binding.changePasswordLayout.setOnClickListener {
            showChangePasswordDialog()
        }

        // Logout
        binding.logoutLayout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Bahasa Indonesia", "English")
        val languageCodes = arrayOf("id", "en")
        val currentLanguage = PreferenceManager.getLanguage()
        val checkedItem = languageCodes.indexOf(currentLanguage)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pilih Bahasa")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                PreferenceManager.setLanguage(languageCodes[which])
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        // TODO: Implement change password dialog
        Snackbar.make(binding.root, "Fitur ganti password akan segera hadir", Snackbar.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Keluar dari Aplikasi?")
            .setMessage("Anda yakin ingin keluar dari akun Anda?")
            .setPositiveButton("Keluar") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        authRepository.signOut()

        // Clear preferences
        PreferenceManager.clearUserData()

        // Navigate to login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}