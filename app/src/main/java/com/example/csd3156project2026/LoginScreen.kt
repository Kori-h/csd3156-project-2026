package com.example.csd3156project2026

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp

data object Login

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(value = null) }
    var isRegisterMode by rememberSaveable { mutableStateOf(value = false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to the Coffee App",
            modifier = Modifier
                .padding(bottom = 24.dp),
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(height = 16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        Spacer(modifier = Modifier.height(height = 12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(height = 16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password cannot be empty"
                    return@Button
                }

                if (isRegisterMode) {
                    FirebaseAuth.register(email, password) { success, error ->
                        if (success) {
                            onLoginSuccess()
                        } else {
                            errorMessage = error
                        }
                    }
                } else {
                    FirebaseAuth.login(email, password) { success, error ->
                        if (success) {
                            onLoginSuccess()
                        } else {
                            errorMessage = error
                        }
                    }
                }
            }
        ) {
            Text(text = if (isRegisterMode) "Register" else "Login")
        }

        Spacer(modifier = Modifier.height(height = 8.dp))

        TextButton(
            onClick = { isRegisterMode = !isRegisterMode }
        ) {
            Text(
                text = if (isRegisterMode)
                    "Already have an account? Login"
                else
                    "No account? Register"
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
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