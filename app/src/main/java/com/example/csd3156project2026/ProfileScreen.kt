package com.example.csd3156project2026

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.example.csd3156project2026.ui.theme.ButtonBrown
import com.example.csd3156project2026.ui.theme.CardBrown
import com.example.csd3156project2026.ui.theme.CreamText
import com.example.csd3156project2026.ui.theme.MainBrown
import com.example.csd3156project2026.ui.theme.WhiteText
import com.google.firebase.auth.UserProfileChangeRequest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onJournalClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "Guest"

    var username by remember {
        mutableStateOf(
            user?.displayName ?: user?.email?.substringBefore("@") ?: ""
        )
    }

    var isSaving by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MainBrown,
        topBar = { AppTopBar("Profile") },
        bottomBar = {
            AppBottomBar(
                currentRoute = "profile",
                onHomeClick = onHomeClick,
                onJournalClick = onJournalClick,
                onProfileClick = {}
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = CreamText,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username (Editable)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CardBrown,
                    unfocusedBorderColor = CardBrown,
                    focusedLabelColor = WhiteText,
                    unfocusedLabelColor = WhiteText,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    cursorColor = WhiteText,
                    disabledLabelColor = WhiteText,
                    disabledTextColor = WhiteText,
                    disabledBorderColor = CardBrown
                ),
                textStyle = androidx.compose.ui.text.TextStyle(color = WhiteText)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updatedUsername = username.ifBlank { email.substringBefore("@") }
                    user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(updatedUsername)
                            .build()
                    )?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            UserSession.setDisplayName(updatedUsername)
                            showSavedMessage = true
                        }
                        isSaving = false
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBrown),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes", color = WhiteText)
                }
            }

            if (showSavedMessage) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Changes saved!",
                    color = CreamText,
                    fontSize = 14.sp
                )

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    showSavedMessage = false
                }
            }
        }
    }
}