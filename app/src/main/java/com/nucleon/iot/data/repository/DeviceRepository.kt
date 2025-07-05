package com.nucleon.iot.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nucleon.iot.data.model.Device
import com.nucleon.iot.data.model.Relay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeviceRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    suspend fun addDevice(device: Device): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val deviceRef = database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .push()

            device.id = deviceRef.key ?: ""
            deviceRef.setValue(device).await()

            Result.success(device.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDevice(deviceId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .child(deviceId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDevice(deviceId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .child(deviceId)
                .removeValue()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDevicesFlow(): Flow<List<Device>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                snapshot.children.forEach { deviceSnapshot ->
                    val device = deviceSnapshot.getValue(Device::class.java)
                    device?.let {
                        it.id = deviceSnapshot.key ?: ""
                        devices.add(it)
                    }
                }
                trySend(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = database.reference
            .child("users")
            .child(uid)
            .child("devices")

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    suspend fun getDevice(deviceId: String): Result<Device> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .child(deviceId)
                .get()
                .await()

            val device = snapshot.getValue(Device::class.java)
            device?.let {
                it.id = deviceId
                Result.success(it)
            } ?: Result.failure(Exception("Device not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRelaysFlow(deviceId: String): Flow<Map<String, Relay>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val relays = mutableMapOf<String, Relay>()
                snapshot.children.forEach { relaySnapshot ->
                    val relay = relaySnapshot.getValue(Relay::class.java)
                    relay?.let {
                        relays[relaySnapshot.key ?: ""] = it
                    }
                }
                trySend(relays)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = database.reference
            .child("users")
            .child(uid)
            .child("devices")
            .child(deviceId)
            .child("relays")

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    suspend fun toggleRelay(
        deviceId: String,
        relayIndex: Int,
        newStatus: String
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .child(deviceId)
                .child("relays")
                .child("relay$relayIndex")
                .child("status")
                .setValue(newStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRelayName(
        deviceId: String,
        relayIndex: Int,
        newName: String
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .child(deviceId)
                .child("relays")
                .child("relay$relayIndex")
                .child("name")
                .setValue(newName)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setAllRelays(deviceId: String, status: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val device = getDevice(deviceId).getOrThrow()
            val updates = mutableMapOf<String, Any>()

            device.relays.keys.forEach { relayKey ->
                updates["$relayKey/status"] = status
            }

            database.reference
                .child("users")
                .child(uid)
                .child("devices")
                .child(deviceId)
                .child("relays")
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}