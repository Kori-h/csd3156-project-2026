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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.csd3156project2026.ui.theme.ButtonBrown
import com.example.csd3156project2026.ui.theme.CardBrown
import com.example.csd3156project2026.ui.theme.CreamText
import com.example.csd3156project2026.ui.theme.MainBrown
import com.example.csd3156project2026.ui.theme.NavBrown
import com.example.csd3156project2026.ui.theme.StarYellow
import com.example.csd3156project2026.ui.theme.WhiteText
import com.google.android.gms.maps.CameraUpdateFactory
import getCurrentLocation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data object Home

@Composable
fun MapViewComposable(
    modifier: Modifier = Modifier,
    onMarkerSelected: (MarkerData, List<ReviewData>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var firestoreMarkers by remember { mutableStateOf(listOf<MarkerData>()) }
    //var selectedMarker by remember { mutableStateOf<MarkerData?>(null) }
    //var selectedMarkerReviews by remember { mutableStateOf(listOf<ReviewData>()) }

    val collectionPath = "markers"
    val defaultLocation = LatLng(1.3521, 103.8198)
    var location by remember { mutableStateOf(defaultLocation) }
    val context = LocalContext.current

    suspend fun fetchGPSLocation() {
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

    //var selectedMarkerId by rememberSaveable { mutableStateOf<String?>(null) }
    //var showReviewDialog by rememberSaveable { mutableStateOf(false) }
    var reviewsLoading by rememberSaveable { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.email?.substringBefore("@") ?: "Anonymous"

    // Function to fetch reviews from Firestore
    fun fetchReviews(markerId: String, onResult: (List<ReviewData>) -> Unit) {
        reviewsLoading = true
        db.collection("reviews")
            .whereEqualTo("markerId", markerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.documents.map { doc ->
                    ReviewData(
                        markerId = doc.getString("markerId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                }
                reviewsLoading = false
                onResult(reviews)
            }
            .addOnFailureListener {
                it.printStackTrace()
                //selectedMarkerReviews = emptyList()
                reviewsLoading = false
                onResult(emptyList())
            }
    }

    // Function to fetch markers from Firestore
    suspend fun fetchMarkers() {
        try {
            val snapshot = db.collection(collectionPath).get().await()

            firestoreMarkers = snapshot.documents.map { doc ->
                MarkerData(
                    id = doc.id,
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0,
                    title = doc.getString("title") ?: "",
                    snippet = doc.getString("snippet") ?: "",
                    imageUrl = doc.getString("imageUrl")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Restore selectedMarker after rotation
    //LaunchedEffect(firestoreMarkers, selectedMarkerId) {
    //    selectedMarker = firestoreMarkers.find { it.title == selectedMarkerId }
    //}

    // Camera focus on first marker or default
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            firestoreMarkers.firstOrNull()?.toLatLng() ?: location,
            12f
        )
    }

    LaunchedEffect(context) {
        fetchMarkers()
        fetchGPSLocation()
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(location, 15f)
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            ),
            onMapClick = { latLng ->
                //selectedMarker = null
                //selectedMarkerReviews = emptyList()
            }
        ) {
            firestoreMarkers.forEach { marker ->
                val markerState = remember(marker.id) {
                    com.google.maps.android.compose.MarkerState(marker.toLatLng())
                }

                Marker(
                    state = markerState,
                    title = marker.title,
                    snippet = marker.snippet,
                    anchor = Offset(0.5f, 1.0f),
                    onClick = {
                        fetchReviews(marker.id) { reviews ->
                            onMarkerSelected(marker, reviews)
                        }
                        true
                    }
                )
            }
        }

        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch {
                    fetchMarkers()
                    fetchGPSLocation()
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(location, 15f)
                    )
                }
            },
            modifier = Modifier.align(Alignment.TopEnd)
                               .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBrown
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh",
                tint = WhiteText,
                modifier = Modifier.size(18.dp)
            )
        }

        // Debug Text
        //firestoreMarkers.forEach { marker ->
        //    Text(
        //        text = "Title: ${marker.title}\nSnippet: ${marker.snippet}",
        //        modifier = Modifier.padding(8.dp)
        //    )
        //}

//        selectedMarker?.let { marker ->
//            Column(modifier = Modifier.fillMaxWidth()
//                                      .padding(8.dp)) {
//                marker.imageUrl?.let { url ->
//                    AsyncImage(
//                        model = url,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(180.dp)
//                    )
//                }
//
//                Text(marker.title, fontSize = 18.sp)
//                Text(marker.snippet, fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
//
//                Button(
//                    onClick = { showReviewDialog = true },
//                    modifier = Modifier.padding(top = 8.dp)
//                                       .fillMaxWidth()
//                ) {
//                    Text("Add Review")
//                }
//            }
//
//            if (reviewsLoading) {
//                Text("Loading reviews...", modifier = Modifier.padding(8.dp))
//            } else if (selectedMarkerReviews.isEmpty()) {
//                Text("No reviews yet", modifier = Modifier.padding(8.dp))
//            } else {
//                LazyColumn(
//                    modifier = Modifier.fillMaxWidth()
//                                       .padding(horizontal = 8.dp)
//                                       .weight(1f)
//                ) {
//                    items(selectedMarkerReviews) { review ->
//                        androidx.compose.material3.Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 6.dp)
//                        ) {
//                            Column(modifier = Modifier.padding(12.dp)) {
//
//                                Text("User: ${review.userId}")
//                                Text("Rating: ${"★".repeat(review.rating)}")
//
//                                Spacer(modifier = Modifier.height(6.dp))
//
//                                Text(review.comment)
//
//                                // SHOW IMAGE IF EXISTS
//                                review.imageUrl?.let { url ->
//                                    Spacer(modifier = Modifier.height(8.dp))
//
//                                    AsyncImage(
//                                        model = url,
//                                        contentDescription = "Review Image",
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .height(180.dp)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    // Review Dialog
//    if (showReviewDialog && selectedMarker != null) {
//        ReviewDialog(
//            marker = selectedMarker!!,
//            onDismiss = { showReviewDialog = false },
//            onConfirm = { comment, rating, imageUrl ->
//                val newReview = ReviewData(
//                    markerId = selectedMarker!!.id,
//                    userId = userId,
//                    rating = rating,
//                    comment = comment,
//                    imageUrl = imageUrl
//                )
//                SaveReviewToFirebase(newReview)
//                fetchReviews(selectedMarker!!.id) // Refresh reviews
//            }
//        )
//    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier,
               onUploadClick: () -> Unit,
               onJournalClick: () -> Unit,
               onProfileClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = (
            UserSession.displayName.value
                ?: user?.email?.substringBefore("@")
                ?: "User"
            ).replaceFirstChar { it.uppercase() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedMarker by remember { mutableStateOf<MarkerData?>(null) }
    var selectedMarkerReviews by remember { mutableStateOf(listOf<ReviewData>()) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MainBrown,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hello $displayName 👋",
                            color = WhiteText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }
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
                    // home
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedTab = 0
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = CreamText,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Home",
                            color = CreamText,
                            fontSize = 12.sp
                        )
                    }

                    // journal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedTab = 1
                            onJournalClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = "Journal",
                            tint = CardBrown,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Reviews",
                            color = CardBrown,
                            fontSize = 12.sp
                        )
                    }

                    // profile
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            selectedTab = 2
                            onProfileClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = CardBrown,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Profile",
                            color = CardBrown,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map now takes full screen
            MapViewComposable(
                modifier = Modifier.fillMaxSize(),
                onMarkerSelected = { marker, reviews ->
                    selectedMarker = marker
                    selectedMarkerReviews = reviews
                    showBottomSheet = true
                }
            )

            // "Register a New Coffee Shop" button overlay at bottom
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(300.dp)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBrown
                )
            ) {
                Text("Register a New Coffee Shop")
            }
        }
    }

    // Bottom Sheet for selected location
    if (showBottomSheet && selectedMarker != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = CardBrown,
            contentColor = NavBrown,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(NavBrown.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
            }
        ) {
            LocationBottomSheetContent(
                marker = selectedMarker!!,
                reviews = selectedMarkerReviews,
                onAddReviewClick = {
                    showBottomSheet = false
                    showReviewDialog = true
                }
            )
        }
    }

    // Review Dialog
    if (showReviewDialog && selectedMarker != null) {
        ReviewDialog(
            marker = selectedMarker!!,
            onDismiss = { showReviewDialog = false },
            onConfirm = { comment, rating, imageUrl ->
                val userId = user?.email?.substringBefore("@") ?: "Anonymous"
                val newReview = ReviewData(
                    markerId = selectedMarker!!.id,
                    userId = userId,
                    rating = rating,
                    comment = comment,
                    imageUrl = imageUrl
                )
                SaveReviewToFirebase(newReview)
                // Refresh reviews for this marker
                val db = FirebaseFirestore.getInstance()
                db.collection("reviews")
                    .whereEqualTo("markerId", selectedMarker!!.id)
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
                    }
                showReviewDialog = false
                showBottomSheet = true
            }
        )
    }
}

@Composable
fun LocationBottomSheetContent(
    marker: MarkerData,
    reviews: List<ReviewData>,
    onAddReviewClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Location Header
        Text(
            text = marker.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavBrown
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = marker.snippet,
            fontSize = 14.sp,
            color = NavBrown.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        marker.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Location Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // avg rating
        val totalStars = reviews.sumOf { it.rating }
        val avgRating = if (reviews.isNotEmpty()) totalStars.toDouble() / reviews.size else 0.0

        Row(verticalAlignment = Alignment.CenterVertically) {
            val roundedRating = Math.round(avgRating).toInt()
            repeat(5) { index ->
                Text(
                    text = if (index < roundedRating) "★" else "☆",
                    fontSize = 16.sp,
                    color = StarYellow
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format("%.1f (${reviews.size})", avgRating),
                color = NavBrown.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // review header w add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavBrown
            )

            Button(
                onClick = onAddReviewClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBrown
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Add Review",
                    color = WhiteText,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reviews List
        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reviews yet. Be the first to review!",
                    color = WhiteText.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(review = review)
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBrown.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userId,
                    color = NavBrown,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row {
                    repeat(5) { index ->
                        Text(
                            text = if (index < review.rating) "★" else "☆",
                            fontSize = 14.sp,
                            color = StarYellow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = review.comment,
                color = NavBrown,
                fontSize = 14.sp
            )

            review.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Review Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

fun SaveMarkerToFirebase(marker: MarkerData) {
    val db = FirebaseFirestore.getInstance()

    val markerMap = hashMapOf(
        "latitude" to marker.latitude,
        "longitude" to marker.longitude,
        "title" to marker.title,
        "snippet" to marker.snippet,
        "imageUrl" to marker.imageUrl
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

    val permissionGranted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

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

    fun launchCamera() {
        val photoFile = File(
            context.getExternalFilesDir("Pictures"),
            "review_${System.currentTimeMillis()}.jpg"
        )
        photoUri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", photoFile
        )
        cameraLauncher.launch(photoUri!!)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) launchCamera()
            else errorMessage = "Camera permission denied"
        }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MainBrown)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Review: ${marker.title}",
                    color = NavBrown,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBrown)
                        .clickable(enabled = reviewImageBitmap == null) {
                            if (permissionGranted) launchCamera()
                            else cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (reviewImageBitmap == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_camera),
                                contentDescription = null,
                                tint = WhiteText.copy(alpha = 0.4f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Tap to add or take a photo",
                                color = WhiteText.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Image(
                            bitmap = reviewImageBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // X to clear
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(26.dp)
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                    androidx.compose.foundation.shape.CircleShape)
                                .clickable { reviewImageBitmap = null; photoUri = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", color = WhiteText, fontSize = 12.sp)
                        }
                    }
                }

                // camera / file buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (permissionGranted) launchCamera()
                            else cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBrown)
                    ) {
                        Text("Use Camera", color = WhiteText, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBrown)
                    ) {
                        Text("From File", color = WhiteText, fontSize = 13.sp)
                    }
                }

                // rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Rating: ",
                        color = NavBrown,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    for (i in 1..5) {
                        Text(
                            text = if (i <= rating) "★" else "☆",
                            fontSize = 26.sp,
                            color = StarYellow,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable { rating = i }
                        )
                    }
                }

                // comment
                Text("Description:", color = NavBrown, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text("Write your review...", color = WhiteText.copy(alpha = 0.4f)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CreamText,
                        unfocusedBorderColor = CardBrown,
                        focusedContainerColor = CardBrown,
                        unfocusedContainerColor = CardBrown,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        cursorColor = WhiteText
                    ),
                    maxLines = 5
                )

                errorMessage?.let {
                    Text(it, color = androidx.compose.ui.graphics.Color.Red, fontSize = 12.sp)
                }

                // submit
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
                    modifier = Modifier.align(Alignment.End),
                    enabled = !uploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonBrown,
                        disabledContainerColor = ButtonBrown.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (uploading) "Uploading..." else "Submit",
                        color = WhiteText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}