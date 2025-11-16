package com.example.freepark.data.repository

import android.net.Uri
import com.example.freepark.data.datasource.CloudinaryDataSource
import com.example.freepark.data.datasource.FirebaseSpotDataSource
import com.example.freepark.data.model.Spot
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SpotRepo @Inject constructor(
    private val dataSource: FirebaseSpotDataSource,
    private val uploader: CloudinaryDataSource,
    private val firestore: FirebaseFirestore
) {

    suspend fun addSpot(spot: Spot): Result<Unit> = suspendCancellableCoroutine { cont ->
        dataSource.addSpot(spot) { ex ->
            if (ex == null) cont.resume(Result.success(Unit))
            else cont.resume(Result.failure(ex))
        }
    }

    fun observeSpots(): Flow<List<Spot>> = dataSource.observeSpotsRealtime()

    suspend fun uploadImage(uri: Uri, folder: String = "Spots"): String? =
        suspendCancellableCoroutine { cont ->
            uploader.uploadImage(uri, folder) { url, ex ->
                if (ex == null) cont.resume(url)
                else cont.resumeWithException(ex)
            }
        }

    suspend fun updateSpotUpdatedAt(spotId: String): Result<Unit> = try {
        firestore
            .collection("spots")
            .document(spotId)
            .update("updatedAt", Timestamp.now())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}