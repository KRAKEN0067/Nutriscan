package com.example.nutriscan5.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutriscan5.data.models.CommunityReview
import com.example.nutriscan5.data.models.ScanResult
import com.example.nutriscan5.data.repository.DataRepository
import com.example.nutriscan5.data.repository.FirebaseRepository
import com.example.nutriscan5.utils.AnalysisResult
import com.example.nutriscan5.utils.HealthAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun ResultScreen(
    innerPadding: PaddingValues,
    imageUri: Uri?,
    existingScan: ScanResult? = null,
    existingCommunityReview: CommunityReview? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val initialCommunityScan = existingCommunityReview?.toScanResult()
    val initialScan = existingScan ?: initialCommunityScan
    var savedScan by remember(initialScan) { mutableStateOf(initialScan) }
    var extractedText by remember(initialScan) { mutableStateOf(initialScan?.extractedText.orEmpty()) }
    var analysisResult by remember(initialScan) {
        mutableStateOf(
            initialScan?.toAnalysisResult()
        )
    }
    var isLoading by remember(initialScan) { mutableStateOf(initialScan == null && imageUri != null) }
    var errorMessage by remember(initialScan) { mutableStateOf<String?>(null) }
    var shareMessage by remember { mutableStateOf<String?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareProductName by remember(initialScan) {
        mutableStateOf(initialScan?.productName.orEmpty())
    }

    LaunchedEffect(imageUri, initialScan) {
        if (initialScan != null || imageUri == null) return@LaunchedEffect

        isLoading = true
        errorMessage = null
        shareMessage = null

        runCatching {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val textResult = recognizeText(recognizer, image)
            recognizer.close()

            val cleanedText = cleanExtractedText(textResult.text)
            val analysis = HealthAnalyzer.analyzeText(cleanedText)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val newScan = ScanResult(
                id = UUID.randomUUID().toString(),
                date = dateFormat.format(Date()),
                imageUri = imageUri,
                productName = analysis.productName,
                extractedText = cleanedText,
                healthScore = analysis.score,
                findings = analysis.findings,
                summary = analysis.summary,
                recommendation = analysis.recommendation
            )

            DataRepository.addScan(newScan)
            savedScan = newScan
            extractedText = cleanedText
            analysisResult = analysis
        }.onFailure { error ->
            errorMessage = error.message ?: "Something went wrong while analyzing the label."
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFC))
            .padding(innerPadding)
            .statusBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = savedScan?.productName ?: "Analysis Result",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0E2341)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AsyncImage(
                model = imageUri ?: savedScan?.imageUri,
                contentDescription = "Captured product image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        when {
            isLoading -> {
                LoadingCard()
            }

            errorMessage != null -> {
                MessageCard(
                    title = "Analysis failed",
                    body = errorMessage!!,
                    bodyColor = Color(0xFFB42318)
                )
            }

            analysisResult != null && savedScan != null -> {
                AnalysisOverviewCard(scan = savedScan!!)

                MessageCard(
                    title = "What this means",
                    body = savedScan!!.summary
                )

                MessageCard(
                    title = "How often to consume",
                    body = savedScan!!.recommendation
                )

                FindingsCard(findings = savedScan!!.findings)

                MessageCard(
                    title = "Extracted ingredient text",
                    body = extractedText.ifBlank { "No clear ingredient text was detected." }
                )

                if (!savedScan!!.isShared) {
                    OutlinedButton(
                        onClick = {
                            shareProductName = savedScan!!.productName
                            showShareDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Share This Analysis")
                    }
                }

                if (shareMessage != null) {
                    Text(
                        text = shareMessage!!,
                        color = Color(0xFF10C281),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            else -> {
                MessageCard(
                    title = "No text found",
                    body = "Try retaking the photo with the ingredient list fully visible and well lit."
                )
            }
        }

        if (showShareDialog && savedScan != null) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("Share to Community") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Enter the product name you want people to see in the community tab.")
                        OutlinedTextField(
                            value = shareProductName,
                            onValueChange = { shareProductName = it },
                            singleLine = true,
                            label = { Text("Product name") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val finalName = shareProductName.trim().ifBlank { savedScan!!.productName }
                            val updatedScan = savedScan!!.copy(
                                productName = finalName,
                                isShared = true
                            )
                            DataRepository.updateScan(updatedScan)
                            DataRepository.shareScan(updatedScan)
                            DataRepository.communityReviews
                                .firstOrNull { it.id == updatedScan.id }
                                ?.let { review ->
                                    FirebaseRepository.shareCommunityReview(
                                        context = context,
                                        review = review,
                                        onSuccess = {
                                            shareMessage = "Shared to the community tab."
                                        },
                                        onError = { error ->
                                            shareMessage = error.message
                                                ?: "Image upload failed while sharing."
                                        }
                                    )
                                }
                            savedScan = updatedScan
                            shareMessage = "Uploading image and sharing..."
                            showShareDialog = false
                        }
                    ) {
                        Text("Share")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10C281))
        ) {
            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color(0xFF10C281))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Reading the label and analyzing ingredients...",
                color = Color(0xFF6E86A6)
            )
        }
    }
}

@Composable
private fun AnalysisOverviewCard(scan: ScanResult) {
    val scoreColor = when {
        scan.healthScore >= 80 -> Color(0xFF10C281)
        scan.healthScore >= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(scoreColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${scan.healthScore}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )
            }

            Column {
                Text(
                    text = "Health score",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0E2341)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scoreLabel(scan.healthScore),
                    color = Color(0xFF6E86A6)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scan.date,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun FindingsCard(findings: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Key findings",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0E2341)
            )
            Spacer(modifier = Modifier.height(10.dp))
            findings.forEach { finding ->
                Text(
                    text = "\u2022 $finding",
                    color = Color(0xFF334155),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    body: String,
    bodyColor: Color = Color(0xFF334155)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0E2341)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                color = bodyColor,
                lineHeight = 22.sp
            )
        }
    }
}

private fun cleanExtractedText(rawText: String): String {
    val lines = rawText.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val ingredientStart = lines.indexOfFirst {
        it.contains("ingredient", ignoreCase = true)
    }

    val relevantLines = if (ingredientStart != -1) {
        lines.drop(ingredientStart).take(8)
    } else {
        lines.take(8)
    }

    return relevantLines.joinToString("\n")
}

private fun scoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Good choice most of the time"
        score >= 50 -> "Okay occasionally"
        else -> "Better as a rare treat"
    }
}

private fun ScanResult.toAnalysisResult(): AnalysisResult {
    return AnalysisResult(
        productName = productName,
        score = healthScore,
        summary = summary,
        recommendation = recommendation,
        findings = findings
    )
}

private fun CommunityReview.toScanResult(): ScanResult {
    return ScanResult(
        id = id,
        date = date,
        imageUri = imageUri,
        productName = productName,
        extractedText = "",
        healthScore = healthScore,
        findings = findings,
        summary = summary,
        recommendation = "This analysis was shared by the community.",
        isShared = true
    )
}

private suspend fun recognizeText(
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    image: InputImage
): com.google.mlkit.vision.text.Text = suspendCancellableCoroutine { continuation ->
    recognizer.process(image)
        .addOnSuccessListener { result ->
            continuation.resume(result)
        }
        .addOnFailureListener { error ->
            continuation.resumeWithException(error)
        }
}
