package com.example.freepark.data.datasource


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    fun signUp(
        email: String,
        password: String,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception)
                }
            }
    }

    fun addUserToFirestore(
        userId: String,
        userMap: Map<String, Any>,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception)
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception)
                }
            }
    }

    fun checkUsernameExists(username: String, onResult: (Boolean, Exception?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(!snapshot.isEmpty, null) // true if exists already
            }
            .addOnFailureListener { e ->
                onResult(false, e)
            }
    }

    fun logout() {
        auth.signOut()
    }

}