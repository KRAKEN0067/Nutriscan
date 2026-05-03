package com.example.nutriscan5.data.repository

import android.content.Context
import com.example.nutriscan5.data.models.CommunityReview
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun listenToCommunityReviews(
        onSuccess: (List<CommunityReview>) -> Unit,
        onError: (Exception) -> Unit
    ): () -> Unit {
        val registration = firestore.collection("communityReviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents.orEmpty().mapNotNull { document ->
                    runCatching {
                        document.toCommunityReview()
                    }.getOrNull()
                }

                onSuccess(reviews)
            }

        return {
            registration.remove()
        }
    }

    fun shareCommunityReview(
        context: Context,
        review: CommunityReview,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (review.imageUri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                runCatching {
                    CloudinaryRepository.uploadImage(context, review.imageUri)
                }.onSuccess { imageUrl ->
                    writeCommunityReview(
                        review = review.copy(imageUrl = imageUrl),
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }.onFailure { error ->
                    onError(Exception(error.message ?: "Cloudinary upload failed."))
                }
            }
        } else {
            writeCommunityReview(review, onSuccess, onError)
        }
    }

    private fun DocumentSnapshot.toCommunityReview(): CommunityReview {
        val rawFindings = get("findings") as? List<*>

        return CommunityReview(
            id = id,
            productName = getString("productName").orEmpty().ifBlank { "Unnamed product" },
            healthScore = getNumberValue("healthScore").coerceIn(0, 100),
            summary = getString("summary").orEmpty(),
            findings = rawFindings
                ?.mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
                ?: emptyList(),
            date = getString("date").orEmpty(),
            imageUri = null,
            imageUrl = getString("imageUrl")
        )
    }

    private fun DocumentSnapshot.getNumberValue(field: String): Int {
        return when (val value = get(field)) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun writeCommunityReview(
        review: CommunityReview,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "productName" to review.productName,
            "healthScore" to review.healthScore,
            "summary" to review.summary,
            "findings" to review.findings,
            "date" to review.date,
            "imageUrl" to (review.imageUrl ?: "")
        )

        firestore.collection("communityReviews")
            .document(review.id)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
