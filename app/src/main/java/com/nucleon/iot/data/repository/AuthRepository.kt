package com.nucleon.iot.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.nucleon.iot.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login gagal"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Update display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                user.updateProfile(profileUpdates).await()

                // Create user profile in database
                createUserProfile(user.uid, email, displayName)

                Result.success(user)
            } ?: Result.failure(Exception("Pendaftaran gagal"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()

            result.user?.let { user ->
                // Check if user profile exists
                val profileExists = checkUserProfileExists(user.uid)
                if (!profileExists) {
                    createUserProfile(
                        user.uid,
                        user.email ?: "",
                        user.displayName ?: "User"
                    )
                }
                Result.success(user)
            } ?: Result.failure(Exception("Google sign in gagal"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    private suspend fun createUserProfile(uid: String, email: String, name: String) {
        val user = User(
            uid = uid,
            email = email,
            name = name,
            theme = "light",
            language = "id"
        )

        database.reference
            .child("users")
            .child(uid)
            .child("profile")
            .setValue(user)
            .await()
    }

    private suspend fun checkUserProfileExists(uid: String): Boolean {
        return try {
            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .get()
                .await()

            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}