package com.example.csd3156project2026

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.example.csd3156project2026.ui.theme.ButtonBrown
import com.example.csd3156project2026.ui.theme.WhiteText
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(value = null) }
    var isRegisterMode by rememberSaveable { mutableStateOf(value = false) }
    var isProcessing by remember { mutableStateOf(false) }
    val isDebugMode = true

    LaunchedEffect(isDebugMode) {
        if (isDebugMode) {
            email = "admin@admin.com"
            password = "admin123"
        }

        if (FirebaseAuthenticator.isLoggedIn()) {
            FirebaseAuth.getInstance().signOut()
            UserSession.setDisplayName("")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome",
                modifier = Modifier
                    .padding(bottom = 24.dp),
                fontSize = 28.sp,
                color = WhiteText
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email", color = WhiteText) },
                textStyle = androidx.compose.ui.text.TextStyle(color = WhiteText),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhiteText,
                    unfocusedBorderColor = WhiteText.copy(alpha = 0.5f),
                    focusedLabelColor = WhiteText,
                    unfocusedLabelColor = WhiteText.copy(alpha = 0.7f),
                    cursorColor = WhiteText
                )
            )

            Spacer(modifier = Modifier.height(height = 12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password", color = WhiteText) },
                textStyle = androidx.compose.ui.text.TextStyle(color = WhiteText),
                visualTransformation = PasswordVisualTransformation(),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhiteText,
                    unfocusedBorderColor = WhiteText.copy(alpha = 0.5f),
                    focusedLabelColor = WhiteText,
                    unfocusedLabelColor = WhiteText.copy(alpha = 0.7f),
                    cursorColor = WhiteText
                )
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            Button(
                onClick = {
                    if (isProcessing) return@Button

                    isProcessing = true
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and password cannot be empty"
                        return@Button
                    }

                    if (isRegisterMode) {
                        FirebaseAuthenticator.register(email, password) { success, error ->
                            if (success) {
                                errorMessage = null
                                onLoginSuccess()
                            } else {
                                errorMessage = error
                                isProcessing = false
                            }
                        }
                    } else {
                        FirebaseAuthenticator.login(email, password) { success, error ->
                            if (success) {
                                errorMessage = null
                                onLoginSuccess()
                            } else {
                                errorMessage = error
                                isProcessing = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBrown,
                    contentColor = WhiteText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = if (isRegisterMode) "Register" else "Login")
                }
            }

            Spacer(modifier = Modifier.height(height = 8.dp))

            TextButton(
                onClick = { isRegisterMode = !isRegisterMode },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = WhiteText
                )
            ) {
                Text(
                    text = if (isRegisterMode)
                        "Already have an account? Login"
                    else
                        "No account? Register"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red
                    )
                }
            }
        }
    }
}