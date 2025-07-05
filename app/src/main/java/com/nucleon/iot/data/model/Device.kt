package com.nucleon.iot.data.model

data class Device(
    var id: String = "",
    val name: String = "",
    val type: String = "",
    val mode: String = "wifi",
    val relays: Map<String, Relay> = emptyMap(),
    val online: Boolean = false
)