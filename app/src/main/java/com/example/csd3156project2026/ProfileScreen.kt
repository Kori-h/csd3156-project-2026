package com.example.csd3156project2026

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csd3156project2026.ui.theme.MainBrown
import com.example.csd3156project2026.ui.theme.NavBrown
import com.example.csd3156project2026.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onSignOut: () -> Unit
) {
    // Simple print to logcat to verify it's working
    println("👤 PROFILE SCREEN: Successfully loaded!")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MainBrown,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = WhiteText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = WhiteText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavBrown
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👤",
                    fontSize = 60.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Profile Screen",
                    fontSize = 24.sp,
                    color = WhiteText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This is just a placeholder",
                    fontSize = 16.sp,
                    color = WhiteText.copy(alpha = 0.7f)
                )
            }
        }
    }
}
