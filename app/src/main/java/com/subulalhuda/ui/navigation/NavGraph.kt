package com.subulalhuda.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subulalhuda.data.local.ContentRepository
import com.subulalhuda.data.repository.YouTubeRepository
import com.subulalhuda.ui.screens.*

/**
 * Main navigation graph for the app.
 *
 * Bottom nav: 5 tabs (Home, Lectures, Sheikhs, Interactive, More)
 * Nested screens: Sheikh profile, videos, quizzes, games, search, settings, about, contact
 */
@Composable
fun SubulNavGraph(
    contentRepository: ContentRepository,
    youtubeRepository: YouTubeRepository?,
    isDark: Boolean = false,
    onThemeChanged: (Boolean) -> Unit = {},
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { SubulBottomBar(navController = navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // Primary destinations
            composable(Screen.Home.route) {
                HomeScreen(
                    contentRepository = contentRepository,
                    youtubeRepository = youtubeRepository,
                    onSheikhClick = { sheikhId ->
                        navController.navigate(Screen.SheikhProfile.createRoute(sheikhId))
                    },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.YouTubePlayer.createRoute(videoId))
                    },
                    onLecturesClick = {
                        navController.navigate(Screen.Lectures.route)
                    },
                )
            }

            composable(Screen.Lectures.route) {
                LecturesScreen(
                    contentRepository = contentRepository,
                    youtubeRepository = youtubeRepository,
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.YouTubePlayer.createRoute(videoId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                )
            }

            composable(Screen.Sheikhs.route) {
                SheikhsScreen(
                    contentRepository = contentRepository,
                    onSheikhClick = { sheikhId ->
                        navController.navigate(Screen.SheikhProfile.createRoute(sheikhId))
                    },
                )
            }

            composable(Screen.Interactive.route) {
                InteractiveScreen(
                    contentRepository = contentRepository,
                    onQuizClick = { quizId ->
                        navController.navigate(Screen.Quiz.createRoute(quizId))
                    },
                    onGameClick = { gameId ->
                        navController.navigate(Screen.KidsGame.createRoute(gameId))
                    },
                )
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onContactClick = { navController.navigate(Screen.Contact.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onAboutClick = { navController.navigate(Screen.About.route) },
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                )
            }

            // Nested screens
            composable(
                route = Screen.SheikhProfile.route,
                arguments = listOf(navArgument("sheikhId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val sheikhId = backStackEntry.arguments?.getString("sheikhId") ?: return@composable
                SheikhProfileScreen(
                    sheikhId = sheikhId,
                    contentRepository = contentRepository,
                    youtubeRepository = youtubeRepository,
                    onVideoClick = { videoId, title ->
                        navController.navigate(Screen.YouTubePlayer.createRoute(videoId, title))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.Quiz.route,
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType },
                    navArgument("difficulty") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("count") {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                val difficulty = backStackEntry.arguments?.getString("difficulty")
                val count = backStackEntry.arguments?.getInt("count") ?: 0
                QuizScreen(
                    quizId = quizId,
                    contentRepository = contentRepository,
                    initialDifficulty = difficulty,
                    initialCount = count,
                    onQuizComplete = { score, total, diff, cnt ->
                        navController.navigate(
                            Screen.QuizResult.createRoute(quizId, score, total, diff, cnt),
                        ) {
                            popUpTo(Screen.Quiz.createRoute(quizId)) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.QuizResult.route,
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType },
                    navArgument("score") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType },
                    navArgument("difficulty") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("count") {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
                val score = backStackEntry.arguments?.getInt("score") ?: 0
                val total = backStackEntry.arguments?.getInt("total") ?: 0
                val difficulty = backStackEntry.arguments?.getString("difficulty")
                val count = backStackEntry.arguments?.getInt("count") ?: 0
                QuizResultScreen(
                    quizId = quizId,
                    score = score,
                    total = total,
                    difficulty = difficulty,
                    count = count,
                    contentRepository = contentRepository,
                    onRetry = {
                        navController.navigate(
                            Screen.Quiz.createRoute(quizId, difficulty, count),
                        ) {
                            popUpTo(Screen.QuizResult.route) { inclusive = true }
                        }
                    },
                    onNewQuiz = {
                        navController.navigate(Screen.Quiz.createRoute(quizId)) {
                            popUpTo(Screen.QuizResult.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.KidsGame.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                KidsGameScreen(
                    gameId = gameId,
                    contentRepository = contentRepository,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.YouTubePlayer.route,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType },
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
                val title = backStackEntry.arguments?.getString("title")
                VideoPlayerScreen(
                    videoId = videoId,
                    title = title,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    contentRepository = contentRepository,
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.YouTubePlayer.createRoute(videoId))
                    },
                    onSheikhClick = { sheikhId ->
                        navController.navigate(Screen.SheikhProfile.createRoute(sheikhId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Contact.route) {
                ContactScreen(
                    contentRepository = contentRepository,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    isDark = isDark,
                    onThemeChanged = onThemeChanged,
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
fun SubulBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom bar on primary destinations
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    if (!showBottomBar) return

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}
