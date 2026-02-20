package com.example.csd3156project2026

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            permissionGranted = isGranted
        }

    private var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavLogic(
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
        }
    }
}

@Composable
fun NavLogic(modifier: Modifier = Modifier) {
    val backStack = remember { mutableStateListOf<Any>(Login) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is Login -> NavEntry(key) {
                    LoginScreen(
                        modifier,
                        onLogin = {
                            backStack.add(Home)
                        }
                    )
                }

                is Home -> NavEntry(key) {
                    HomeScreen(
                        modifier,
                        onUploadClick = {
                            backStack.add(Upload)
                        }
                    )
                }

                is Upload -> NavEntry(key) {
                    UploadScreen(
                        onClose = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                else -> NavEntry(key = Unit) {
                    Text(text = "Unknown route")
                }
            }
        }
    )
}

data object Upload