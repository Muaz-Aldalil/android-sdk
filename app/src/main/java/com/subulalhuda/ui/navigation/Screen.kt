package com.subulalhuda.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the app.
 *
 * Five primary bottom-navigation destinations:
 * 1. الرئيسية (Home)
 * 2. الدروس (Lectures)
 * 3. العلماء (Sheikhs)
 * 4. التفاعلي (Interactive)
 * 5. المزيد (More)
 *
 * Nested screens (accessed from primary screens):
 * - SheikhProfile, SheikhVideos
 * - Quiz, QuizResult, KidsGame
 * - YouTubePlayer
 * - Search
 * - Contact, Settings, About
 */
sealed class Screen(val route: String) {
    // Primary destinations (bottom nav)
    data object Home : Screen("home")
    data object Lectures : Screen("lectures")
    data object Sheikhs : Screen("sheikhs")
    data object Interactive : Screen("interactive")
    data object More : Screen("more")

    // Nested screens
    data object SheikhProfile : Screen("sheikh/{sheikhId}") {
        fun createRoute(sheikhId: String) = "sheikh/$sheikhId"
    }

    data object SheikhVideos : Screen("sheikh/{sheikhId}/videos") {
        fun createRoute(sheikhId: String) = "sheikh/$sheikhId/videos"
    }

    data object Quiz : Screen("quiz/{quizId}") {
        fun createRoute(quizId: String) = "quiz/$quizId"
    }

    data object QuizResult : Screen("quiz/{quizId}/result?score={score}&total={total}") {
        fun createRoute(quizId: String, score: Int, total: Int) =
            "quiz/$quizId/result?score=$score&total=$total"
    }

    data object KidsGame : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }

    data object YouTubePlayer : Screen("video/{videoId}") {
        fun createRoute(videoId: String) = "video/$videoId"
    }

    data object Search : Screen("search")
    data object Contact : Screen("contact")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}

/**
 * Bottom navigation items.
 * Matches the 5-tab layout: الرئيسية, الدروس, العلماء, التفاعلي, المزيد
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "الرئيسية",
        icon = Icons.Default.Home,
        route = Screen.Home.route,
    ),
    BottomNavItem(
        label = "الدروس",
        icon = Icons.Default.MenuBook,
        route = Screen.Lectures.route,
    ),
    BottomNavItem(
        label = "العلماء",
        icon = Icons.Default.People,
        route = Screen.Sheikhs.route,
    ),
    BottomNavItem(
        label = "التفاعلي",
        icon = Icons.Default.Quiz,
        route = Screen.Interactive.route,
    ),
    BottomNavItem(
        label = "المزيد",
        icon = Icons.Default.MoreVert,
        route = Screen.More.route,
    ),
)
