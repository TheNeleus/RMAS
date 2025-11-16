package com.example.freepark.data.repository

import android.location.Location
import android.net.Uri
import com.example.freepark.BuildConfig
import com.example.freepark.data.datasource.FirebaseAuthDataSource
import com.example.freepark.data.datasource.CloudinaryDataSource
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class AuthenticateRepo @Inject constructor(
    private val FirebaseAuthDataSource: FirebaseAuthDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource
) {
    val currentUserId: String?
        get() = FirebaseAuthDataSource.currentUser?.uid

    fun registerUser(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
        phone: String,
        photoUri: Uri?,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        FirebaseAuthDataSource.checkUsernameExists(username) { exists, error ->
            if (error != null) {
                onComplete(false, error)
                return@checkUsernameExists
            }

            if (exists) {
                onComplete(false, Exception("Username is already claimed. Try another one."))
                return@checkUsernameExists
            }

            FirebaseAuthDataSource.signUp(email, password) { success, error ->
                if (!success) {
                    onComplete(false, error)
                    return@signUp
                }

                val userId = FirebaseAuthDataSource.currentUser?.uid ?: ""

                val currentUser = FirebaseAuthDataSource.currentUser
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        println("Error while setting displayName: ${task.exception}")
                    }
                }

                val uploadAndSave: (String?) -> Unit = { photoUrl ->
                    val userMap = mapOf(
                        "username" to username,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "phone" to phone,
                        "photo" to (photoUrl ?: "")
                    )
                    FirebaseAuthDataSource.addUserToFirestore(userId, userMap, onComplete)
                }

                if (photoUri != null) {
                    cloudinaryDataSource.uploadImage(photoUri, folder = "Profiles") { url, err ->
                        if (err != null) {
                            onComplete(false, err)
                        } else {
                            uploadAndSave(url)
                        }
                    }
                } else {
                    uploadAndSave("https://res.cloudinary.com/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload/v1757901407/default-avatar_fpawnn.png")
                }
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        FirebaseAuthDataSource.login(email, password, onComplete)
    }

    fun logoutUser() {
        FirebaseAuthDataSource.logout()
    }

    suspend fun getNearbyUsers(currentLocation: Location, radiusMeters: Double): List<DocumentSnapshot> {
        val allUsers = FirebaseAuthDataSource.getAllUsers()
        val nearbyUsers = mutableListOf<DocumentSnapshot>()
        val distanceArray = FloatArray(1)

        for (doc in allUsers) {
            if (doc.id == currentUserId) continue

            val geo = doc.getGeoPoint("location") ?: continue

            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                geo.latitude, geo.longitude,
                distanceArray
            )

            if (distanceArray[0] <= radiusMeters) {
                nearbyUsers.add(doc)
            }
        }
        return nearbyUsers
    }
}

