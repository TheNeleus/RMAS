package com.example.freepark.data.datasource

import com.example.freepark.data.model.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseReviewDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun collection(spotId: String) =
        firestore.collection("spots").document(spotId).collection("reviews")

    fun addReview(review: Review, onComplete: (Exception?) -> Unit) {
        val docRef = if (review.id.isBlank()) collection(review.spotId).document() else collection(review.spotId).document(review.id)
        val map = hashMapOf<String, Any?>(
            "id" to docRef.id,
            "spotId" to review.spotId,
            "userId" to review.userId,
            "username" to review.username,
            "rating" to review.rating,
            "comment" to review.comment,
            "createdAt" to (review.createdAt ?: Timestamp.now()),
            "photoUrl" to review.photoUrl
        )
        docRef.set(map)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { ex -> onComplete(ex) }
    }

    fun observeReviewsRealtime(spotId: String) = callbackFlow<List<Review>> {
        val listener: ListenerRegistration = collection(spotId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toReview() } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toReview(): Review? {
        val id = getString("id") ?: id
        val spotId = getString("spotId") ?: return null
        val userId = getString("userId") ?: ""
        val username = getString("username") ?: "Unknown"
        val rating = getLong("rating")?.toInt() ?: 0
        val comment = getString("comment") ?: ""
        val createdAt = getTimestamp("createdAt") ?: Timestamp.now()
        val photoUrl = getString("photoUrl")
        return Review(id, spotId, userId, username, rating, comment, createdAt, photoUrl)
    }
}