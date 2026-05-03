package com.example.nutriscan5

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.nutriscan5.ui.components.BottomNavBar
import com.example.nutriscan5.ui.screens.CommunityScreen
import com.example.nutriscan5.ui.screens.HistoryScreen
import com.example.nutriscan5.ui.screens.HomeScreen
import com.example.nutriscan5.ui.screens.ResultScreen
import com.example.nutriscan5.ui.screens.ScanScreen
import com.example.nutriscan5.ui.theme.Nutriscan5Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nutriscan5Theme {
                StableAppScale {
                    NutriScanApp()
                }
            }
        }
    }
}

@Composable
private fun StableAppScale(content: @Composable () -> Unit) {
    val currentDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = 1f
        )
    ) {
        content()
    }
}

enum class BottomTab {
    Home,
    History,
    Community
}

enum class Screen {
    Home,
    History,
    Community,
    Scan,

    Result
}

@Composable
fun NutriScanApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var capturedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedScanId by remember { mutableStateOf<String?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            capturedImageUri = uri
            selectedScanId = null
            currentScreen = Screen.Result
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.Scan && currentScreen != Screen.Result) {
                BottomNavBar(
                    selectedTab = when (currentScreen) {
                        Screen.Home -> BottomTab.Home
                        Screen.History -> BottomTab.History
                        Screen.Community -> BottomTab.Community
                        Screen.Scan -> BottomTab.Home
                        Screen.Result -> BottomTab.Home
                    },
                    onTabSelected = {
                        currentScreen = when (it) {
                            BottomTab.Home -> Screen.Home
                            BottomTab.History -> Screen.History
                            BottomTab.Community -> Screen.Community
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Home -> HomeScreen(
                innerPadding = innerPadding,
                onScanClick = {
                    selectedScanId = null
                    currentScreen = Screen.Scan
                },
                onGalleryClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onHistoryClick = { currentScreen = Screen.History },
                onCommunityClick = { currentScreen = Screen.Community }
            )

            Screen.History -> HistoryScreen(
                innerPadding = innerPadding,
                onScanSelected = { scanId ->
                    selectedScanId = scanId
                    currentScreen = Screen.Result
                }
            )

            Screen.Community -> CommunityScreen(
                innerPadding = innerPadding,
                onReviewSelected = { reviewId ->
                    selectedScanId = reviewId
                    currentScreen = Screen.Result
                }
            )

            Screen.Scan -> ScanScreen(
                modifier = Modifier.padding(innerPadding),
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    selectedScanId = null
                    currentScreen = Screen.Result
                },
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )

            Screen.Result -> ResultScreen(
                innerPadding = innerPadding,
                imageUri = if (selectedScanId == null) capturedImageUri else null,
                existingScan = selectedScanId?.let { com.example.nutriscan5.data.repository.DataRepository.findScanById(it) },
                existingCommunityReview = selectedScanId?.let { com.example.nutriscan5.data.repository.DataRepository.findCommunityReviewById(it) },
                onBackClick = {
                    capturedImageUri = null
                    currentScreen = Screen.Home
                }
            )
        }
    }
}
