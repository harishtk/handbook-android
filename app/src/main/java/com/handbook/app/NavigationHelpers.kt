package com.handbook.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * Obtains an instance of the view model with the [ComponentActivity] as the view model
 * store owner.
 */
@Composable
inline fun <reified T : ViewModel> activityViewModel(): T =
    viewModel(modelClass = T::class.java, viewModelStoreOwner = LocalActivity.current as ComponentActivity)

@Composable
inline fun <reified T : ViewModel> hiltActivityViewModel(): T =
    hiltViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)

/**
 * The [ViewModel] obtained with this helper is scoped to the parent graph or destination. So that
 * the consecutive children will receive the same instance of the view model, just like
 * navGraphViewModel
 */
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavHostController
): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}