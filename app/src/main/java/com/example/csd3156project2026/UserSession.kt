package com.example.csd3156project2026

import androidx.compose.runtime.State
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.mutableStateOf

object UserSession {
    private val _displayName = mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName ?: "")
    val displayName: State<String> = _displayName

    fun setDisplayName(newName: String) {
        _displayName.value = newName
    }
}