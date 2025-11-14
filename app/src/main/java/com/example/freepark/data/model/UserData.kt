package com.example.freepark.data.model

import com.google.firebase.firestore.DocumentId

data class UserData(
    @DocumentId val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val photo: String = ""
)