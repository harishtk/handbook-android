package com.handbook.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.metrics.performance.JankStats
import androidx.profileinstaller.ProfileVerifier
import com.google.gson.GsonBuilder
import com.handbook.app.common.util.GsonParser
import com.handbook.app.common.util.JsonParser
import com.handbook.app.core.util.NetworkMonitor
import com.handbook.app.ui.theme.HandbookTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.handbook.app.core.domain.model.DarkThemeConfig
import com.handbook.app.core.domain.model.ThemeBrand
import com.handbook.app.feature.home.navigation.HOME_GRAPH_ROUTE_PATTERN
import com.handbook.app.feature.onboard.navigation.AUTH_GRAPH_ROUTE_PATTERN
import com.handbook.app.ui.HandbookApp
import timber.log.Timber
import javax.inject.Inject

val defaultJsonParser: JsonParser
    get() = GsonParser(GsonBuilder().create())

private const val TAG = "MainActivity"

/**
 * TODO: 1. Refactor userViewModel for shared view model approach using LocalCompositionProvider
 * TODO: 2. Handle conditional nav graph for not authenticated state.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * Lazily inject [JankStats], which is used to track jank throughout the app.
     */
    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    val viewModel: MainActivityViewModel by viewModels()
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                    }
                    .collect()
            }
        }

        // Keep the splash screen on-screen until the UI state is loaded. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                else -> false
            }
        }


        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations, and go edge-to-edge
        // This also sets up the initial system bar style based on the platform theme
        enableEdgeToEdge()

        var startDestination: String by mutableStateOf("")
        var startGraph: String? by mutableStateOf(null)

        // TODO: Check force-update flag and validate against play store availability
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    Timber.tag(TAG).d("state=$uiState")
                    when (uiState) {
                        MainActivityUiState.Loading -> { /* no-op */ }
                        MainActivityUiState.Maintenance -> {
                            Timber.tag(TAG).d("Start destination: maintenance")
                            startGraph = "maintenance_graph"
                        }
                        is MainActivityUiState.Login -> {
                            Timber.tag(TAG).d("Start destination: login")
                            startGraph = AUTH_GRAPH_ROUTE_PATTERN
                        }
                        is MainActivityUiState.Success -> {
                            uiState?.data?.let { userData ->
                                Timber.d("OnboardStep: ${userData.onboardStep}")
//                                when (uiState.data.onboardStep) {
//                                    // TODO: add onboard step redirections
//                                    "store" -> {
//                                        startGraph = ONBOARD_GRAPH_ROUTE_PATTERN
//                                        startDestination = addStoreRoute
//                                    }
//                                    "product" -> {
//                                        startGraph = ONBOARD_GRAPH_ROUTE_PATTERN
//                                        startDestination = addProductRoute
//                                    }
//                                    "bank" -> {
//                                        startGraph = ONBOARD_GRAPH_ROUTE_PATTERN
//                                        startDestination = addBankAccountRoute
//                                    }
//                                    "launch" -> {
//                                        startGraph = ONBOARD_GRAPH_ROUTE_PATTERN
//                                        startDestination = launchStoreRoute
//                                    }
//                                    else -> startGraph = HOME_GRAPH_ROUTE_PATTERN
//                                }
                                startGraph = HOME_GRAPH_ROUTE_PATTERN
                            }
                        }
                    }
                }
            }
        }

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)

            // Update the edge to edge configuration to match the theme
            // This is the same parameters as the default enableEdgeToEdge call, but we manually
            // resolve whether or not to show dark theme using uiState, since it can be different
            // than the configuration's dark theme value based on the user preference.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme }
                )
                onDispose {}
            }

            CompositionLocalProvider {
                HandbookTheme(
                    darkTheme = darkTheme,
                    androidTheme = shouldUseAndroidTheme(uiState),
                    disableDynamicTheming = shouldDisableDynamicTheming(uiState),
                ) {
                    // TODO: can we do this in other way
                    startGraph?.let { graph ->
                        HandbookApp(
                            windowSizeClass = calculateWindowSizeClass(activity = this),
                            networkMonitor = networkMonitor,
                            sharedViewModel = sharedViewModel,
                            startGraph = graph,
                            startDestination = startDestination,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lazyStats.get().isTrackingEnabled = true
        lifecycleScope.launch {
            logCompilationStatus()
        }
    }

    override fun onPause() {
        super.onPause()
        lazyStats.get().isTrackingEnabled = false
    }

    /**
     * Logs the app's Baseline Profile Compilation Status using [ProfileVerifier].
     */
    private suspend fun logCompilationStatus() {
        /*
        When delivering through Google Play, the baseline profile is compiled during installation.
        In this case you will see the correct state logged without any further action necessary.
        To verify baseline profile installation locally, you need to manually trigger baseline
        profile installation.
        For immediate compilation, call:
         `adb shell cmd package compile -f -m speed-profile com.example.macrobenchmark.target`
        You can also trigger background optimizations:
         `adb shell pm bg-dexopt-job`
        Both jobs run asynchronously and might take some time complete.
        To see quick turnaround of the ProfileVerifier, we recommend using `speed-profile`.
        If you don't do either of these steps, you might only see the profile status reported as
        "enqueued for compilation" when running the sample locally.
        */
        withContext(Dispatchers.IO) {
            val status = ProfileVerifier.getCompilationStatusAsync().get()
            android.util.Log.d(TAG, "ProfileInstaller status code: ${status.profileInstallResultCode}")
            Log.d(
                TAG,
                when {
                    status.isCompiledWithProfile -> "ProfileInstaller: is compiled with profile"
                    status.hasProfileEnqueuedForCompilation() ->
                        "ProfileInstaller: Enqueued for compilation"

                    else -> "Profile not compiled or enqueued"
                },
            )
        }
    }
}

/**
 * Returns `true` if the Android theme should be used, as a funciton of the [uiState].
 */
@Composable
private fun shouldUseAndroidTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    is MainActivityUiState.Success -> when (uiState.data.themeBrand) {
        ThemeBrand.DEFAULT -> false
        ThemeBrand.ANDROID -> true
    }

    else -> false
}

/**
 * Returns `true` if the dynamic color is disabled, as a function of the [uiState].
 */
@Composable
private fun shouldDisableDynamicTheming(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    is MainActivityUiState.Success -> !uiState.data.useDynamicColor
    else -> true
}

/**
 * Returns `true` if dark theme should be used, as a
 * function of current system context.
 */
@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Success -> when (uiState.data.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }

    else -> isSystemInDarkTheme()
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)