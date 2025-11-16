package com.example.freepark.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Spot(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val photoUrl: String? = null,
    val location: GeoPoint? = null,
    val authorId: String? = null,
    val authorName: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)