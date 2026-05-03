package com.example.nutriscan5.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.nutriscan5.data.models.CommunityReview
import com.example.nutriscan5.data.models.ScanResult

object DataRepository {
    val scanHistory = mutableStateListOf<ScanResult>()
    val communityReviews = mutableStateListOf<CommunityReview>()

    fun addScan(scan: ScanResult) {
        scanHistory.add(0, scan)
    }

    fun updateScan(updatedScan: ScanResult) {
        val index = scanHistory.indexOfFirst { it.id == updatedScan.id }
        if (index != -1) {
            scanHistory[index] = updatedScan
        }
    }

    fun shareScan(scan: ScanResult) {
        if (communityReviews.any { it.id == scan.id }) return

        communityReviews.add(
            0,
            CommunityReview(
                id = scan.id,
                productName = scan.productName,
                healthScore = scan.healthScore,
                summary = scan.summary,
                findings = scan.findings,
                date = scan.date,
                imageUri = scan.imageUri,
                imageUrl = null
            )
        )

        updateScan(scan.copy(isShared = true))
    }

    fun findScanById(id: String): ScanResult? = scanHistory.firstOrNull { it.id == id }

    fun replaceCommunityReviews(reviews: List<CommunityReview>) {
        communityReviews.clear()
        communityReviews.addAll(reviews)
    }

    fun findCommunityReviewById(id: String): CommunityReview? {
        return communityReviews.firstOrNull { it.id == id }
    }
}
