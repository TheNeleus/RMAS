package com.example.freepark.data.datasource

import com.example.freepark.data.model.Spot
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirebaseSpotDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("spots")

    fun addSpot(spot: Spot, onComplete: (Exception?) -> Unit) {
        val docRef = if (spot.id.isBlank()) collection.document() else collection.document(spot.id)
        val map = hashMapOf<String, Any?>(
            "id" to docRef.id,
            "name" to spot.name,
            "description" to spot.description,
            "type" to spot.type,
            "photoUrl" to spot.photoUrl,
            "location" to spot.location,
            "authorId" to spot.authorId,
            "authorName" to spot.authorName,
            "createdAt" to spot.createdAt,
            "updatedAt" to spot.updatedAt,
        )
        docRef.set(map)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { ex -> onComplete(ex) }
    }


    fun observeSpotsRealtime() = callbackFlow<List<Spot>> {
        val listener: ListenerRegistration = collection
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toSpot() } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toSpot(): Spot? {
        val id = getString("id") ?: id
        val name = getString("name") ?: ""
        val description = getString("description") ?: ""
        val type = getString("type") ?: ""
        val photoUrl = getString("photoUrl")
        val location = getGeoPoint("location")
        val authorId = getString("authorId")
        val authorName = getString("authorName")
        val createdAt = getTimestamp("createdAt")
        val updatedAt = getTimestamp("updatedAt")
        return Spot(id, name, description, type, photoUrl, location, authorId, authorName, createdAt, updatedAt)
    }

}