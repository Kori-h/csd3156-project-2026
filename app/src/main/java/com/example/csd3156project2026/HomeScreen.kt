package com.example.csd3156project2026

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.content.pm.PackageManager
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import getCurrentLocation

data object Home

@Composable
fun MapViewComposable(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()

    var firestoreMarkers by remember { mutableStateOf(listOf<MarkerData>()) }
    var selectedMarker by remember { mutableStateOf<MarkerData?>(null) }
    var selectedMarkerReviews by remember { mutableStateOf(listOf<ReviewData>()) }

    val collectionPath = "markers"
    val defaultLocation = LatLng(1.3521, 103.8198)
    var location by remember { mutableStateOf(defaultLocation) }
    val context = LocalContext.current

    LaunchedEffect(context) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            try {
                location = getCurrentLocation(context) ?: defaultLocation
            } catch (e: SecurityException) {
                location = defaultLocation
            }
        } else {
            location = defaultLocation
        }
    }

    var selectedMarkerId by rememberSaveable { mutableStateOf<String?>(null) }
    var showReviewDialog by rememberSaveable { mutableStateOf(false) }
    var reviewsLoading by rememberSaveable { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.email?.substringBefore("@") ?: "Anonymous"

    // Function to fetch reviews from Firestore
    fun fetchReviews(markerId: String) {
        reviewsLoading = true
        db.collection("reviews")
            .whereEqualTo("markerId", markerId)
            .get()
            .addOnSuccessListener { snapshot ->
                selectedMarkerReviews = snapshot.documents.map { doc ->
                    ReviewData(
                        markerId = doc.getString("markerId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                }
                reviewsLoading = false
            }
            .addOnFailureListener {
                it.printStackTrace()
                selectedMarkerReviews = emptyList()
                reviewsLoading = false
            }
    }

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
                        snippet = doc.getString("snippet") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    // Fetch markers once
    LaunchedEffect(Unit) {
        fetchMarkers()
    }

    // Restore selectedMarker after rotation
    LaunchedEffect(firestoreMarkers, selectedMarkerId) {
        selectedMarker = firestoreMarkers.find { it.title == selectedMarkerId }
    }

    // Camera focus on first marker or default
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            firestoreMarkers.firstOrNull()?.toLatLng() ?: location,
            12f
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        //Button(
        //    onClick = { fetchMarkers() },
        //    modifier = Modifier.padding(8.dp)
        //) {
        //    Text("Refresh")
        //}

        GoogleMap(
            modifier = Modifier.fillMaxWidth()
                               .height(300.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            ),
            onMapClick = { latLng ->
                selectedMarker = null
                selectedMarkerReviews = emptyList()
            }
        ) {
            firestoreMarkers.forEach { marker ->
                val markerState = rememberMarkerState(
                    position = marker.toLatLng()
                )
                Marker(
                    state = markerState,
                    title = marker.title,
                    snippet = marker.snippet,
                    anchor = Offset(0.5f, 1.0f),
                    onClick = {
                        selectedMarker = marker
                        fetchReviews(marker.title)
                        false
                    }
                )
            }
        }

        // Debug Text
        //firestoreMarkers.forEach { marker ->
        //    Text(
        //        text = "Title: ${marker.title}\nSnippet: ${marker.snippet}",
        //        modifier = Modifier.padding(8.dp)
        //    )
        //}

        selectedMarker?.let { marker ->
            Column(modifier = Modifier.fillMaxWidth()
                                      .padding(8.dp)) {
                marker.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }

                Text(marker.title, fontSize = 18.sp)
                Text(marker.snippet, fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)

                Button(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                                       .fillMaxWidth()
                ) {
                    Text("Add Review")
                }
            }

            if (reviewsLoading) {
                Text("Loading reviews...", modifier = Modifier.padding(8.dp))
            } else if (selectedMarkerReviews.isEmpty()) {
                Text("No reviews yet", modifier = Modifier.padding(8.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                                       .padding(horizontal = 8.dp)
                                       .weight(1f)
                ) {
                    items(selectedMarkerReviews) { review ->
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {

                                Text("User: ${review.userId}")
                                Text("Rating: ${"★".repeat(review.rating)}")

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(review.comment)

                                // SHOW IMAGE IF EXISTS
                                review.imageUrl?.let { url ->
                                    Spacer(modifier = Modifier.height(8.dp))

                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Review Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Review Dialog
    if (showReviewDialog && selectedMarker != null) {
        ReviewDialog(
            marker = selectedMarker!!,
            onDismiss = { showReviewDialog = false },
            onConfirm = { comment, rating, imageUrl ->
                val newReview = ReviewData(
                    markerId = selectedMarker!!.title,
                    userId = userId,
                    rating = rating,
                    comment = comment,
                    imageUrl = imageUrl
                )
                SaveReviewToFirebase(newReview)
                fetchReviews(selectedMarker!!.title) // Refresh reviews
            }
        )
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier,
               onUploadClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.email?.substringBefore(delimiter = "@") ?: "User"

    Column(
        modifier = modifier.fillMaxSize()
                           .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Hello $displayName 👋",
            fontSize = 22.sp,
            modifier = Modifier.padding(16.dp)
        )

        MapViewComposable(modifier = Modifier.fillMaxWidth()
                                             .weight(1.0f))

        Button(
            onClick = onUploadClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Register a New Coffee Shop")
        }
    }
}

fun SaveMarkerToFirebase(marker: MarkerData) {
    val db = FirebaseFirestore.getInstance()

    val markerMap = hashMapOf(
        "latitude" to marker.latitude,
        "longitude" to marker.longitude,
        "title" to marker.title,
        "snippet" to marker.snippet
    )

    db.collection("markers")
        .add(markerMap)
        .addOnSuccessListener {
            println("A new coffee marker saved to cloud!")
        }
        .addOnFailureListener { e ->
            println("Error saving marker: $e")
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

//@Composable
//fun ReviewDialog(
//    marker: MarkerData,
//    onDismiss: () -> Unit,
//    onConfirm: (comment: String, rating: Int) -> Unit
//) {
//    var comment by remember { mutableStateOf("") }
//    var rating by remember { mutableStateOf(5) }
//
//    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
//        androidx.compose.material3.Card(
//            modifier = Modifier.padding(16.dp),
//            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(
//                    text = "Review for ${marker.title}",
//                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
//
//                androidx.compose.material3.OutlinedTextField(
//                    value = comment,
//                    onValueChange = { comment = it },
//                    label = { Text("Write your review...") },
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
//                )
//
//                androidx.compose.foundation.layout.Row(
//                    horizontalArrangement = Arrangement.Center,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    (1..5).forEach { index ->
//                        androidx.compose.material3.IconButton(onClick = { rating = index }) {
//                            Text(
//                                text = if (index <= rating) "★" else "☆",
//                                fontSize = 24.sp
//                            )
//                        }
//                    }
//                }
//
//                Button(
//                    onClick = {
//                        onConfirm(comment, rating)
//                        onDismiss()
//                    },
//                    modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
//                ) {
//                    Text("Submit Review")
//                }
//            }
//        }
//    }
//}

@Composable
fun ReviewDialog(
    marker: MarkerData,
    onDismiss: () -> Unit,
    onConfirm: (comment: String, rating: Int, imageUrl: String?) -> Unit
) {
    val context = LocalContext.current

    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var reviewImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var uploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // CAMERA
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                val stream = context.contentResolver.openInputStream(photoUri!!)
                reviewImageBitmap = BitmapFactory.decodeStream(stream)
                errorMessage = null
            } else {
                errorMessage = "File not supported"
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted && photoUri != null) {
                cameraLauncher.launch(photoUri!!)
            } else {
                errorMessage = "Camera permission denied"
            }
        }

    // GALLERY
    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType?.startsWith("image") == true) {
                    val stream = context.contentResolver.openInputStream(uri)
                    reviewImageBitmap = BitmapFactory.decodeStream(stream)
                    errorMessage = null
                } else {
                    errorMessage = "File not supported"
                }
            }
        }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            modifier = Modifier.padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Review for ${marker.title}",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Write your review...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating
                Row(horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()) {

                    for (i in 1..5) {
                        Text(
                            text = "★",
                            fontSize = 26.sp,
                            color = if (i <= rating)
                                androidx.compose.ui.graphics.Color(0xFFFFC107)
                            else
                                androidx.compose.ui.graphics.Color.Gray,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { rating = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 🖼 Image Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            androidx.compose.ui.graphics.Color.LightGray,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (reviewImageBitmap == null) {
                        Text("No Image Selected")
                    } else {
                        Image(
                            bitmap = reviewImageBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {

                            val photoFile = File(
                                context.getExternalFilesDir("Pictures"),
                                "review_${System.currentTimeMillis()}.jpg"
                            )

                            photoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                photoFile
                            )

                            cameraLauncher.launch(photoUri!!)
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Use Camera")
                    }

                    Button(onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Text("From File")
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it, color = androidx.compose.ui.graphics.Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit
                Button(
                    onClick = {
                        uploading = true

                        if (reviewImageBitmap != null) {
                            uploadToCloudinary(reviewImageBitmap!!) { url ->
                                onConfirm(comment, rating, url)
                                uploading = false
                                onDismiss()
                            }
                        } else {
                            onConfirm(comment, rating, null)
                            uploading = false
                            onDismiss()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (uploading) "Uploading..." else "Submit Review")
                }
            }
        }
    }
}