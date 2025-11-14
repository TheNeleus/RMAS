package com.example.freepark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freepark.ui.map.MapScreen
import com.example.freepark.ui.signin.SignInScreen
import com.example.freepark.ui.signup.SignUpScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "signin"
    ) {
        composable("signin") {
            SignInScreen(
                navController = navController
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
            )
        }

        composable("map") {
            MapScreen(
                navController = navController
            )
        }
    }
}