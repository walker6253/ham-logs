package com.hamlog.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hamlog.ui.screen.LogEntryScreen
import com.hamlog.ui.screen.MainScreen
import com.hamlog.ui.screen.SettingsScreen
import com.hamlog.viewmodel.LogEntryViewModel
import com.hamlog.viewmodel.MainViewModel
import com.hamlog.viewmodel.SettingsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Main : Screen("main", "日志", Icons.Default.DateRange)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(Screen.Main, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Main.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                val mainViewModel: MainViewModel = viewModel()
                MainScreen(
                    viewModel = mainViewModel,
                    onNavigateToLog = { dateEpochDay ->
                        navController.navigate("log/$dateEpochDay")
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Main.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "log/{dateEpochDay}",
                arguments = listOf(navArgument("dateEpochDay") { type = NavType.LongType })
            ) { backStackEntry ->
                val dateEpochDay = backStackEntry.arguments?.getLong("dateEpochDay") ?: return@composable
                val logEntryViewModel: LogEntryViewModel = viewModel()
                LogEntryScreen(
                    dateEpochDay = dateEpochDay,
                    viewModel = logEntryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel()
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
