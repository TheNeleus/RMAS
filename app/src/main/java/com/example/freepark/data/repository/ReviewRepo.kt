package com.example.freepark.data.repository

import android.net.Uri
import com.example.freepark.data.datasource.CloudinaryDataSource
import com.example.freepark.data.datasource.FirebaseReviewDataSource
import com.example.freepark.data.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ReviewRepo @Inject constructor(
    private val dataSource: FirebaseReviewDataSource,
    private val uploader: CloudinaryDataSource
) {

    suspend fun addReview(review: Review): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            dataSource.addReview(review) { ex ->
                if (ex == null) cont.resume(Result.success(Unit))
                else cont.resume(Result.failure(ex))
            }
        }

    fun observeReviews(spotId: String): Flow<List<Review>> =
        dataSource.observeReviewsRealtime(spotId)

    suspend fun uploadImage(uri: Uri, folder: String = "ReviewPhotos"): String? =
        suspendCancellableCoroutine { cont ->
            uploader.uploadImage(uri, folder) { url, error ->
                if (error == null && url != null) cont.resume(url)
                else cont.resumeWithException(error ?: Exception("Upload failed"))
            }
        }
}