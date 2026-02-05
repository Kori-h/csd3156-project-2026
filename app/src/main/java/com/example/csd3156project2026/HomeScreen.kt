package com.example.csd3156project2026

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

data object Home

@Composable
fun MapViewComposable(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    var firestoreMarkers by remember { mutableStateOf(listOf<MarkerData>()) }

    val collectionPath = "markers"
    val defaultLocation = LatLng(1.3521, 103.8198)

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
                    anchor = Offset(0.5f, 1.0f)
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