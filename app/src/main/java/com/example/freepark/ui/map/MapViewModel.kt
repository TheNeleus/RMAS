package com.example.freepark.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.freepark.data.repository.AuthenticateRepo
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class MapUiState(
    val properties: MapProperties = MapProperties(isMyLocationEnabled = false),
    val uiSettings: MapUiSettings = MapUiSettings(myLocationButtonEnabled = true)
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepo: AuthenticateRepo // Injektujemo repozitorijum
) : ViewModel() {
    val uiState = mutableStateOf(MapUiState())


    fun logout() {
        authRepo.logoutUser()
    }
}