package com.example.nutriscan5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutriscan5.data.repository.DataRepository

@Composable
fun HistoryScreen(
    innerPadding: PaddingValues,
    onScanSelected: (String) -> Unit = {}
) {
    val bgColor = Color(0xFFF7FAFC)
    val darkText = Color(0xFF0E2341)
    val mutedText = Color(0xFF8CA0BC)
    val brandGreen = Color(0xFF10C281)
    val history = DataRepository.scanHistory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(innerPadding)
            .statusBarsPadding()
    ) {
        TopBar(title = "Scan History")

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No history yet. Start scanning!",
                    color = mutedText,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { scanResult ->
                    Card(
                        onClick = { onScanSelected(scanResult.id) },
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
                                if (scanResult.imageUri != null) {
                                    AsyncImage(
                                        model = scanResult.imageUri,
                                        contentDescription = "History image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = "History image",
                                        tint = mutedText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(scanResult.date, color = mutedText, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    scanResult.productName,
                                    color = darkText,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val scoreColor = when {
                                    scanResult.healthScore >= 80 -> brandGreen
                                    scanResult.healthScore >= 50 -> Color(0xFFF59E0B)
                                    else -> Color(0xFFEF4444)
                                }
                                
                                Text("Score: ${scanResult.healthScore}", color = scoreColor, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("View Details", color = brandGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String) {
    val brandGreen = Color(0xFF10C281)
    val darkText = Color(0xFF0E2341)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(brandGreen, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "App logo",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = darkText,
            modifier = Modifier.weight(1f)
        )
    }
}
