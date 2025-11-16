package com.example.freepark.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp

data class User(
    @DocumentId val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val photo: String = "",
    val location: GeoPoint? = null,
    val lastLocationUpdate: Timestamp? = null
)