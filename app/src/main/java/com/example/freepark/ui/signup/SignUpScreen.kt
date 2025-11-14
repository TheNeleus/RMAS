package com.example.freepark.ui.signup

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.freepark.R
import java.io.File

@Composable
fun SignUpScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val username by viewModel.username
    val password by viewModel.password
    val firstName by viewModel.firstName
    val lastName by viewModel.lastName
    val phoneNumber by viewModel.phoneNumber
    val photoUri by viewModel.photoUri
    val showPassword by viewModel.showPassword
    val passwordError by viewModel.passwordError

    val signUpSuccess by viewModel.signUpSuccess
    val signUpError by viewModel.signUpError

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.photoUri.value = uri }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) viewModel.photoUri.value = tempCameraUri }

    fun createImageUri(context: Context): Uri {
        val imageFile = File.createTempFile("profile_", ".jpg", context.cacheDir).apply { deleteOnExit() }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    }

    val customBackgroundColor = Color(0xFFFAFAFA)
    val formCardColor = Color.White
    val primaryColor = Color.Black
    val secondaryColor = Color(0xFF555555)


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(customBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = formCardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )

                AvatarPicker(
                    photoUri = photoUri,
                    onRemove = { viewModel.photoUri.value = null },
                    pickGallery = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    takePhoto = {
                        val uri = createImageUri(context)
                        tempCameraUri = uri
                        takePicture.launch(uri)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = username,
                    onValueChange = { viewModel.username.value = it },
                    label = "Username",
                    primaryColor = primaryColor
                )

                CustomPasswordTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    label = "Password",
                    showPassword = showPassword,
                    onTogglePassword = { viewModel.showPassword.value = !showPassword },
                    isError = passwordError != null,
                    primaryColor = primaryColor
                )

                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-10).dp)
                            .padding(start = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomTextField(
                        value = firstName,
                        onValueChange = { viewModel.firstName.value = it },
                        label = "First Name",
                        modifier = Modifier.weight(1f),
                        primaryColor = primaryColor
                    )
                    CustomTextField(
                        value = lastName,
                        onValueChange = { viewModel.lastName.value = it },
                        label = "Last Name",
                        modifier = Modifier.weight(1f),
                        primaryColor = primaryColor
                    )
                }

                val numericRegex = Regex("[^0-9]")
                CustomTextField(
                    value = phoneNumber,
                    onValueChange = {
                        val stripped = numericRegex.replace(it, "")
                        viewModel.phoneNumber.value =
                            if (stripped.length >= 10) stripped.substring(0..9) else stripped
                    },
                    label = "Enter Phone Number",
                    visualTransformation = SerbianVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    primaryColor = primaryColor
                )

                Button(
                    onClick = { viewModel.register() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Sign Up", color = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Already have an account?", color = secondaryColor)
                    Text(
                        text = "Sign In",
                        color = primaryColor,
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable { navController.navigate("signin") }
                            .padding(top = 4.dp)
                    )
                }

                signUpSuccess?.let { success ->
                    if (success) Text("Sign up successful!", color = primaryColor)
                    else Text("Error: $signUpError", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// AŽURIRANA POMOĆNA KOMPONENTA: Tekst unutar polja je sada CRN
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    primaryColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8F8F8),
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primaryColor,
            unfocusedLabelColor = Color.Gray,
            // NOVO: Boja teksta unutar polja je CRNA
            focusedTextColor = primaryColor,
            unfocusedTextColor = primaryColor,
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
    )
}

// AŽURIRANA POMOĆNA KOMPONENTA: Tekst unutar polja je sada CRN
@Composable
fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    primaryColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8F8F8),
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primaryColor,
            unfocusedLabelColor = Color.Gray,
            // NOVO: Boja teksta unutar polja je CRNA
            focusedTextColor = primaryColor,
            unfocusedTextColor = primaryColor,
        ),
        trailingIcon = {
            val icon = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = onTogglePassword) {
                Icon(icon, contentDescription = null, tint = Color.Gray)
            }
        }
    )
}

@Composable
fun AvatarPicker(
    photoUri: Uri?,
    onRemove: () -> Unit,
    pickGallery: () -> Unit,
    takePhoto: () -> Unit
) {
    val primaryColor = Color.Black
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            val painter = if (photoUri != null) {
                rememberAsyncImagePainter(photoUri)
            } else {
                painterResource(id = R.drawable.ic_default_avatar)
            }

            Image(
                painter = painter,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            if (photoUri != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .offset(6.dp, (-6).dp)
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = pickGallery) { Text("From Gallery", color = primaryColor) }
            OutlinedButton(onClick = takePhoto) { Text("Take Photo", color = primaryColor) }
        }
    }
}

class SerbianVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 10) text.text.substring(0..9) else text.text
        var out = ""
        for (i in trimmed.indices) {
            if (i == 3 || i == 6) out += " "
            out += trimmed[i]
        }
        return TransformedText(AnnotatedString(out), serbianOffsetTranslator)
    }

    private val serbianOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = when {
            offset <= 3 -> offset
            offset <= 6 -> offset + 1
            else -> offset + 2
        }

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= 3 -> offset
            offset <= 7 -> offset - 1
            else -> offset - 2
        }
    }
}