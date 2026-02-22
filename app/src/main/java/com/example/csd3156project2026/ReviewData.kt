package com.example.csd3156project2026

data class ReviewData(
    val markerId: String,   // Links the review to a specific map marker
    val userId: String,
    val rating: Int,
    val comment: String,
    val imageUrl: String?   // Store the link to the image here
) {

}