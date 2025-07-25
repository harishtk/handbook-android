@file:OptIn(ExperimentalCoroutinesApi::class)

package com.handbook.app.feature.home.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHostState
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
import com.handbook.app.feature.home.presentation.backup.BackupRestoreRoute
import com.handbook.app.feature.home.presentation.backup.BackupRestoreScreen
import com.handbook.app.feature.home.presentation.bank.AllBanksRoute
import com.handbook.app.feature.home.presentation.bank.addbank.AddBankRoute
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
import com.handbook.app.hiltActivityViewModel
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

const val PARTY_ID = "partyId"
const val PICKER_MODE = "pickerMode"
const val allPartiesNavigationRoute = "all_parties_route?$PARTY_ID={$PARTY_ID}?$PICKER_MODE={$PICKER_MODE}"

const val addPartyNavigationRoute = "add_party_route?$PARTY_ID={$PARTY_ID}"
const val CATEGORY_ID = "categoryId"
const val TRANSACTION_TYPE = "transactionType"
const val allCategoriesNavigationRoute = "all_categories_route?$CATEGORY_ID={$CATEGORY_ID}&$PICKER_MODE={$PICKER_MODE}?$TRANSACTION_TYPE={$TRANSACTION_TYPE}"
const val addCategoryNavigationRoute = "add_category_route?$CATEGORY_ID={$CATEGORY_ID}?$TRANSACTION_TYPE={$TRANSACTION_TYPE}"

const val BANK_ID = "bankId"
const val allBanksNavigationRoute = "all_banks_route?$BANK_ID={$BANK_ID}&$PICKER_MODE={$PICKER_MODE}"
const val addBankNavigationRoute = "add_bank_route?$BANK_ID={$BANK_ID}"

const val ACCOUNT_ENTRY_ID = "accountEntryId"
const val ACCOUNT_ENTRY_TRANSACTION_TYPE = "transactionType"
const val addAccountEntryNavigationRoute = "add_account_entry_route?$ACCOUNT_ENTRY_ID={$ACCOUNT_ENTRY_ID}&$ACCOUNT_ENTRY_TRANSACTION_TYPE={$ACCOUNT_ENTRY_TRANSACTION_TYPE}"

const val backupAndRestoreNavigationRoute = "backup_and_restore"

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
    partyId: Long = 0L,
    isInPickerMode: Boolean = false,
    navOptions: NavOptions? = null
) {
    this.navigate(allPartiesNavigationRoute
        .replace("{$PARTY_ID}", partyId.toString())
        .replace("{$PICKER_MODE}", isInPickerMode.toString()), navOptions)
}

fun NavController.navigateToAddParty(
    partyId: String? = null,
    navOptions: NavOptions? = null
) {
    this.navigate(addPartyNavigationRoute.replace("{$PARTY_ID}", partyId ?: ""), navOptions)
}

fun NavController.navigateToAllCategories(
    categoryId: Long = 0L,
    transactionType: String? = "",
    isInPickerMode: Boolean = false,
    navOptions: NavOptions? = null
) {
    this.navigate(allCategoriesNavigationRoute
        .replace("{$CATEGORY_ID}", categoryId.toString())
        .replace("{$PICKER_MODE}", isInPickerMode.toString())
        .replace("{$TRANSACTION_TYPE}", transactionType ?: ""), navOptions)
}

fun NavController.navigateToAddCategory(
    categoryId: String? = null,
    transactionType: TransactionType = TransactionType.INCOME,
    navOptions: NavOptions? = null
) {
    this.navigate(addCategoryNavigationRoute
        .replace("{$CATEGORY_ID}", categoryId ?: "")
        .replace("{$TRANSACTION_TYPE}", transactionType.name),
        navOptions)
}

fun NavController.navigateToAllBanks(
    bankId: Long = 0L,
    isInPickerMode: Boolean = false,
    navOptions: NavOptions? = null
) {
    this.navigate(allBanksNavigationRoute
        .replace("{$BANK_ID}", bankId.toString())
        .replace("{$PICKER_MODE}", isInPickerMode.toString()), navOptions)
}

fun NavController.navigateToAddBank(
    bankId: String? = null,
    navOptions: NavOptions? = null
) {
    this.navigate(addBankNavigationRoute.replace("{$BANK_ID}", bankId ?: ""), navOptions)
}

fun NavController.navigateToAddAccountEntry(
    accountEntryId: String? = null,
    transactionType: String = TransactionType.INCOME.name,
    navOptions: NavOptions? = null
) {
    this.navigate(
        addAccountEntryNavigationRoute
            .replace("{$ACCOUNT_ENTRY_ID}", accountEntryId ?: "")
            .replace("{$ACCOUNT_ENTRY_TRANSACTION_TYPE}", transactionType),
        navOptions)
}

fun NavController.navigateToBackupAndRestore(
    navOptions: NavOptions? = null
) {
    this.navigate(backupAndRestoreNavigationRoute, navOptions)
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
        val sharedViewModel: SharedViewModel = hiltActivityViewModel<SharedViewModel>()
        HomeRoute(
            navController = navController,
            sharedViewModel = sharedViewModel,
            onAddEntryRequest = { entryId, transactionType ->
                navController.navigateToAddAccountEntry(
                    accountEntryId = entryId.toString(),
                    transactionType = transactionType.name
                )
            },
            onNavigateToNotifications = {
                navController.navigateToNotifications()
            },
            onSelectPartyRequest = { partyId ->
                navController.navigateToAllParties(partyId, true)
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
            val sharedViewModel: SharedViewModel = hiltActivityViewModel<SharedViewModel>()
            HomeRoute(
                navController = navController,
                sharedViewModel = sharedViewModel,
                onAddEntryRequest = { entryId, transactionType ->
                    navController.navigateToAddAccountEntry(
                        accountEntryId = entryId.toString(),
                        transactionType = transactionType.name
                    )
                },
                onNavigateToNotifications = {
                    // navController.navigateToNotifications()
                    navController.navigateToAllParties()
                },
                onSelectPartyRequest = { partyId ->
                    navController.navigateToAllParties(partyId, true)
                }
            )
        }

        composable(
            route = allPartiesNavigationRoute,
            arguments = listOf(
                navArgument(PARTY_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(PICKER_MODE) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            AllPartiesRoute(
                onNavUp = { navController.navigateUp() },
                navController = navController,
                onAddPartyRequest = { partyId ->
                    navController.navigateToAddParty(partyId = partyId?.toString())
                }
            )
        }

        composable(
            route = addPartyNavigationRoute,
            arguments = listOf(
                navArgument(PARTY_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "seller://party/${PARTY_ID}"
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
                navArgument(CATEGORY_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(PICKER_MODE) {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument(TRANSACTION_TYPE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            )
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)

            AllCategoriesRoute(
                navController = navController,
                onNavUp = { navController.navigateUp() },
                onAddCategoryRequest = { categoryId, transactionType ->
                    navController.navigateToAddCategory(categoryId = categoryId?.toString(), transactionType = transactionType)
                }
            )
        }

        composable(
            route = addCategoryNavigationRoute,
            arguments = listOf(
                navArgument(CATEGORY_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(TRANSACTION_TYPE) {
                    type = NavType.EnumType(TransactionType::class.java)
                    defaultValue = TransactionType.INCOME
                }
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
                navArgument(ACCOUNT_ENTRY_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
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
                onSelectCategoryRequest = { categoryId, transactionType ->
                    navController.navigateToAllCategories(categoryId, transactionType = transactionType.name, true)
                },
                onSelectPartyRequest = { partyId ->
                    navController.navigateToAllParties(partyId, true)
                },
                onSelectBankRequest = { bankId ->
                    navController.navigateToAllBanks(bankId, true)
                }
            )
        }

        composable(
            route = allBanksNavigationRoute,
            arguments = listOf(
                navArgument(BANK_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(PICKER_MODE) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
            /* TODO: add deep links and other args here */
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)

            AllBanksRoute(
                navController = navController,
                onNavUp = { navController.navigateUp() },
                onAddBankRequest = { bankId ->
                    navController.navigateToAddBank(bankId = bankId?.toString())
                }
            )
        }

        composable(
            route = addBankNavigationRoute,
            arguments = listOf(
                navArgument(BANK_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
        ) {
            val sharedViewModel = it.sharedViewModel<SharedViewModel>(navController)
            AddBankRoute(
                onNextPage = { navController.navigateUp() },
            )
        }

        composable(
            route = backupAndRestoreNavigationRoute,
        ) {
            BackupRestoreRoute(
                onNavUp = { navController.popBackStack() }
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
