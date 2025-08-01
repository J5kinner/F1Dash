package com.jskinner.f1dash.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import f1dash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

sealed class F1Destinations(
    val route: String,
    val icon: ImageVector
) {
    data object Race : F1Destinations("race", Icons.Default.Flag)
    data object PreviousRaces : F1Destinations("previous_races", Icons.Default.CalendarMonth)
    data object Teams : F1Destinations("teams", Icons.Default.Group)
    data object Stats : F1Destinations("stats", Icons.Default.BarChart)
    data object Replay : F1Destinations("replay", Icons.Default.Flag)
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
            val titleResource = when (destination) {
                F1Destinations.Race -> Res.string.navigation_race
                F1Destinations.PreviousRaces -> Res.string.navigation_previous_races
                F1Destinations.Teams -> Res.string.navigation_teams
                F1Destinations.Stats -> Res.string.navigation_stats
                F1Destinations.Replay -> Res.string.navigation_replay
            }
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(titleResource)
                    )
                },
                label = {
                    Text(
                        text = stringResource(titleResource),
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