package com.example.freepark.ui.map

import android.app.Application
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freepark.data.datasource.FirebaseAuthDataSource
import com.example.freepark.data.model.Review
import com.example.freepark.data.model.Spot
import com.example.freepark.data.repository.*
import com.example.freepark.data.service.NotificationService
import com.google.android.gms.location.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class MapUiState(
    val properties: MapProperties = MapProperties(isMyLocationEnabled = false),
    val uiSettings: MapUiSettings = MapUiSettings(myLocationButtonEnabled = true),
    val lastLocation: Location? = null,
    val spots: List<Spot> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val reviews: List<Review> = emptyList(),
)

@HiltViewModel
class MapViewModel @Inject constructor(
    application: Application,
    private val spotRepo: SpotRepo,
    private val reviewRepo: ReviewRepo,
    private val authenticateRepo: AuthenticateRepo,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState
    private val notificationService = NotificationService(application)
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null
    private val lastNotifiedMap = mutableMapOf<String, Long>()
    private val cooldownMillis = 5 * 60 * 1000L

    val allSpots: StateFlow<List<Spot>> = _uiState.map { it.spots }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            spotRepo.observeSpots().collectLatest { spots ->
                _uiState.value = _uiState.value.copy(spots = spots)
            }
        }
    }

    fun enableMyLocationLayer(enabled: Boolean) {
        val old = _uiState.value
        _uiState.value = old.copy(properties = old.properties.copy(isMyLocationEnabled = enabled))
    }

    fun startLocationUpdates() {
        if (locationCallback != null) return
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                _uiState.value = _uiState.value.copy(lastLocation = loc)
                viewModelScope.launch { checkNearby(loc) }
            }
        }
        try {
            fusedClient.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                    .setMinUpdateIntervalMillis(2000L)
                    .setMaxUpdateDelayMillis(10000L)
                    .build(),
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {}
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    fun addSpot(name: String, description: String, type: String, imageUri: Uri?) {
        viewModelScope.launch {
            val loc = _uiState.value.lastLocation ?: run {
                _uiState.value = _uiState.value.copy(message = "Current location not available.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            try {
                val photoUrl = imageUri?.let { spotRepo.uploadImage(it, "Freepark") }
                val user = auth.currentUser
                val spot = Spot(
                    id = "",
                    name = name,
                    description = description,
                    type = type,
                    photoUrl = photoUrl,
                    location = com.google.firebase.firestore.GeoPoint(loc.latitude, loc.longitude),
                    authorId = user?.uid,
                    authorName = user?.displayName ?: user?.email ?: "Unknown",
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                val result = spotRepo.addSpot(spot)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false, message = "Location added.")
                    },
                    onFailure = { ex -> _uiState.value = _uiState.value.copy(isLoading = false, message = "Error: ${ex.message}") }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Error: ${e.message}")
            }
        }
    }

    fun addReview(spotId: String, rating: Int, comment: String, imageUri: Uri?) {
        val user = auth.currentUser ?: run {
            _uiState.value = _uiState.value.copy(message = "Unregistered user")
            return
        }
        viewModelScope.launch {
            try {
                val photoUrl = imageUri?.let { reviewRepo.uploadImage(it, "Freepark") }
                val review = Review(
                    id = "",
                    spotId = spotId,
                    userId = user.uid,
                    username = user.displayName ?: user.email ?: "Unknown",
                    rating = rating,
                    comment = comment,
                    createdAt = Timestamp.now(),
                    photoUrl = photoUrl
                )
                val result = reviewRepo.addReview(review)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(message = "Review sent")
                        spotRepo.updateSpotUpdatedAt(spotId)
                        loadReviews(spotId)
                    },
                    onFailure = { ex -> _uiState.value = _uiState.value.copy(message = "Error: ${ex.message}") }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Error: ${e.message}")
            }
        }
    }



    fun loadReviews(spotId: String) {
        viewModelScope.launch {
            reviewRepo.observeReviews(spotId).collect { reviews ->
                _uiState.value = _uiState.value.copy(reviews = reviews)
            }
        }
    }

    private suspend fun checkNearby(loc: Location) {
        val radiusMeters = 100.0
        val now = System.currentTimeMillis()

        val nearbyUsers = authenticateRepo.getNearbyUsers(loc, radiusMeters)
        for (doc in nearbyUsers) {
            val uid = doc.id
            val lastNotified = lastNotifiedMap[uid] ?: 0L
            if (now - lastNotified >= cooldownMillis) {
                notificationService.showNotification("User nearby", "${doc.getString("username") ?: "Unknown"} is within range of ${radiusMeters.toInt()}m")
                lastNotifiedMap[uid] = now
            }
        }

        val allSpotsList = _uiState.value.spots
        val distanceArray = FloatArray(1)

        for (spot in allSpotsList) {
            val locSpot = spot.location ?: continue

            Location.distanceBetween(
                loc.latitude, loc.longitude,
                locSpot.latitude, locSpot.longitude,
                distanceArray
            )

            val lastNotified = lastNotifiedMap[spot.id] ?: 0L
            if (distanceArray[0] <= radiusMeters && now - lastNotified >= cooldownMillis) {
                notificationService.showNotification("Parking nearby", "${spot.name} is within range of ${radiusMeters.toInt()}m")
                lastNotifiedMap[spot.id] = now
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}