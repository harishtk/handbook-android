package com.handbook.app.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.os.trace
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.handbook.app.bottomBarDestinations
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.feature.home.navigation.homeNavigationRoute
import com.handbook.app.feature.home.navigation.navigateToAllCategories
import com.handbook.app.feature.home.navigation.navigateToAllParties
import com.handbook.app.feature.home.navigation.navigateToHome
import com.handbook.app.feature.home.navigation.navigateToProfile
import com.handbook.app.feature.home.navigation.navigateToSampleScreen
import com.handbook.app.feature.home.navigation.navigateToSearch
import com.handbook.app.feature.home.navigation.navigateToSettings
import com.handbook.app.feature.home.navigation.profileNavigationRoute
import com.handbook.app.navigation.NavigationDrawerDestination
import com.handbook.app.navigation.NavigationDrawerDestination.Settings
import com.handbook.app.navigation.NavigationDrawerDestination.Support
import com.handbook.app.navigation.TopLevelDestination
import com.handbook.app.navigation.TopLevelDestination.HOME
import com.handbook.app.navigation.TopLevelDestination.PROFILE
import timber.log.Timber

@Composable
fun rememberSellerAppState(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): HandbookAppState {
    NavigationTrackingSideEffect(navController)
    return remember(
        navController,
        coroutineScope,
        windowSizeClass,
        networkMonitor,
    ) {
        HandbookAppState(
            navController,
            coroutineScope,
            windowSizeClass,
            networkMonitor
        )
    }
}

@Stable
class HandbookAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            homeNavigationRoute -> HOME
            profileNavigationRoute -> PROFILE
            else -> null
        }

    val shouldShowBottomBar: Boolean
        @Composable get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact &&
                currentDestination/*?.parent?.findStartDestination()*/?.route in bottomBarDestinations

    val shouldShowNavRail: Boolean
        @Composable get() = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact &&
                currentDestination?.route in bottomBarDestinations

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Map of top level destinations to be used in the TopBar, BotttomBar and NavRail. The key is the
     * route.
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * Map of navigation drawer destinations to be used in the Navigation Drawer. The key is the
     * route.
     */
    val navigationDrawerDestinations: List<NavigationDrawerDestination> =
        NavigationDrawerDestination.values().asList()

    /**
     *
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        Timber.tag("Navigation").d("topLevelDestination: ${topLevelDestination.name}")
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // re-selecting the same item
                launchSingleTop = true
                // Restore state when re-selecting a previously selected item
                restoreState = true
            }

            when (topLevelDestination) {
                HOME -> navController.navigateToHome(topLevelNavOptions)
                TopLevelDestination.SEARCH -> navController.navigateToSearch(topLevelNavOptions)
                PROFILE -> navController.navigateToProfile(navOptions = topLevelNavOptions)
            }
        }
    }

    fun navigateToDrawerDestination(navigationDrawerDestination: NavigationDrawerDestination) {
        Timber.tag("Navigation").d("navigationDrawerDestination: ${navigationDrawerDestination.name}")
        trace("NavigationDrawer: ${navigationDrawerDestination.name}") {
            when (navigationDrawerDestination) {
                NavigationDrawerDestination.Parties -> navController.navigateToAllParties()
                NavigationDrawerDestination.Category -> navController.navigateToAllCategories()
                Support -> navController.navigateToSampleScreen(navigationDrawerDestination.name)
                Settings -> navController.navigateToSettings()
            }
        }
    }

}

/**
 * Stores information about navigation events to be used with JankStats
 */
@Composable
private fun NavigationTrackingSideEffect(navController: NavHostController) {
    TrackDisposableJank(navController) { metricsHolder ->
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            metricsHolder.state?.putState("Navigation", destination.route.toString())
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }

    }
}
