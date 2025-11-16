package com.example.freepark.data.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val spotId: String = "",
    val userId: String = "",
    val username: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val photoUrl: String? = null
)