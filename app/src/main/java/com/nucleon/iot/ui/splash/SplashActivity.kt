package com.nucleon.iot.ui.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nucleon.iot.databinding.ActivitySplashBinding
import com.nucleon.iot.ui.auth.LoginActivity
import com.nucleon.iot.ui.main.MainActivity
import com.nucleon.iot.utils.PreferenceManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme based on saved preference
        setTheme(PreferenceManager.getTheme())

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start animations
        startAnimations()
    }

    private fun startAnimations() {
        // Fade in logo
        binding.logoImage.alpha = 0f
        binding.logoImage.animate()
            .alpha(1f)
            .setDuration(800)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Wait for 2 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        fadeOutAndNavigate()
                    }, 2000)
                }
            })
            .start()
    }

    private fun fadeOutAndNavigate() {
        binding.root.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    navigateToNextScreen()
                }
            })
            .start()
    }

    private fun navigateToNextScreen() {
        val intent = if (auth.currentUser != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}