package com.example.freepark.ui.map

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpot(
    onDismiss: () -> Unit,
    onSubmit: (name: String, description: String, type: String, imageUri: Uri) -> Unit,
    defaultType: String = "Street Parking"
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(defaultType) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("Street Parking", "Free Street Parking", "Public Parking", "Private Parking",
        "Disabled Parking")

    val pickGallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) imageUri = uri
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = tempCameraUri
    }

    fun createImageUri(context: Context): Uri {
        val imageFile = File.createTempFile("spot_", ".jpg", context.cacheDir).apply { deleteOnExit() }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    val primaryColor = Color.Black
    val secondaryColor = Color(0xFF555555)
    val dialogBackgroundColor = Color.White
    val dialogContentColor = Color.Black

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add parking spot", color = dialogContentColor) },
        containerColor = dialogBackgroundColor,
        textContentColor = dialogContentColor,

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = secondaryColor,
                    cursorColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = secondaryColor,
                    focusedTextColor = dialogContentColor,
                    unfocusedTextColor = dialogContentColor,
                    focusedContainerColor = dialogBackgroundColor,
                    unfocusedContainerColor = dialogBackgroundColor
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = secondaryColor) },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = secondaryColor) },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        label = { Text("Type of parking", color = secondaryColor) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = textFieldColors,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(dialogBackgroundColor)
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t, color = dialogContentColor) },
                                onClick = {
                                    type = t
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "chosen photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                            IconButton(
                                onClick = { imageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.Black)
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val buttonColors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)

                        OutlinedButton(
                            onClick = { pickGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = buttonColors
                        ) {
                            Text("From gallery")
                        }
                        OutlinedButton(
                            onClick = {
                                val uri = createImageUri(context)
                                tempCameraUri = uri
                                takePhoto.launch(uri)
                            },
                            colors = buttonColors
                        ) {
                            Text("Take a photo")
                        }
                    }
                    if (imageUri == null) {
                        Text(
                            "Picture is required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && imageUri != null) {
                        onSubmit(name, description, type, imageUri!!)
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = secondaryColor)
            ) { Text("Cancel") }
        }
    )
}