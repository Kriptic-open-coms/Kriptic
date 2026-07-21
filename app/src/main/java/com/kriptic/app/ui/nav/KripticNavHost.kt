package com.kriptic.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kriptic.app.identity.Identity
import com.kriptic.app.ui.screens.KnowledgeScreen
import com.kriptic.app.ui.screens.MapScreen
import com.kriptic.app.ui.screens.MessagingScreen
import com.kriptic.app.ui.theme.DesignTokens

sealed class Screen(val route: String, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Messaging : Screen("messaging", "Messages", Icons.Filled.Chat, Icons.Outlined.Chat)
    data object Map : Screen("map", "Map", Icons.Filled.Map, Icons.Outlined.Map)
    data object Knowledge : Screen("knowledge", "Knowledge", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
}

private val screens = listOf(Screen.Messaging, Screen.Map, Screen.Knowledge)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KripticNavHost(identity: Identity) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DesignTokens.AccentColor,
                            selectedTextColor = DesignTokens.AccentColor,
                            indicatorColor = DesignTokens.AccentColor.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Messaging.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Messaging.route) {
                MessagingScreen(identity = identity)
            }
            composable(Screen.Map.route) {
                MapScreen(identity = identity)
            }
            composable(Screen.Knowledge.route) {
                KnowledgeScreen()
            }
        }
    }
}
