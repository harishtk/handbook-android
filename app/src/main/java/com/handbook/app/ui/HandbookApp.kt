package com.handbook.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import com.handbook.app.R
import com.handbook.app.SharedViewModel
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.LocalWindowSizeClass
import com.handbook.app.core.designsystem.component.DefaultNavigationDrawer
import com.handbook.app.core.designsystem.component.HandbookBackground
import com.handbook.app.core.designsystem.component.HandbookGradientBackground
import com.handbook.app.core.designsystem.component.HandbookNavigationBar
import com.handbook.app.core.designsystem.component.HandbookNavigationBarItem
import com.handbook.app.core.designsystem.component.HandbookTopAppBar
import com.handbook.app.core.domain.model.DarkThemeConfig
import com.handbook.app.core.domain.model.LoginUser
import com.handbook.app.core.domain.model.ShopData
import com.handbook.app.core.domain.model.ThemeBrand
import com.handbook.app.core.domain.model.UserData
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.domain.usecase.UserAuthStateUseCase
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.feature.home.navigation.HOME_GRAPH_ROUTE_PATTERN
import com.handbook.app.navigation.HandbookNavHost
import com.handbook.app.navigation.NavigationDrawerDestination
import com.handbook.app.navigation.TopLevelDestination
import com.handbook.app.ui.theme.GradientColors
import com.handbook.app.ui.theme.LocalGradientColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun HandbookApp(
    windowSizeClass: WindowSizeClass,
    networkMonitor: NetworkMonitor,
    sharedViewModel: SharedViewModel,
    appState: HandbookAppState = rememberSellerAppState(
        windowSizeClass = windowSizeClass,
        networkMonitor = networkMonitor,
    ),
    startGraph: String,
    startDestination: String,
) {
    Timber.d("NavHost: startDestination=$startDestination")
    val shouldShowGradientBackground = true
    /*appState.currentTopLevelDestination == TopLevelDestination.HOME*/

    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    CompositionLocalProvider(
        LocalWindowSizeClass provides appState.windowSizeClass
    ) {
        HandbookBackground {
            HandbookGradientBackground(
                gradientColors = if (shouldShowGradientBackground) {
                    LocalGradientColors.current
                } else {
                    GradientColors()
                },
            ) {
                val snackbarHostState = remember { SnackbarHostState() }

                val isOffline by appState.isOffline.collectAsStateWithLifecycle()

                // If user is not connected to the internet show a snack bar to inform them.
                val notConnectedMessage = stringResource(R.string.you_are_not_connected_to_the_internet)
                /*LaunchedEffect(isOffline) {
                    if (isOffline) {
                        snackbarHostState.showSnackbar(
                            message = notConnectedMessage,
                            duration = SnackbarDuration.Indefinite,
                        )
                    }
                }*/

                if (showSettingsDialog) {
                    // TODO: show settings dialog
                }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var drawerWidth by remember { mutableFloatStateOf(drawerState.offset.value) }
                Timber.d("NavigationDrawer: offset=${drawerState.offset}")

                // As soon the user move the drawer, the content must move in sync.
                // So here we're creating a derived state of the drawer state
                // to update the content position.
                val contentOffset = remember {
                    derivedStateOf {
                        drawerState.offset.value
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier
                        ) {
                            /* Drawer Content */
                            ShopsNavigationDrawer(
                                destinations = appState.navigationDrawerDestinations,
                                onNavigateToDestination = { destination ->
                                    appState.navigateToDrawerDestination(destination)
                                    scope.launch {
                                        drawerState.apply {
                                            if (isOpen) close()
                                        }
                                    }
                                },
                                currentDestination = appState.currentDestination,
                                modifier = Modifier.testTag("ShopsNavigationDrawer")
                            )
                        }
                    },
                    gesturesEnabled = drawerState.isOpen
                ) {
                    Scaffold(
                        modifier = Modifier
                            // .navigationBarsPadding()
                            .semantics {
                                testTagsAsResourceId = true
                            }
                        ,
                        // containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
//                        bottomBar = {
//                            AnimatedVisibility(
//                                visible = appState.shouldShowBottomBar,
//                                enter = slideInVertically(
//                                    initialOffsetY = { height ->
//                                        height / 2
//                                    }
//                                ),
//                                exit = slideOutVertically(
//                                    targetOffsetY = { height -> height },
//                                    animationSpec = tween(
//                                        durationMillis = 175,
//                                        easing = FastOutLinearInEasing
//                                    )
//                                ),
//                            ) {
//                                ShopsBottomBar(
//                                    destinations = appState.topLevelDestinations,
//                                    onNavigateToDestination = appState::navigateToTopLevelDestination,
//                                    currentDestination = appState.currentDestination,
//                                    modifier = Modifier.testTag("ShopsBottomBar"),
//                                )
//                            }
//                        },
                    ) { padding ->
                        val xPos = (abs(drawerWidth) - abs(contentOffset.value))
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .consumeWindowInsets(padding)
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Horizontal,
                                    ),
                                )
                            /*.offset(
                                x = with (LocalDensity.current) {
                                    max(0.dp, xPos.toDp() - 56.dp)
                                }
                            )*/,
                        ) {
                            if (appState.shouldShowNavRail) {
                                ShopsNavRail(
                                    destinations = appState.topLevelDestinations,
                                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                                    currentDestination = appState.currentDestination,
                                    modifier = Modifier
                                        .testTag("ShopsNavRail")
                                        .safeDrawingPadding(),
                                )
                            }

                            Column(Modifier.fillMaxSize()) {
                                // Show the top app bar on top level destinations.
                                val destination = appState.currentTopLevelDestination
                                if (destination != null) {
                                    HandbookTopAppBar(
                                        modifier = Modifier
                                            .shadow(4.dp),
                                        title = @Composable {
                                            Text(
                                                text = "Timeline",
                                                style = MaterialTheme.typography.titleLarge
                                                    .copy(fontWeight = FontWeight.W700)
                                            )
                                        },
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                scope.launch {
                                                    drawerState.apply {
                                                        if (isClosed) open() else close()
                                                    }
                                                }
                                            }) {
                                                Icon(
                                                    painter = painterResource(id = HandbookIcons.Id_Breadcrumbs),
                                                    contentDescription = "Open Drawer",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        },
                                        actions = {
                                            IconButton(onClick = {}) {
                                                Icon(
                                                    imageVector = HandbookIcons.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                    )
                                }

                                HandbookNavHost(
                                    modifier = Modifier.weight(1F),
                                    appState = appState,
                                    onShowSnackBar = { message, action ->
                                        snackbarHostState.showSnackbar(
                                            message = message,
                                            actionLabel = action,
                                            duration = SnackbarDuration.Short,
                                        ) == SnackbarResult.ActionPerformed
                                    },
                                    startGraph = startGraph,
                                    startDestination = startDestination
                                )

                                AnimatedVisibility(
                                    visible = isOffline,
                                    enter = slideInVertically(
                                        animationSpec = tween(
                                            200,
                                        )
                                    ) { fullHeight ->
                                        fullHeight / 3
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1F)
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text(
                                            text = notConnectedMessage,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = insetMedium)
                                        )
                                    }
                                }
                            }

                            // TODO: We may want to add padding or spacer when the snackbar is shown so that
                            //  content doesn't display behind it.
                        }
                    }
                }

                SideEffect {
                    if (drawerWidth == 0f) {
                        drawerWidth = drawerState.currentOffset
                    }
                }

                BackHandler(
                    enabled = drawerState.isOpen
                ) {
                    scope.launch {
                        drawerState.apply {
                            if (isOpen) {
                                close()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Deprecated("Not yet implemented")
@Composable
private fun ShopsNavRail(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    /*ShopsNavigationRail(modifier) {
        destinations.forEach { destination ->
            val hasUnread = true
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            ShopsNavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconTextId)) },
                modifier = if (hasUnread) Modifier.notificationDot() else modifier,
            )
        }
    }*/
}

@Composable
private fun ShopsBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    HandbookNavigationBar(
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val hasUnread = false
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)

            if (destination.titleTextId == null) {
                HandbookNavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            contentDescription = null,
                        )
                    },
                    modifier = if (hasUnread) Modifier.notificationDot() else modifier,
                )
            } else {
                HandbookNavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.unselectedIcon),
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            painter = painterResource(id = destination.selectedIcon),
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) },
                    modifier = if (hasUnread) Modifier.notificationDot() else modifier,
                )
            }
        }
    }
}

@Composable
private fun ShopsNavigationDrawer(
    destinations: List<NavigationDrawerDestination>,
    onNavigateToDestination: (NavigationDrawerDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    val shopData = ShopData(
        id = "0",
        name = "Ganesh Homes",
        thumbnail = "",
        category = "Construction & Consulting",
        description = "A construction and consulting company",
        address = "625012",
        image = "",
    )
    DefaultNavigationDrawer(
        shopData = shopData,
        destinations = destinations,
        onNavigateToDestination = onNavigateToDestination,
        currentDestination = currentDestination,
        modifier = modifier,
    )
}

private fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center = center + Offset(
                    64.dp.toPx() * .45f,
                    32.dp.toPx() * -.45f - 6.dp.toPx(),
                ),
            )
        }
    }


private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination): Boolean {
    val stackString = this?.hierarchy?.map { it.route }?.joinToString("->") ?: ""
    Timber.tag("Navigation").d("Hierarchy stack $stackString")
    return this?.hierarchy
        ?.filterNot { it.route?.contains("_graph") == true }
        ?.any {
            it.route?.contains(destination.name, ignoreCase = true) ?: false
        } ?: false
}