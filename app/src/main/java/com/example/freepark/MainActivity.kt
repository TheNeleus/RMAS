package com.example.freepark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph
import com.example.freepark.ui.navigation.NavGraph
import com.example.freepark.ui.theme.FreeParkTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.freepark.ui.signup.SignUpScreen

@AndroidEntryPoint //  generates an individual Hilt component for each Android class in your project
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeParkTheme {
                NavGraph()
            }
        }
    }
}