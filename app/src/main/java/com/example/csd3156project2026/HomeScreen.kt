package com.example.csd3156project2026

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.io.File

data object Home

@Composable
fun MapViewComposable(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    var firestoreMarkers by remember { mutableStateOf(listOf<MarkerData>()) }

    val collectionPath = "markers"
    val defaultLocation = LatLng(1.3521, 103.8198)

    var showReviewDialog by remember { mutableStateOf(false)}
    var selectedMarker by remember { mutableStateOf<MarkerData?>(null) }


    // Function to fetch markers from Firestore
    fun fetchMarkers() {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { snapshot ->
                firestoreMarkers = snapshot.documents.map { doc ->
                    MarkerData(
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        title = doc.getString("title") ?: "",
                        snippet = doc.getString("snippet") ?: ""
                    )
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
    // Initial load (This will only call the function once)
    LaunchedEffect(Unit) {
        fetchMarkers()
    }

    // Camera focus on first marker or default
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            firestoreMarkers.firstOrNull()?.toLatLng() ?: defaultLocation,
            12f
        )
    }

    Column(modifier = modifier) {
        Button(
            onClick = { fetchMarkers() },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Refresh")
        }

        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            firestoreMarkers.forEach { marker ->
                Marker(
                    state = rememberMarkerState(position = marker.toLatLng()),
                    title = marker.title,
                    snippet = marker.snippet,
                    anchor = Offset(0.5f, 1.0f),
                    onInfoWindowClick = {
                        // Trigger the Review Dialog or Navigate to a Review Screen
                        selectedMarker = marker // Current marker that was selected
                        showReviewDialog = true // Flip the switch to show the dialog
                    }
                )
            }
        }

        // Debug Text
        firestoreMarkers.forEach { marker ->
            Text(
                text = "Title: ${marker.title}\nSnippet: ${marker.snippet}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    if (showReviewDialog && selectedMarker != null) {
        ReviewDialog(
            marker = selectedMarker!!,
            onDismiss = { showReviewDialog = false },
            onConfirm = { comment, rating ->
                // Create the review object using your data class
                val newReview = ReviewData(
                    markerId = selectedMarker!!.title,
                    userId = "Team_Member",
                    rating = rating,
                    comment = comment,
                    imageUrl = null
                )
                // Save it to Firebase using your existing function
                SaveReviewToFirebase(newReview)
            }
        )
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Welcome Home", modifier = Modifier.padding(16.dp))
        MapViewComposable(modifier = Modifier.fillMaxWidth().height(300.dp))
    }
}

fun SaveReviewToFirebase(review: ReviewData) {
    val db = FirebaseFirestore.getInstance()

    // Create a map of data to send to the cloud
    val reviewMap = hashMapOf(
        "markerId" to review.markerId,
        "userId" to review.userId,
        "rating" to review.rating,
        "comment" to review.comment,
        "imageUrl" to review.imageUrl   // Currently, cloud stores the "address" of the local file
    )

    db.collection("reviews")
        .add(reviewMap)
        .addOnSuccessListener {
            println("Review text save to cloud!")
        }
}

fun SaveImageLocally(context: Context, bitmap: Bitmap, fileName: String): String {
    val file = File(context.filesDir, "$fileName.jpg")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return file.absolutePath    // Path to send to Firebase
}

@Composable
fun ReviewDialog(
    marker: MarkerData,
    onDismiss: () -> Unit,
    onConfirm: (comment: String, rating: Int) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            modifier = Modifier.padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Review for ${marker.title}",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

                androidx.compose.material3.OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Write your review...") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )

                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { index ->
                        androidx.compose.material3.IconButton(onClick = { rating = index }) {
                            Text(
                                text = if (index <= rating) "★" else "☆",
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onConfirm(comment, rating)
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
                ) {
                    Text("Submit Review")
                }
            }
        }
    }
}