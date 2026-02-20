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
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun UploadScreen(
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Upload New") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()) {

        // Top Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { selectedTab = "Upload New" }) {
                Text("Upload New")
            }
            Button(onClick = { selectedTab = "Upload Existing" }) {
                Text("Upload Existing")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        UploadContent(
            title = selectedTab,
            onClose = onClose
        )
    }
}

@Composable
fun UploadContent(
    title: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                val stream = context.contentResolver.openInputStream(photoUri!!)
                imageBitmap = android.graphics.BitmapFactory.decodeStream(stream)
                errorMessage = null
            } else {
                errorMessage = "file not supported"
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                errorMessage = "Camera permission denied"
            }
            else {
                cameraLauncher.launch(photoUri!!)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Location: Placeholder Location")
        Text("GPS: 1.3521, 103.8198")

        Spacer(modifier = Modifier.height(16.dp))

        // Grey Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap == null) {
                Button(onClick = {

                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

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

                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }

                }) {
                    Text("Use Camera")
                }
            } else {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }

        if (imageBitmap != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onClose() // Just return to Home
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload")
            }
        }
    }
}