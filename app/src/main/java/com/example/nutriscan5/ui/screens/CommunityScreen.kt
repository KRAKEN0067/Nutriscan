package com.example.nutriscan5.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutriscan5.data.repository.DataRepository
import com.example.nutriscan5.data.repository.FirebaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    innerPadding: PaddingValues,
    onReviewSelected: (String) -> Unit = {}
) {
    val bgColor = Color(0xFFF7FAFC)
    val mutedText = Color(0xFF8CA0BC)
    val borderColor = Color(0xFFDDE6F0)
    
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firebaseReviews by remember {
        mutableStateOf(emptyList<com.example.nutriscan5.data.models.CommunityReview>())
    }

    DisposableEffect(Unit) {
        val stopListening = FirebaseRepository.listenToCommunityReviews(
            onSuccess = { reviews ->
                firebaseReviews = reviews
                DataRepository.replaceCommunityReviews(reviews)
                isLoading = false
                errorMessage = null
            },
            onError = { error ->
                errorMessage = error.message ?: "Could not load community reviews."
                isLoading = false
            }
        )

        onDispose {
            stopListening()
        }
    }
    
    val allReviews = (firebaseReviews + DataRepository.communityReviews)
        .distinctBy { it.id }
    val filteredReviews = if (searchQuery.isBlank()) {
        allReviews
    } else {
        allReviews.filter {
            it.productName.contains(searchQuery, ignoreCase = true) || 
            it.summary.contains(searchQuery, ignoreCase = true) ||
            it.findings.any { finding -> finding.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(innerPadding)
            .statusBarsPadding()
    ) {
        TopBar(title = "Community Reviews")

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search products...", color = mutedText) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = mutedText
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .background(Color.White, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10C281),
                unfocusedBorderColor = borderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF10C281))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFB42318),
                    fontSize = 16.sp
                )
            }
        } else if (filteredReviews.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No community reviews found.",
                    color = mutedText,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = filteredReviews,
                    key = { review -> review.id }
                ) { review ->
                    Card(
                        onClick = { onReviewSelected(review.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFFE7ECEF), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (review.imageUrl != null || review.imageUri != null) {
                                    AsyncImage(
                                        model = review.imageUrl ?: review.imageUri,
                                        contentDescription = review.productName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = review.productName,
                                        tint = mutedText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = review.productName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0E2341)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = review.date,
                                    fontSize = 13.sp,
                                    color = mutedText
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val scoreColor = when {
                                    review.healthScore >= 80 -> Color(0xFF10C281)
                                    review.healthScore >= 50 -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                                Text(
                                    text = "Score: ${review.healthScore}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = review.summary.ifBlank { "No summary provided yet." },
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155),
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Shared by community",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF10C281)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
