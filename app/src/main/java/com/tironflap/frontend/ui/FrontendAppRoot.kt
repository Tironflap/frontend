package com.tironflap.frontend.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tironflap.frontend.ui.screens.HomeScreen
import com.tironflap.frontend.ui.screens.LibraryScreen
import com.tironflap.frontend.ui.screens.SettingsScreen
import com.tironflap.frontend.ui.screens.SystemsScreen

@Composable
fun FrontendAppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToLibrary = { navController.navigate("library") },
                onNavigateToSystems = { navController.navigate("systems") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("library") {
            LibraryScreen(onBack = { navController.popBackStack() })
        }
        composable("systems") {
            SystemsScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
