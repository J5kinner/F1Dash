package com.jskinner.f1dash.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class F1Destinations(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Race : F1Destinations("race", "Race", Icons.Default.Flag)
    data object PreviousRaces : F1Destinations("previous_races", "Previous Races", Icons.Default.CalendarMonth)
    data object Teams : F1Destinations("teams", "Teams", Icons.Default.Group)
    data object Stats : F1Destinations("stats", "Stats", Icons.Default.BarChart)
    data object Replay : F1Destinations("replay", "Replay", Icons.Default.Flag)
}

val bottomNavItems = listOf(
    F1Destinations.Race,
    F1Destinations.PreviousRaces,
//    F1Destinations.Teams,
//    F1Destinations.Stats
)

@Composable
fun F1BottomNavigation(
    navController: NavController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        val startDestination = navController.graph.findStartDestination()
                        popUpTo(startDestination.route ?: destination.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}