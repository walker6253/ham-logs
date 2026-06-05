package com.hamlog.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
import com.hamlog.ui.screen.StatsScreen
import com.hamlog.viewmodel.LogEntryViewModel
import com.hamlog.viewmodel.MainViewModel
import com.hamlog.viewmodel.SettingsViewModel
import com.hamlog.viewmodel.StatsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Main : Screen("main", "日志", Icons.Default.DateRange)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

private object Routes {
    const val STATS = "stats"
    const val LOG = "log/{dateEpochDay}"
    fun log(dateEpochDay: Long) = "log/$dateEpochDay"
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 0.dp,
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { screen ->
                                val selected = currentRoute == screen.route
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Main.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (selected) 4.dp else 0.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Icon(
                                        screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        screen.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
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
                    onNavigateToLog = { navController.navigate(Routes.log(it)) },
                    onNavigateToSettings = {},
                    onNavigateToStats = { navController.navigate(Routes.STATS) }
                )
            }
            composable(
                Routes.LOG,
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
            composable(Routes.STATS) {
                val vm: StatsViewModel = viewModel()
                StatsScreen(vm, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
