package com.handbook.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.handbook.app.feature.home.navigation.HOME_GRAPH_ROUTE_PATTERN
import com.handbook.app.feature.home.navigation.homeGraph
import com.handbook.app.feature.home.navigation.navigateToWebPage
import com.handbook.app.feature.home.navigation.profileGraph
import com.handbook.app.feature.home.navigation.searchGraph
import com.handbook.app.feature.home.navigation.settingsGraph
import com.handbook.app.feature.home.navigation.webPageScreen
import com.handbook.app.feature.home.presentation.miscellaneous.SampleRoute
import com.handbook.app.ui.HandbookAppState
import timber.log.Timber

@Composable
fun HandbookNavHost(
    appState: HandbookAppState,
    onShowSnackBar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startGraph: String = HOME_GRAPH_ROUTE_PATTERN,
    startDestination: String = "",
) {
    Timber.d("HandbookNavHost() called with: appState = [$appState], onShowSnackBar = [$onShowSnackBar], modifier = [$modifier], startDestination = [$startDestination]")
    val navController = appState.navController

    NavHost(
        navController = navController,
        startDestination = startGraph,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        modifier = modifier
    ) {
        // TODO: add navigation items
        homeGraph(
            navController = navController,
            onBackClick = navController::popBackStack,
        ) {
            settingsGraph(
                onBackClick = navController::popBackStack,
                onOpenWebPage = { url ->
                    navController.navigateToWebPage(url)
                },
            ) {
                // TODO: add nested graph items
                // TODO: 1. Add web page preview
                webPageScreen(
                    onBackClick = navController::popBackStack
                )
            }
        }

        profileGraph(
            navController = navController,
            onBackClick = navController::popBackStack,
        ) {}

        searchGraph(
            navController = navController,
            onBackClick = navController::popBackStack,
        ) {}

        // TODO: 1. Add maintenance graph
        // TODO: 2. Add force-update graph

        composable(
            route = "sample?title={title}",
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = "Sample"
                }
            ),
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Sample"
            SampleRoute(title = title)
        }

    }
}

private const val TIME_DURATION = 300

val fragmentCloseEnterTransition: EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 50,
            delayMillis = 66, // This is the startOffset
            easing = LinearEasing
        ),
        initialAlpha = 0f
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = 300,
            // delayMillis = 0, // Default, scale starts immediately with the group unless specified otherwise
            easing = FastOutSlowInEasing // Approximates @anim/fragment_fast_out_extra_slow_in
        ),
        initialScale = 1.1f,
        transformOrigin = TransformOrigin.Center // Corresponds to pivotX="50%" pivotY="50%"
    )

val fragmentCloseExitTransition: ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 50,    // from alpha animation
            delayMillis = 66,       // from alpha animation (startOffset)
            easing = LinearEasing   // from alpha animation
        ),
        targetAlpha = 0f            // toAlpha="0.0"
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = 300,   // from scale animation
            // delayMillis = 0,    // scale animation starts immediately within the set
            easing = FastOutSlowInEasing // Equivalent to @anim/fragment_fast_out_extra_slow_in
        ),
        targetScale = 0.9f,         // toXScale="0.9", toYScale="0.9"
        transformOrigin = TransformOrigin.Center // pivotX="50%", pivotY="50%"
    )

val fragmentOpenEnterTransition: EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = 50,    // from alpha animation
            delayMillis = 35,       // from alpha animation (startOffset)
            easing = LinearEasing   // from alpha animation
        ),
        initialAlpha = 0f           // fromAlpha="0"
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = 300,   // from scale animation
            // delayMillis = 0,    // scale animation starts immediately within the set
            easing = FastOutSlowInEasing // Equivalent to @anim/fragment_fast_out_extra_slow_in
        ),
        initialScale = 0.85f,       // fromXScale="0.85", fromYScale="0.85"
        transformOrigin = TransformOrigin.Center // pivotX="50%", pivotY="50%"
    )

val fragmentOpenExitTransition: ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 50,    // from alpha animation
            delayMillis = 35,       // from alpha animation (startOffset)
            easing = LinearEasing   // from alpha animation
        ),
        targetAlpha = 0f            // toAlpha="0.0"
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = 300,   // from scale animation
            // delayMillis = 0,    // scale animation starts immediately within the set
            easing = FastOutSlowInEasing // Equivalent to @anim/fragment_fast_out_extra_slow_in
        ),
        targetScale = 1.15f,
        transformOrigin = TransformOrigin.Center
    )

val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
    { fragmentOpenEnterTransition }

val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
    { fragmentOpenExitTransition }

val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
    { fragmentCloseEnterTransition }

val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
    { fragmentCloseExitTransition }