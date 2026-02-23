package com.example.csd3156project2026

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.csd3156project2026.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class JournalEntry(
    val review: ReviewData,
    val markerTitle: String = "",
    val markerSnippet: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.email?.substringBefore("@") ?: "Anonymous"

    var starFilter by rememberSaveable { mutableStateOf<Int?>(null) }
    var showFilterDropdown by rememberSaveable { mutableStateOf(false) }

    var journalEntries by remember { mutableStateOf(listOf<JournalEntry>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    // Fetch this user's reviews and join with marker info
    LaunchedEffect(userId) {
        db.collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { reviewSnapshot ->
                val reviews = reviewSnapshot.documents.map { doc ->
                    ReviewData(
                        markerId = doc.getString("markerId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                }

                // Fetch marker details for each review
                db.collection("markers").get().addOnSuccessListener { markerSnapshot ->
                    val markerMap = markerSnapshot.documents.associate { doc ->
                        (doc.getString("title") ?: "") to (doc.getString("snippet") ?: "")
                    }

                    journalEntries = reviews.map { review ->
                        JournalEntry(
                            review = review,
                            markerTitle = review.markerId,
                            markerSnippet = markerMap[review.markerId] ?: ""
                        )
                    }

                    isLoading = false
                }.addOnFailureListener {
                    journalEntries = reviews.map { JournalEntry(review = it, markerTitle = it.markerId) }
                    isLoading = false
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    val filteredEntries = journalEntries.filter { entry ->
        val matchesSearch = searchQuery.isEmpty() ||
                entry.markerTitle.contains(searchQuery, ignoreCase = true) ||
                entry.review.comment.contains(searchQuery, ignoreCase = true)
        val matchesStar = starFilter == null || entry.review.rating == starFilter
        matchesSearch && matchesStar
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                            text = "All Entries",
                            color = WhiteText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
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
                            onHomeClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = CardBrown,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Home",
                            color = CardBrown,
                            fontSize = 12.sp
                        )
                    }

                    // journal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = "Journal",
                            tint = CreamText,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Reviews",
                            color = CreamText,
                            fontSize = 12.sp
                        )
                    }

                    // profile
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Entries", fontSize = 16.sp, color = NavBrown.copy(alpha = 0.4f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MainBrown.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    Box {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Filter",
                            tint = MainBrown.copy(alpha = 0.7f),
                            modifier = Modifier.clickable { showFilterDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false },
                            modifier = Modifier.background(WhiteText)
                        ) {
                            // "all" option
                            DropdownMenuItem(
                                text = { Text("All", color = NavBrown) },
                                onClick = { starFilter = null; showFilterDropdown = false }
                            )
                            // star options
                            (5 downTo 1).forEach { stars ->
                                val capturedStars = stars
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(capturedStars) {
                                                Text("★", fontSize = 16.sp, color = StarYellow)
                                            }
                                            repeat(5 - capturedStars) {
                                                Text("☆", fontSize = 16.sp, color = StarYellow)
                                            }
                                        }
                                    },
                                    onClick = { starFilter = capturedStars; showFilterDropdown = false }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    focusedTextColor = MainBrown,
                    unfocusedTextColor = MainBrown,
                    cursorColor = MainBrown
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CreamText)
                }
            } else if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No entries yet.\nStart reviewing coffee shops!"
                        else "No results for \"$searchQuery\"",
                        color = NavBrown.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEntries) { entry ->
                        JournalEntryCard(entry = entry)
                    }
                }
            }
        }

        // add new coffee shop
        //Box(
        //    modifier = Modifier
        //        .fillMaxSize()
        //        .padding(paddingValues)
        //        .padding(bottom = 16.dp),
        //    contentAlignment = Alignment.BottomCenter
        //) {
        //    Button(
        //        onClick = { onUploadClick() },
        //        colors = ButtonDefaults.buttonColors(containerColor = ButtonBrown),
        //        shape = RoundedCornerShape(24.dp),
        //        modifier = Modifier.padding(horizontal = 32.dp)
        //    ) {
        //        Text(
        //            text = "Add New Entry",
        //            color = WhiteText,
        //            fontWeight = FontWeight.SemiBold,
        //            fontSize = 15.sp
        //        )
        //    }
        //}
    }
}

@Composable
fun JournalEntryCard(entry: JournalEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBrown)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Image thumbnail
            if (entry.review.imageUrl != null) {
                AsyncImage(
                    model = entry.review.imageUrl,
                    contentDescription = "Review Image",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MainBrown),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = CreamText.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title row with time placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.markerTitle,
                        color = NavBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // star rating
                Row {
                    Row {
                        repeat(5) { index ->
                            Text(
                                text = if (index < entry.review.rating) "★" else "☆",
                                fontSize = 16.sp,
                                color = StarYellow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // comment preview
                Text(
                    text = entry.review.comment.ifEmpty { entry.markerSnippet },
                    color = NavBrown.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    maxLines = 4,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
