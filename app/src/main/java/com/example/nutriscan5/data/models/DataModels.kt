package com.example.nutriscan5.data.models

import android.net.Uri

data class ScanResult(
    val id: String,
    val date: String,
    val imageUri: Uri?,
    val productName: String,
    val extractedText: String,
    val healthScore: Int,
    val findings: List<String>,
    val summary: String,
    val recommendation: String,
    val isShared: Boolean = false
)

data class CommunityReview(
    val id: String,
    val productName: String,
    val healthScore: Int,
    val summary: String,
    val findings: List<String>,
    val date: String,
    val imageUri: Uri? = null,
    val imageUrl: String? = null
)
