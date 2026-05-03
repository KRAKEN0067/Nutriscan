package com.example.nutriscan5.utils

object HealthAnalyzer {

    fun analyzeText(text: String): AnalysisResult {
        val normalizedText = normalizeIngredientsText(text)
        if (normalizedText.isBlank()) {
            return AnalysisResult(
                productName = "Unknown product",
                score = 0,
                summary = "No readable ingredient text was found.",
                recommendation = "Retake the photo in good lighting and keep the ingredient list flat.",
                findings = listOf("No readable text found on the image.")
            )
        }

        val findings = mutableListOf<String>()
        var score = 75

        val strongNegativeSignals = listOf(
            Signal(
                keys = listOf("high fructose corn syrup", "hfcs"),
                impact = -25,
                finding = "High fructose corn syrup is a strong processed-sugar red flag."
            ),
            Signal(
                keys = listOf("hydrogenated", "partially hydrogenated", "trans fat"),
                impact = -25,
                finding = "Hydrogenated fats are a major nutritional red flag."
            ),
            Signal(
                keys = listOf("monosodium glutamate", "msg"),
                impact = -12,
                finding = "MSG suggests a more heavily engineered processed product."
            )
        )

        val negativeSignals = listOf(
            Signal(listOf("sugar", "added sugar"), -12, "Added sugar appears in the ingredient list."),
            Signal(listOf("corn syrup"), -12, "Corn syrup suggests a sugary formulation."),
            Signal(listOf("glucose syrup"), -12, "Glucose syrup suggests a highly processed sweetener."),
            Signal(listOf("palm oil"), -10, "Palm oil suggests a more processed formulation."),
            Signal(listOf("artificial flavor", "artificial flavour"), -10, "Artificial flavouring suggests a more processed product."),
            Signal(listOf("artificial color", "artificial colour"), -12, "Artificial colouring suggests a more processed product."),
            Signal(listOf("preservative", "sodium benzoate", "potassium sorbate"), -8, "Preservatives suggest longer shelf-life processing."),
            Signal(listOf("maltodextrin"), -10, "Maltodextrin is a heavily processed additive."),
            Signal(listOf("dextrose"), -8, "Dextrose adds another refined sugar signal."),
            Signal(listOf("refined flour", "maida", "bleached flour"), -10, "Refined flour reduces the nutritional quality.")
        )

        val positiveSignals = listOf(
            Signal(listOf("oats", "rolled oats"), 8, "Oats can add fiber and improve satiety."),
            Signal(listOf("whole grain", "whole wheat"), 10, "Whole grains are usually a positive sign."),
            Signal(listOf("almond", "almonds"), 6, "Nuts can contribute healthy fats and texture."),
            Signal(listOf("peanut", "peanuts"), 6, "Peanuts may provide some protein and fats."),
            Signal(listOf("milk", "milk solids"), 4, "Milk ingredients may add some protein or calcium."),
            Signal(listOf("chickpea", "lentil", "millet"), 8, "Legume or millet ingredients improve the profile."),
            Signal(listOf("fiber", "fibre"), 6, "Added fiber can help balance a processed product.")
        )

        val allSignals = strongNegativeSignals + negativeSignals + positiveSignals
        allSignals.forEach { signal ->
            if (signal.matches(normalizedText)) {
                findings += signal.finding
                score += signal.impact
            }
        }

        val ingredientCount = extractIngredientCount(normalizedText)
        if (ingredientCount >= 15) {
            score -= 8
            findings += "A long ingredient list usually points to heavier processing."
        } else if (ingredientCount in 1..5) {
            score += 4
            findings += "A shorter ingredient list is usually easier to understand and often less processed."
        }

        if (normalizedText.contains("sugar") && normalizedText.contains("corn syrup")) {
            score -= 8
            findings += "Multiple sugar-type ingredients appear together."
        }

        if (normalizedText.contains("whole grain") && normalizedText.contains("fiber")) {
            score += 4
            findings += "Whole grains with fiber make the product more balanced."
        }

        score = score.coerceIn(5, 95)

        val distinctFindings = findings.distinct().take(5)
        val summary = when {
            score >= 80 -> "This product looks relatively balanced for a packaged grocery item."
            score >= 65 -> "This product looks acceptable in moderation, though it is still processed."
            score >= 45 -> "This product appears fairly processed and is better treated as an occasional choice."
            else -> "This product looks heavily processed and is not ideal for regular consumption."
        }

        val recommendation = when {
            score >= 80 -> "Reasonable a few times a week if it fits your overall diet."
            score >= 65 -> "Best kept to a few times per week rather than every day."
            score >= 45 -> "Better as an occasional choice, around a few times per month."
            else -> "Best treated as a rare treat rather than a regular habit."
        }

        return AnalysisResult(
            productName = guessProductName(text),
            score = score,
            summary = summary,
            recommendation = recommendation,
            findings = if (distinctFindings.isEmpty()) {
                listOf("The ingredient list was read, but the product needs a clearer label for deeper analysis.")
            } else {
                distinctFindings
            }
        )
    }

    private fun normalizeIngredientsText(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9,\\s-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun guessProductName(text: String): String {
        return text.lineSequence()
            .map { it.trim() }
            .firstOrNull { line ->
                line.isNotBlank() &&
                    line.length in 3..40 &&
                    !line.contains("ingredient", ignoreCase = true) &&
                    !line.contains("nutrition", ignoreCase = true)
            }
            ?: "Unknown product"
    }

    private fun extractIngredientCount(normalizedText: String): Int {
        return normalizedText.split(",")
            .map { it.trim() }
            .count { it.isNotBlank() }
    }
}

private data class Signal(
    val keys: List<String>,
    val impact: Int,
    val finding: String
) {
    fun matches(text: String): Boolean = keys.any { key -> text.contains(key) }
}

data class AnalysisResult(
    val productName: String,
    val score: Int,
    val summary: String,
    val recommendation: String,
    val findings: List<String>
)
