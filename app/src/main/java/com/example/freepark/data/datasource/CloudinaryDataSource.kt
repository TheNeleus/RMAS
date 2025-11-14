package com.example.freepark.data.datasource

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import javax.inject.Inject

class CloudinaryDataSource @Inject constructor() {

    /**
     * @param uri - Uri slike koja se uploaduje
     * @param folder - folder na Cloudinary gde zelimo da se slika smesti
     * @param onResult - callback sa URL-om ili Exception
     */
    fun uploadImage(
        uri: Uri,
        folder: String = "Profiles",
        onResult: (String?, Exception?) -> Unit
    ) {
        MediaManager.get().upload(uri)
            .option("folder", folder)  // definisanje foldera
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    onResult(url, null)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onResult(null, Exception(error?.description))
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    onResult(null, Exception(error?.description))
                }
            })
            .dispatch()
    }
}