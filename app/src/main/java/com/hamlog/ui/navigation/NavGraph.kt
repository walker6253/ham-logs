package com.hamlog.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

private const val ANIM_DURATION = 250

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Main.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(ANIM_DURATION)) +
                    slideInHorizontally(
                        animationSpec = tween(ANIM_DURATION),
                        initialOffsetX = { it / 8 }
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(ANIM_DURATION)) +
                    slideOutHorizontally(
                        animationSpec = tween(ANIM_DURATION),
                        targetOffsetX = { -it / 8 }
                    )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(ANIM_DURATION)) +
                    slideInHorizontally(
                        animationSpec = tween(ANIM_DURATION),
                        initialOffsetX = { -it / 8 }
                    )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(ANIM_DURATION)) +
                    slideOutHorizontally(
                        animationSpec = tween(ANIM_DURATION),
                        targetOffsetX = { it / 8 }
                    )
            }
        ) {
            composable(Screen.Main.route) {
                val vm: MainViewModel = viewModel()
                MainScreen(
                    vm,
                    onNavigateToLog = { navController.navigate("log/$it") },
                    onNavigateToSettings = {}
                )
            }
            composable(
                "log/{dateEpochDay}",
                arguments = listOf(navArgument("dateEpochDay") { type = NavType.LongType })
            ) { entry ->
                val date = entry.arguments?.getLong("dateEpochDay") ?: return@composable
                val vm: LogEntryViewModel = viewModel()
                LogEntryScreen(date, vm, { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel()
                SettingsScreen(vm)
            }
        }
    }
}
