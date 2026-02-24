package com.example.csd3156project2026

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthenticator {
    private val auth = FirebaseAuth.getInstance()

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val name = user?.displayName
                        ?: user?.email?.substringBefore("@")
                        ?: ""
                    UserSession.setDisplayName(name)
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val name = user?.displayName
                        ?: user?.email?.substringBefore("@")
                        ?: ""
                    UserSession.setDisplayName(name)
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}