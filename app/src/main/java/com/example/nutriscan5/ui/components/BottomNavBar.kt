package com.example.nutriscan5.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.nutriscan5.BottomTab

@Composable
fun BottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = selectedTab == BottomTab.Home,
            onClick = { onTabSelected(BottomTab.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomTab.History,
            onClick = { onTabSelected(BottomTab.History) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "History"
                )
            },
            label = { Text("History") }
        )

        NavigationBarItem(
            selected = selectedTab == BottomTab.Community,
            onClick = { onTabSelected(BottomTab.Community) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            label = { Text("Search") }
        )
    }
}
