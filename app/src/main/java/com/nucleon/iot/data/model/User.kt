package com.nucleon.iot.data.model

import androidx.annotation.Keep

@Keep
data class User(
    val uid: String = "",        // Firebase UID
    val email: String = "",      // Email pengguna
    val name: String = "",       // Nama tampilan
    val theme: String = "light", // Preferensi tema UI: "light" atau "dark"
    val language: String = "id"  // Bahasa UI default: "id", bisa juga "en", dst.
)
