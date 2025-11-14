package com.example.freepark.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
// OBAVEZNO OVI UVOZI
import com.example.freepark.ui.signup.CustomPasswordTextField
import com.example.freepark.ui.signup.CustomTextField

// Composable
@Composable
fun SignInScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val username by viewModel.username
    val password by viewModel.password
    val showPassword by viewModel.showPassword
    val loginSuccess by viewModel.loginSuccess
    val loginError by viewModel.loginError

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )

                CustomTextField(
                    value = username,
                    onValueChange = { viewModel.username.value = it },
                    label = "Username",
                    primaryColor = primaryColor // Koristi primaryColor (Crna)
                )

                CustomPasswordTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    label = "Password",
                    showPassword = showPassword,
                    onTogglePassword = { viewModel.showPassword.value = !showPassword },
                    isError = false,
                    primaryColor = primaryColor // Koristi primaryColor (Crna)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Login", color = Color.White)
                }

                loginSuccess?.let { success ->
                    if (success) {
                        LaunchedEffect(Unit) {
                            navController.navigate("map")
                        }
                    }
                    else Text("Error: $loginError", color = MaterialTheme.colorScheme.error)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Don't have an account?", color = secondaryColor)
                    Text(
                        text = "Sign Up",
                        color = primaryColor,
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable { navController.navigate("signup") }
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}