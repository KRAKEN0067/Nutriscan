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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onScanClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onCommunityClick: () -> Unit = {}
) {
    val brandGreen = Color(0xFF10C281)
    val darkText = Color(0xFF0E2341)
    val subText = Color(0xFF6E86A6)
    val bgColor = Color(0xFFF7FAFC)
    val cardBorder = Color(0xFFDDE6F0)
    val tipBg = Color(0xFFEAF9F1)
    val tipBorder = Color(0xFFCBEFDE)
    val configuration = LocalConfiguration.current
    val compactScreen = configuration.screenWidthDp < 380
    val horizontalPadding = if (compactScreen) 18.dp else 22.dp
    val logoTextSize = if (compactScreen) 20.sp else 24.sp
    val heroTextSize = if (compactScreen) 24.sp else 30.sp
    val heroLineHeight = if (compactScreen) 30.sp else 36.sp
    val bodyTextSize = if (compactScreen) 14.sp else 18.sp
    val bodyLineHeight = if (compactScreen) 21.sp else 26.sp
    val scanCardHeight = if (compactScreen) 148.dp else 168.dp
    val actionCardHeight = if (compactScreen) 102.dp else 118.dp
    val actionTitleSize = if (compactScreen) 15.sp else 18.sp
    val tipTitleSize = if (compactScreen) 17.sp else 20.sp
    val tipBodySize = if (compactScreen) 14.sp else 16.sp
    val headerSpacing = if (compactScreen) 22.dp else 26.dp
    val sectionSpacing = if (compactScreen) 16.dp else 20.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(innerPadding)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "NutriScan",
                fontSize = logoTextSize,
                fontWeight = FontWeight.Bold,
                color = darkText
            )
        }

        Spacer(modifier = Modifier.height(headerSpacing))

        Text(
            text = buildAnnotatedString {
                append("Know exactly what's\n")
                withStyle(style = SpanStyle(color = brandGreen)) {
                    append("inside")
                }
                append(" your food.")
            },
            fontSize = heroTextSize,
            lineHeight = heroLineHeight,
            fontWeight = FontWeight.ExtraBold,
            color = darkText
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Scan ingredients for instant AI analysis and health scores.",
            fontSize = bodyTextSize,
            lineHeight = bodyLineHeight,
            color = subText
        )

        Spacer(modifier = Modifier.height(headerSpacing))

        Card(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(scanCardHeight),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = brandGreen)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = "Scan ingredients",
                    tint = Color.White,
                    modifier = Modifier.size(if (compactScreen) 28.dp else 32.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Scan Ingredients",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(sectionSpacing))

        Card(
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compactScreen) 58.dp else 64.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(brandGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "Upload photo",
                        tint = brandGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Upload Photo",
                        fontSize = if (compactScreen) 15.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkText
                    )
                    Text(
                        text = "Choose an ingredients image from gallery",
                        fontSize = if (compactScreen) 12.sp else 13.sp,
                        color = subText,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(sectionSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compactScreen) 12.dp else 16.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.History,
                title = "History",
                compactScreen = compactScreen,
                actionTitleSize = actionTitleSize,
                actionCardHeight = actionCardHeight,
                onClick = onHistoryClick,
                darkText = darkText,
                brandGreen = brandGreen,
                cardBorder = cardBorder
            )

            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Search,
                title = "Community",
                compactScreen = compactScreen,
                actionTitleSize = actionTitleSize,
                actionCardHeight = actionCardHeight,
                onClick = onCommunityClick,
                darkText = darkText,
                brandGreen = brandGreen,
                cardBorder = cardBorder
            )
        }

        Spacer(modifier = Modifier.height(headerSpacing))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = tipBg),
            border = BorderStroke(1.dp, tipBorder)
        ) {
            Column(modifier = Modifier.padding(if (compactScreen) 16.dp else 20.dp)) {
                Text(
                    text = "Pro Tip",
                    fontSize = tipTitleSize,
                    fontWeight = FontWeight.Bold,
                    color = darkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Make sure the ingredients list is well-lit and flat for the best analysis results.",
                    fontSize = tipBodySize,
                    lineHeight = if (compactScreen) 22.sp else 25.sp,
                    color = Color(0xFF18795B)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    compactScreen: Boolean,
    actionTitleSize: androidx.compose.ui.unit.TextUnit,
    actionCardHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    darkText: Color,
    brandGreen: Color,
    cardBorder: Color
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(actionCardHeight),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = brandGreen,
                modifier = Modifier.size(if (compactScreen) 24.dp else 28.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = actionTitleSize,
                fontWeight = FontWeight.SemiBold,
                color = darkText
            )
        }
    }
}
