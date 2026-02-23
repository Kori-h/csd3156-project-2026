package com.example.csd3156project2026

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.csd3156project2026.ui.theme.*
import com.example.csd3156project2026.ui.theme.ButtonBrown
import com.example.csd3156project2026.ui.theme.CardBrown
import com.example.csd3156project2026.ui.theme.CreamText
import com.example.csd3156project2026.ui.theme.MainBrown
import com.example.csd3156project2026.ui.theme.NavBrown
import com.example.csd3156project2026.ui.theme.StarYellow
import com.example.csd3156project2026.ui.theme.WhiteText
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onJournalClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    val email = user?.email ?: "Guest"

    var username by remember {
        mutableStateOf(
            UserSession.displayName.value
                ?: user?.email?.substringBefore("@")
                ?: ""
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    // Fetch username from Firestore
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MainBrown,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = WhiteText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavBrown,
                    titleContentColor = WhiteText
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = NavBrown,
                contentColor = WhiteText
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onHomeClick() }
                    ) {
                        Icon(Icons.Filled.Home, "Home", tint = CardBrown)
                        Text("Home", color = CardBrown, fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onJournalClick() }
                    ) {
                        Icon(Icons.Filled.List, "Journal", tint = CardBrown)
                        Text("Journal", color = CardBrown, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Person, "Profile", tint = CreamText)
                        Text("Profile", color = CreamText, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MainBrown),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CreamText)
            }
        } else {

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
                        focusedContainerColor = CardBrown,
                        unfocusedContainerColor = CardBrown,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        cursorColor = WhiteText
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        UserSession.displayName.value =
                            if (username.isBlank())
                                user?.email?.substringBefore("@")
                            else
                                username
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonBrown),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes", color = WhiteText)
                }
            }
        }
    }
}