package com.example.freepark.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    navController: NavController,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: MapViewModel = hiltViewModel()
) {
    val primaryColor = Color.Black

    val elfak = LatLng(43.331251, 21.892423)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(elfak, 15f)
    }

    val uiState = viewModel.uiState.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = uiState.properties,
            uiSettings = uiState.uiSettings,
        ) {
            // Marker i druge stvari idu ovde
        }

        Button(
            onClick = {
                viewModel.logout()
                navController.navigate("signin") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Logout", color = Color.White) // NOVO: Tekst BEO
        }
    }
}