package com.example.csd3156project2026

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import com.example.csd3156project2026.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import getCurrentLocation
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
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
    var isUploading by rememberSaveable { mutableStateOf(false) }

    var locationError by rememberSaveable { mutableStateOf<String?>(null) }
    var descriptionError by rememberSaveable { mutableStateOf<String?>(null) }
    var imageError by rememberSaveable { mutableStateOf<String?>(null) }

    var location by remember { mutableStateOf<LatLng?>(null) }

    val permissionGranted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    // fetch current location
    LaunchedEffect(context) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            location = getCurrentLocation(context)
        }
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                val stream = context.contentResolver.openInputStream(photoUri!!)
                if (stream != null) {
                    imageBitmap = BitmapFactory.decodeStream(stream)
                    stream.close()
                    imageError = null
                } else {
                    errorMessage = "Failed to load image"
                }
            } else {
                errorMessage = "File not supported"
            }
        }

    fun launchCamera() {
        val photoFile = File(
            context.getExternalFilesDir("Pictures"),
            "photo_${System.currentTimeMillis()}.jpg"
        )
        photoUri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", photoFile
        )
        cameraLauncher.launch(photoUri!!)
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) errorMessage = "Camera permission denied"
            else launchCamera()
        }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType?.startsWith("image") == true) {
                    val stream = context.contentResolver.openInputStream(uri)
                    imageBitmap = BitmapFactory.decodeStream(stream)
                    imageError = null
                } else {
                    errorMessage = "File not supported"
                }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MainBrown,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Entry",
                            color = WhiteText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WhiteText
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onClose() }
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = "Home",
                            tint = CardBrown, modifier = Modifier.size(24.dp))
                        Text("Home", color = CardBrown, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.List, contentDescription = "Journal",
                            tint = CardBrown, modifier = Modifier.size(24.dp))
                        Text("Journal", color = CardBrown, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile",
                            tint = CardBrown, modifier = Modifier.size(24.dp))
                        Text("Profile", color = CardBrown, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --- Location ---
            Text("Location Name:", color = NavBrown, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it; locationError = null },
                modifier = Modifier.fillMaxWidth(),
                isError = locationError != null,
                placeholder = { Text("e.g. Coffee Bean", color = WhiteText.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CreamText,
                    unfocusedBorderColor = CardBrown,
                    focusedContainerColor = CardBrown,
                    unfocusedContainerColor = CardBrown,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    cursorColor = WhiteText,
                    errorBorderColor = Color.Red,
                    errorContainerColor = CardBrown
                ),
                singleLine = true
            )
            locationError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

            // gps
            Text(
                text = location?.let { "📍 ${String.format("%.5f", it.latitude)}, ${String.format("%.5f", it.longitude)}" }
                    ?: "📍 Getting GPS location...",
                color = NavBrown.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            // image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBrown)
                    .clickable(enabled = imageBitmap == null) {
                        if (permissionGranted) launchCamera()
                        else cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = null,
                            tint = WhiteText.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // X button to clear
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .clickable { imageBitmap = null; photoUri = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            imageError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

            // camera or file
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    Text("Use Camera", color = WhiteText)
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
                    Text("From File", color = WhiteText)
                }
            }

            // --- Rating ---
            //Row(verticalAlignment = Alignment.CenterVertically) {
            //    Text("Rating: ", color = NavBrown, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            //    Spacer(modifier = Modifier.width(8.dp))
            //    for (i in 1..5) {
            //        Text(
            //            text = "★",
            //            fontSize = 28.sp,
            //            color = if (i <= rating) StarYellow else WhiteText.copy(alpha = 0.3f),
            //            modifier = Modifier
            //                .padding(end = 4.dp)
            //                .clickable { rating = i }
            //        )
            //    }
            //}

            // desc
            Text("Description:", color = NavBrown, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descriptionError = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                isError = descriptionError != null,
                placeholder = { Text("Write a description...", color = WhiteText.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CreamText,
                    unfocusedBorderColor = CardBrown,
                    focusedContainerColor = CardBrown,
                    unfocusedContainerColor = CardBrown,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    cursorColor = WhiteText,
                    errorBorderColor = Color.Red,
                    errorContainerColor = CardBrown
                ),
                maxLines = 6
            )
            descriptionError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // submit
            Button(
                onClick = {
                    var isValid = true
                    if (imageBitmap == null) { imageError = "Image is required"; isValid = false }
                    if (locationName.isBlank()) { locationError = "Location cannot be empty"; isValid = false }
                    if (description.isBlank()) { descriptionError = "Description cannot be empty"; isValid = false }

                    if (isValid && imageBitmap != null) {
                        isUploading = true
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
                                            .addOnSuccessListener { onClose() }
                                    } else {
                                        errorMessage = "Image upload failed"
                                        isUploading = false
                                    }
                                }
                            } else {
                                errorMessage = "Could not retrieve GPS location"
                                isUploading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 8.dp),
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBrown,
                    disabledContainerColor = ButtonBrown.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isUploading) "Uploading..." else "Submit",
                    color = WhiteText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
