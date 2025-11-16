package com.example.freepark.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.freepark.data.model.Spot
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val allSpots by viewModel.allSpots.collectAsState()

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var reviewImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showSpotsList by remember { mutableStateOf(false) }
    var selectedSpot by remember { mutableStateOf<Spot?>(null) }

    val LightCardBackground = Color(0xFFEFEFEF) // Vrlo svetlo siva
    val PrimaryColor = Color.Black

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> reviewImageUri = uri }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) reviewImageUri = tempCameraUri }

    fun createImageUri(context: Context): Uri {
        val imageFile = File.createTempFile("review_", ".jpg", context.cacheDir).apply { deleteOnExit() }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val startPosition = LatLng(43.3247, 21.9033)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, 15f)
    }

    var hasCenteredOnUser by remember { mutableStateOf(false) }


    LaunchedEffect(uiState.lastLocation) {
        val loc = uiState.lastLocation ?: return@LaunchedEffect
        if (!hasCenteredOnUser) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.latitude, loc.longitude),
                    15f
                )
            )
            hasCenteredOnUser = true
        }
    }

    var hasFine by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val grantedFine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val grantedCoarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasFine = grantedFine || grantedCoarse
        if (hasFine) {
            viewModel.enableMyLocationLayer(true)
            viewModel.startLocationUpdates()
        } else {
            viewModel.enableMyLocationLayer(false)
            viewModel.stopLocationUpdates()
        }
    }
    LaunchedEffect(Unit) {
        if (!hasFine) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.enableMyLocationLayer(true)
            viewModel.startLocationUpdates()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = uiState.properties,
            uiSettings = uiState.uiSettings
        ) {
            allSpots.forEach { spot ->
                spot.location?.let { gp ->
                    Marker(
                        state = MarkerState(position = LatLng(gp.latitude, gp.longitude)),
                        title = spot.name,
                        snippet = spot.type,
                        onClick = {
                            selectedSpot = spot
                            viewModel.loadReviews(spot.id)
                            true
                        }
                    )
                }
            }
        }

        selectedSpot?.let { spot ->
            ModalBottomSheet(onDismissRequest = { selectedSpot = null }) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(spot.name, style = MaterialTheme.typography.titleLarge)
                    Text(spot.type, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(spot.description, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))

                    spot.photoUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Author: ${spot.authorName ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))

                    Text("Add a review", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightCardBackground),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            RatingBarInput(
                                rating = rating.coerceAtLeast(1),
                                onRatingChanged = { rating = it.coerceAtLeast(1) }
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                label = { Text("Comment") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color(0xFFCCCCCC),
                                    focusedLabelColor = PrimaryColor,
                                    unfocusedLabelColor = Color(0xFF888888),
                                    cursorColor = PrimaryColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                maxLines = 6,
                                minLines = 4
                            )

                            Spacer(Modifier.height(12.dp))

                            ReviewImagePicker(
                                photoUri = reviewImageUri,
                                onRemove = { reviewImageUri = null },
                                pickGallery = {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                takePhoto = {
                                    val uri = createImageUri(context)
                                    tempCameraUri = uri
                                    takePicture.launch(uri)
                                }
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.addReview(spot.id, rating, comment, reviewImageUri)
                                    rating = 1
                                    comment = ""
                                    reviewImageUri = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryColor,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Send a review")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Spacer(Modifier.height(8.dp))

                    Text("Reviews", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    if (uiState.reviews.isEmpty()) {
                        Text("No reviews", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.reviews.forEach { review ->
                                ReviewItem(review)
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }


        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))

        var menuExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Menu") }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Add Spot") }, onClick = {
                    menuExpanded = false
                    showAddDialog = true
                })
                DropdownMenuItem(text = { Text("Registered locations") }, onClick = {
                    menuExpanded = false
                    showSpotsList = true
                })
            }
        }

        if (showSpotsList) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.5f).align(Alignment.Center),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Registered locations", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showSpotsList = false }) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                    HorizontalDivider()
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(allSpots) { spot ->
                            SpotListItem(spot) {
                                spot.location?.let { gp ->
                                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(gp.latitude, gp.longitude), 17f))
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSpot(
                onDismiss = { showAddDialog = false },
                onSubmit = { name, description, type, imageUri ->
                    viewModel.addSpot(name, description, type, imageUri)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ReviewImagePicker(
    photoUri: Uri?,
    onRemove: () -> Unit,
    pickGallery: () -> Unit,
    takePhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "Review photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .offset(6.dp, (-8).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = pickGallery,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color.Black)
            ) { Text("Gallery") }

            OutlinedButton(
                onClick = takePhoto,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color.Black)
            ) { Text("Camera") }
        }
    }
}