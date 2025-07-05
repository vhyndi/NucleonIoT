package com.nucleon.iot.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.nucleon.iot.data.repository.AuthRepository
import com.nucleon.iot.databinding.ActivityRegisterBinding
import com.nucleon.iot.ui.main.MainActivity
import com.nucleon.iot.utils.PreferenceManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(PreferenceManager.getTheme())

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Register button
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                performRegister(name, email, password)
            }
        }

        // Login link
        binding.loginLink.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Nama tidak boleh kosong"
            return false
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email tidak boleh kosong"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Email tidak valid"
            return false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password tidak boleh kosong"
            return false
        }

        if (password.length < 6) {
            binding.passwordInputLayout.error = "Password minimal 6 karakter"
            return false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Password tidak sama"
            return false
        }

        if (!binding.termsCheckBox.isChecked) {
            showError("Harap setujui syarat dan ketentuan")
            return false
        }

        // Clear errors
        binding.nameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null

        return true
    }

    private fun performRegister(name: String, email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            authRepository.signUp(email, password, name)
                .onSuccess {
                    navigateToMain()
                }
                .onFailure { exception ->
                    showLoading(false)
                    showError("Pendaftaran gagal: ${exception.message}")
                }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}