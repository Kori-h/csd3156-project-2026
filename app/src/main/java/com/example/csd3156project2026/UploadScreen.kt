package com.example.csd3156project2026

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import getCurrentLocation
import java.io.File

@Composable
fun UploadScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageBitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var locationName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf(0) }

    var locationError by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionError by rememberSaveable { mutableStateOf<String?>(null) }
    var imageError by rememberSaveable { mutableStateOf<String?>(null) }

    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                val stream = context.contentResolver.openInputStream(photoUri!!)
                if (stream != null) {
                    imageBitmap = BitmapFactory.decodeStream(stream)
                    stream.close()
                } else {
                    errorMessage = "Failed to load image"
                }
                errorMessage = null
            } else {
                errorMessage = "file not supported"
            }
        }

    fun launchCamera() {
        val photoFile = File(
            context.getExternalFilesDir("Pictures"),
            "photo_${System.currentTimeMillis()}.jpg"
        )

        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )

        cameraLauncher.launch(photoUri!!)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                errorMessage = "Camera permission denied"
            } else {
                launchCamera()
            }
        }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                val mimeType = context.contentResolver.getType(uri)

                if (mimeType?.startsWith("image") == true) {
                    val stream = context.contentResolver.openInputStream(uri)
                    imageBitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    errorMessage = null
                } else {
                    errorMessage = "file not supported"
                }
            } else {
                errorMessage = "No image selected"
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp)
        ) {

            // Location Input
            Text("Location")
            OutlinedTextField(
                value = locationName,
                onValueChange = {
                    locationName = it
                    locationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                isError = locationError != null,
                placeholder = { Text("Enter location name") }
            )

            locationError?.let {
                Text(it, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            var location by remember { mutableStateOf<LatLng?>(null) }

            // GPS Below Location
            LaunchedEffect(Unit) {
                location = getCurrentLocation(context)
            }

            Text(
                text = location?.let {
                    "GPS: ${it.latitude}, ${it.longitude}"
                } ?: "Getting location..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {

                if (imageBitmap == null) {
                    Button(onClick = {

                        if (permissionGranted) {
                            launchCamera()
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }

                    }) {
                        Text("Use Camera")
                    }
                } else {

                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                            .clickable {
                                imageBitmap = null
                                photoUri = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("X", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            imageError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // From File Button
            Button(onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }) {
                Text("From File")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating System
            Row {
                for (i in 1..5) {
                    Text(
                        text = "★",
                        fontSize = 28.sp,
                        color = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable {
                                rating = i
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Box (min 4 lines)
            Text("Description")
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descriptionError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                isError = descriptionError != null,
                placeholder = { Text("Write at least 4 lines...") },
                maxLines = 6
            )

            descriptionError?.let {
                Text(it, color = Color.Red)
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red)
            }

            // Upload button fixed at bottom
            Button(
                onClick = {

                    var isValid = true

                    if (imageBitmap == null) {
                        imageError = "Image is required"
                        isValid = false
                    } else {
                        imageError = null
                    }

                    if (locationName.isBlank()) {
                        locationError = "Location cannot be empty"
                        isValid = false
                    }

                    if (description.isBlank()) {
                        descriptionError = "Description cannot be empty"
                        isValid = false
                    }

                    if (isValid && imageBitmap != null) {

                        scope.launch {

                            val currentLatLng = getCurrentLocation(context)

                            if (currentLatLng != null) {

                                uploadToCloudinary(imageBitmap!!) { imageUrl ->

                                    if (imageUrl != null) {

                                        val marker = hashMapOf(
                                            "latitude" to currentLatLng.latitude,
                                            "longitude" to currentLatLng.longitude,
                                            "title" to locationName,
                                            "snippet" to description,
                                            "imageUrl" to imageUrl,
                                            "type" to "coffee"
                                        )

                                        FirebaseFirestore.getInstance()
                                            .collection("markers")
                                            .add(marker)
                                            .addOnSuccessListener {
                                                onClose()
                                            }

                                    } else {
                                        errorMessage = "Image upload failed"
                                    }
                                }

                            } else {
                                errorMessage = "Could not retrieve GPS location"
                            }
                        }
                    }                    
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Upload")
            }

            Spacer(modifier = Modifier.weight(1f)) // pushes Upload to bottom
        }
    }
}