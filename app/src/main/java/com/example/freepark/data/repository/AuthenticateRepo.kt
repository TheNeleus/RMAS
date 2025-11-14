package com.example.freepark.data.repository

import android.net.Uri
import com.example.freepark.BuildConfig
import com.example.freepark.data.datasource.FirebaseDataSource
import com.example.freepark.data.datasource.CloudinaryDataSource
import javax.inject.Inject

class AuthenticateRepo @Inject constructor(
    private val FirebaseDataSource: FirebaseDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource
) {
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
        FirebaseDataSource.checkUsernameExists(username) { exists, error ->
            if (error != null) {
                onComplete(false, error)
                return@checkUsernameExists
            }

            FirebaseDataSource.signUp(email, password) { success, error ->
                if (!success) {
                    onComplete(false, error)
                    return@signUp
                }

                val userId = FirebaseDataSource.currentUser?.uid ?: ""

                val uploadAndSave: (String?) -> Unit = { photoUrl ->
                    val userMap = mapOf(
                        "username" to username,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "phone" to phone,
                        "photo" to (photoUrl ?: "")
                    )
                    FirebaseDataSource.addUserToFirestore(userId, userMap, onComplete)
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
        FirebaseDataSource.login(email, password, onComplete)
    }

    fun logoutUser() {
        FirebaseDataSource.logout()
    }
}

