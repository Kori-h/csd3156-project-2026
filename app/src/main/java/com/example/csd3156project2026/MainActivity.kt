package com.example.csd3156project2026

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch

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
        enableEdgeToEdge()

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavLogic(
                    modifier = Modifier
                )
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun NavLogic(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val onboardingPreferences = remember { OnboardingPreferences(context) }

    val hasSeenTutorial by onboardingPreferences
        .hasSeenTutorial
        .collectAsState(initial = false)

    val backStack = rememberSaveable(
        saver = listSaver(
            save = { list -> list.map { it.toString() } },
            restore = { saved ->
                mutableStateListOf(*saved.map { key ->
                    when (key) {
                        "Tutorial" -> Tutorial
                        "Login" -> Login
                        "Home" -> Home
                        "Upload" -> Upload
                        else -> Login
                    }
                }.toTypedArray())
            }
        )
    ) {
        mutableStateListOf<Any>(
            if (hasSeenTutorial) Login else Tutorial
        )
    }

    val scope = rememberCoroutineScope()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {

                is Tutorial -> NavEntry(key) {
                    TutorialScreen(
                        onFinish = {
                            scope.launch {
                                onboardingPreferences.setHasSeenTutorial(true)
                            }
                            backStack.removeLastOrNull()
                            backStack.add(Login)
                        }
                    )
                }

                is Login -> NavEntry(key) {
                    LoginScreen(
                        modifier = Modifier.padding(all = 16.dp),
                        onLoginSuccess = {
                            backStack.add(Home)
                        }
                    )
                }

                is Home -> NavEntry(key) {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        onUploadClick = {
                            backStack.add(Upload)
                        },
                        onJournalClick = {
                            backStack.add(Journal)
                        },
                        onProfileClick = {
                            backStack.add(Profile)
                        }
                    )
                }

                is Journal -> NavEntry(key) {
                    JournalScreen(
                        modifier = Modifier.fillMaxSize(),
                        onHomeClick = { backStack.removeLastOrNull() },
                        onProfileClick = { backStack.add(Profile) },
                        onUploadClick = { backStack.add(Upload) }
                    )
                }

                is Upload -> NavEntry(key) {
                    UploadScreen(
                        onClose = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                is Profile -> NavEntry(key) {
                    ProfileScreen(
                        onHomeClick = { backStack.removeLastOrNull() },
                        onJournalClick = {
                            backStack.removeLastOrNull()
                            backStack.add(Journal)
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

data object Tutorial

data object Journal

data object Profile
