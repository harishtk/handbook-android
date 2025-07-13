@file:OptIn(ExperimentalCoroutinesApi::class)

package com.handbook.app.feature.home.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.handbook.app.Constant
import com.handbook.app.SharedViewModel
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.presentation.accounts.addaccount.AddAccountRoute
import com.handbook.app.feature.home.presentation.category.AllCategoriesRoute
import com.handbook.app.feature.home.presentation.category.addcategory.AddCategoryRoute
import com.handbook.app.feature.home.presentation.create.CreateRoute
import com.handbook.app.feature.home.presentation.landing.HomeRoute
import com.handbook.app.feature.home.presentation.party.AllPartiesRoute
import com.handbook.app.feature.home.presentation.party.addparty.AddPartyRoute
import com.handbook.app.feature.home.presentation.profile.ProfileRoute
import com.handbook.app.feature.home.presentation.search.SearchRoute
import com.handbook.app.feature.home.presentation.settings.SettingsRoute
import com.handbook.app.feature.home.presentation.webview.WebPageRoute
import com.handbook.app.sharedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

const val HOME_GRAPH_ROUTE_PATTERN = "home_graph"
const val SETTINGS_GRAPH_ROUTE_PATTERN = "settings_graph"
const val PROFILE_GRAPH_ROUTE_PATTERN = "profile_graph"
const val SEARCH_GRAPH_ROUTE_PATTERN = "search_graph"

const val FIRST_LOG_IN = "firstLogin"

const val homeNavigationRoute = "home_route?${FIRST_LOG_IN}={${FIRST_LOG_IN}}"
const val createNavigationRoute = "create_route"
const val USER_ID_ARG = "userId"
const val profileNavigationRoute = "profile_route/{$USER_ID_ARG}"
const val searchNavigationRoute = "search_route"
const val notificationsNavigationRoute = "notification_route"

const val allPartiesNavigationRoute = "all_parties_route"
const val PARTY_ID_ARG = "partyId"
const val addPartyNavigationRoute = "add_party_route/{$PARTY_ID_ARG}"

const val PICKER_MODE_ARG = "pickerMode"
const val CATEGORY_ID_ARG = "categoryId"
const val allCategoriesNavigationRoute = "all_categories_route?categoryId={$CATEGORY_ID_ARG}?pickerMode={$PICKER_MODE_ARG}"
const val addCategoryNavigationRoute = "add_category_route/{$CATEGORY_ID_ARG}"

const val ACCOUNT_ENTRY_ID_ARG = "accountEntryId"
const val ACCOUNT_ENTRY_TRANSACTION_TYPE = "transactionType"
const val addAccountEntryNavigationRoute = "add_account_entry_route/${ACCOUNT_ENTRY_ID_ARG}?transactionType={$ACCOUNT_ENTRY_TRANSACTION_TYPE}"

const val webPageNavigationRoute = "web_page_route?url={url}"
const val settingsNavigationRoute = "settings_route"

const val maintenanceNavigationRoute = "maintenance_route"

private const val DEEP_LINK_URI_PATTERN =
    "https://www.shopsseller.cc/home"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeNavigationRoute, navOptions)
}

fun NavController.navigateToProfile(
    profileId: String = "",
    navOptions: NavOptions? = null
) {
    this.navigate(
        profileNavigationRoute.replace("{$USER_ID_ARG}", profileId),
        navOptions
    )
}

fun NavController.navigateToSearch(
    navOptions: NavOptions? = null
) {
    this.navigate(searchNavigationRoute, navOptions)
}

fun NavController.navigateToNotifications(
    navOptions: NavOptions? = null
) {
    this.navigate(notificationsNavigationRoute, navOptions)
}

fun NavController.navigateToAllParties(
    navOptions: NavOptions? = null
) {
    this.navigate(allPartiesNavigationRoute, navOptions)
}

fun NavController.navigateToAddParty(
    partyId: String? = null,
    navOptions: NavOptions? = null
) {
    this.navigate(addPartyNavigationRoute.replace("{$PARTY_ID_ARG}", partyId ?: ""), navOptions)
}

fun NavController.navigateToAllCategories(
    categoryId: Long = 0L,
    isInPickerMode: Boolean = false,
    navOptions: NavOptions? = null
) {
    this.navigate(allCategoriesNavigationRoute
        .replace("{$CATEGORY_ID_ARG}", categoryId.toString())
        .replace("{$PICKER_MODE_ARG}", isInPickerMode.toString()), navOptions)
}

fun NavController.navigateToAddCategory(
    categoryId: String? = null,
    navOptions: NavOptions? = null
) {
    this.navigate(addCategoryNavigationRoute.replace("{$CATEGORY_ID_ARG}", categoryId ?: ""), navOptions)
}

fun NavController.navigateToAddAccountEntry(
    accountEntryId: String? = null,
    transactionType: String = TransactionType.INCOME.name,
    navOptions: NavOptions? = null
) {
    this.navigate(
        addAccountEntryNavigationRoute
            .replace("{$ACCOUNT_ENTRY_ID_ARG}", accountEntryId ?: "")
            .replace("{$ACCOUNT_ENTRY_TRANSACTION_TYPE}", transactionType),
        navOptions)
}

fun NavController.navigateToCreate(navOptions: NavOptions? = null) {
    this.navigate(createNavigationRoute, navOptions)
}

fun NavController.navigateToWebPage(url: String, navOptions: NavOptions? = null) {
    this.navigate(webPageNavigationRoute.replace("{url}", url), navOptions)
}

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(settingsNavigationRoute, navOptions)
}

fun NavController.navigateToHomeGraph(navOptions: NavOptions? = null) {
    this.navigate(HOME_GRAPH_ROUTE_PATTERN, navOptions)
}

fun NavController.navigateToSettingsGraph(navOptions: NavOptions? = null) {
    this.navigate(SETTINGS_GRAPH_ROUTE_PATTERN, navOptions)
}

fun NavController.navigateToProfileGraph(navOptions: NavOptions? = null) {
    this.navigate(PROFILE_GRAPH_ROUTE_PATTERN, navOptions)
}
fun NavGraphBuilder.homeScreen(
    navController: NavHostController
) {
    composable(
        route = homeNavigationRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
        arguments = listOf(
            navArgument(FIRST_LOG_IN) { defaultValue = "0" }
        ),
    ) {
        val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
        HomeRoute(
            sharedViewModel = sharedViewModel,
            onWritePostRequest = {
                navController.navigateToAddAccountEntry()
            },
            onNavigateToProfile = { userId ->
                navController.navigateToProfile(userId)
            },
            onNavigateToPost = { postId ->
            },
            onNavigateToNotifications = {
                navController.navigateToNotifications()
            }
        )
    }
}

fun NavGraphBuilder.createScreen() {
    composable(
        route = createNavigationRoute,
    ) {
        CreateRoute()
    }
}

fun NavGraphBuilder.webPageScreen(onBackClick: () -> Unit) {
    composable(
        route = webPageNavigationRoute,
        arguments = listOf(
            navArgument("url") {
                type = NavType.StringType
                defaultValue = Constant.LANDING_URL
            }
        ),
    ) { backStackEntry ->
        val url = backStackEntry.arguments?.getString("url") ?: Constant.LANDING_URL
        WebPageRoute(
            url = url,
            onNavUp = onBackClick,
        )
    }
}

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
    openWebPage: (url: String) -> Unit,
) {
    composable(
        route = settingsNavigationRoute,
    ) {
        SettingsRoute(
            onNavUp = onBackClick,
            onOpenWebPage = openWebPage,
        )
    }
}

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = HOME_GRAPH_ROUTE_PATTERN,
        startDestination = homeNavigationRoute,
    ) {
        composable(
            route = homeNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            HomeRoute(
                sharedViewModel = sharedViewModel,
                onWritePostRequest = {
                    navController.navigateToAddAccountEntry()
                },
                onNavigateToProfile = { userId ->
                    navController.navigateToProfile(userId)
                },
                onNavigateToPost = { postId ->
                },
                onNavigateToNotifications = {
                    // navController.navigateToNotifications()
                    navController.navigateToAllParties()
                }
            )
        }

        composable(
            route = allPartiesNavigationRoute,
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            AllPartiesRoute(
                onNavUp = { navController.navigateUp() },
                onAddPartyRequest = { partyId ->
                    navController.navigateToAddParty(partyId = partyId?.toString())
                }
            )
        }

        composable(
            route = addPartyNavigationRoute,
            arguments = listOf(
                navArgument(PARTY_ID_ARG) { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "seller://party/{$PARTY_ID_ARG}"
            })
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            AddPartyRoute(
                onNextPage = { navController.navigateUp() },
            )
        }

        composable(
            route = allCategoriesNavigationRoute,
            arguments = listOf(
                navArgument(CATEGORY_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(PICKER_MODE_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)

            AllCategoriesRoute(
                navController = navController,
                onNavUp = { navController.navigateUp() },
                onAddCategoryRequest = { categoryId ->
                    navController.navigateToAddCategory(categoryId = categoryId?.toString())
                }
            )
        }

        composable(
            route = addCategoryNavigationRoute,
            arguments = listOf(
                navArgument(CATEGORY_ID_ARG) { type = NavType.StringType }
            ),
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            AddCategoryRoute(
                onNextPage = { navController.navigateUp() },
            )
        }

        composable(
            route = addAccountEntryNavigationRoute,
            arguments = listOf(
                navArgument(ACCOUNT_ENTRY_ID_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                                                  },
                navArgument(ACCOUNT_ENTRY_TRANSACTION_TYPE) {
                    type = NavType.StringType
                    defaultValue = TransactionType.INCOME.name
                }
            ),
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            AddAccountRoute(
                navController = navController,
                onNextPage = { navController.navigateUp() },
                onSelectCategoryRequest = { categoryId ->
                    navController.navigateToAllCategories(categoryId, true)
                }
            )
        }

        nestedGraphs()
    }
}

fun NavGraphBuilder.profileGraph(
    startDestination: String = profileNavigationRoute,
    navController: NavController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = PROFILE_GRAPH_ROUTE_PATTERN,
        startDestination = startDestination,
    ) {
        composable(
            route = profileNavigationRoute,
            arguments = listOf(
                navArgument(USER_ID_ARG) { type = NavType.StringType }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "seller://profile/{$USER_ID_ARG}"
            })
        ) {
            ProfileRoute(
                onOptionSettingsRequest = {
                    navController.navigateToSettings()
                }
            )
        }

        nestedGraphs()
    }
}

fun NavGraphBuilder.searchGraph(
    startDestination: String = searchNavigationRoute,
    navController: NavHostController,
    onBackClick: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = SEARCH_GRAPH_ROUTE_PATTERN,
        startDestination = startDestination,
    ) {
        composable(
            route = searchNavigationRoute,
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            SearchRoute(
                sharedViewModel = sharedViewModel,
                onNavUp = onBackClick,
                onNavigateToProfile = { userId ->
                    navController.navigateToProfile(userId)
                },
                onNavigateToPostDetail = { postId ->
                }
            )
        }

        nestedGraphs()
    }
}

fun NavGraphBuilder.settingsGraph(
    onBackClick: () -> Unit,
    onOpenWebPage: (url: String) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit = {},
) {
    navigation(
        route = SETTINGS_GRAPH_ROUTE_PATTERN,
        startDestination = settingsNavigationRoute,
    ) {
        composable(
            route = settingsNavigationRoute,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {
            SettingsRoute(
                onNavUp = onBackClick,
                onOpenWebPage = onOpenWebPage,
            )
        }
        nestedGraphs()
    }
}

fun NavController.navigateToSampleScreen(title: String, navOptions: NavOptions? = null) {
    this.navigate("sample?title={title}".replace("{title}", title), navOptions)
}
