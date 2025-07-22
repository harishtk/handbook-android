package com.handbook.app.feature.onboard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.handbook.app.feature.onboard.presentation.login.LoginRoute

const val AUTH_GRAPH_ROUTE_PATTERN = "auth_graph"

const val loginNavigationRoute = "login"

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = AUTH_GRAPH_ROUTE_PATTERN,
        startDestination = loginNavigationRoute,
    ) {
        composable(
            route = loginNavigationRoute,
        ) {
            LoginRoute()
        }
    }
}