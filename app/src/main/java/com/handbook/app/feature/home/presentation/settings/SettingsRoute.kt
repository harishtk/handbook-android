@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.handbook.app.BuildConfig
import com.handbook.app.Constant
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.R.string
import com.handbook.app.common.util.UiText
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.HandbookTopAppBarState
import com.handbook.app.core.designsystem.component.HandbookSimpleTopAppBar
import com.handbook.app.core.designsystem.component.LoadingDialog
import com.handbook.app.core.designsystem.component.ThemePreviews
import com.handbook.app.core.domain.model.DarkThemeConfig
import com.handbook.app.core.domain.model.DarkThemeConfig.*
import com.handbook.app.core.domain.model.ThemeBrand
import com.handbook.app.core.domain.model.ThemeBrand.*
import com.handbook.app.feature.home.presentation.util.SettingsItem
import com.handbook.app.feature.home.presentation.util.SettingsListType
import com.handbook.app.showToast
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.MaterialColor
import com.handbook.app.ui.theme.HandbookDarkGreen
import com.handbook.app.ui.theme.HandbookGreen
import com.handbook.app.ui.theme.supportsDynamicTheming
import timber.log.Timber

@Immutable
data class SettingsItemHolder(
    val items: List<SettingsItem>,
)

@Composable
internal fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavUp: () -> Unit,
    onOpenWebPage: (url: String) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeSettingsUiState by viewModel.themeSettingsUiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
        themeSettingsUiState = themeSettingsUiState,
        uiAction = viewModel.accept,
        onNavUp = onNavUp,
        onVersionNameClick = { gotoMarket(context) },
        onLogout = { viewModel.logout() },
        onOpenWebPage = onOpenWebPage,
        onErrorShown = { viewModel.reset() },
    )

    ObserverAsEvents(flow = viewModel.toastText) { event: UiText? ->
        if (event != null) {
            context.showToast(event.asString(context))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState = SettingsUiState.Loading,
    themeSettingsUiState: ThemeSettingsUiState = ThemeSettingsUiState.Loading,
    uiAction: (SettingsUiAction) -> Unit,
    onNavUp: () -> Unit = {},
    onVersionNameClick: () -> Unit = {},
    onOpenWebPage: (url: String) -> Unit = {},
    onLogout: () -> Unit = {},
    onErrorShown: () -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
    var showThemeSettingsDialog by remember { mutableStateOf(false) }

    val title = stringResource(string.label_settings)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = modifier,
                title = {
                    Row {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Theme Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        bottomBar = {
            val brush = Brush.linearGradient(colors = listOf(HandbookGreen, HandbookDarkGreen))

            val appName = stringResource(string.app_name)
            val version = "v${BuildConfig.VERSION_NAME}"

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = stringResource(id = string.made_with_from),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVersionNameClick() },
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall.copy(
                            brush = brush
                        ),
                        text = stringResource(string.version_info, appName, version),
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                )
        ) {
            Timber.d("UiState: $uiState")
            when (uiState) {
                SettingsUiState.Loading -> SettingLoadingView()
                is SettingsUiState.SettingsList -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        SettingsListView(
                            settingsItemsHolder = SettingsItemHolder(uiState.settingsItems),
                            onOpenWebPage = onOpenWebPage,
                            onLogout = {
                                showLogoutConfirmationDialog = true
                            },
                        )
                    }

                    LoadingDialog(isShowingDialog = uiState.loading,)

                    val okText = stringResource(id = string.label_ok)
                    LaunchedEffect(key1 = uiState.uiErrorText, key2 = uiState.loading) {
                        if (!uiState.loading) {
                            uiState.uiErrorText?.asString(context)?.let { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = error,
                                        actionLabel = okText,
                                        duration = SnackbarDuration.Indefinite,
                                    )
                                    onErrorShown()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLogoutConfirmationDialog) {
            LogoutConfirmDialog(
                onDismiss = { showLogoutConfirmationDialog = false },
                onSuccess = {
                    showLogoutConfirmationDialog = false
                    onLogout()
                }
            )
        }

        if (showThemeSettingsDialog) {
            ThemeSettingsDialog(
                themeSettingsUiState = themeSettingsUiState,
                onDismiss = { showThemeSettingsDialog = false },
                onChangeThemeBrand = {
                    uiAction(SettingsUiAction.OnThemeBrandChange(it))
                    showThemeSettingsDialog = false
                },
                onChangeDynamicColorPreference = {
                    uiAction(SettingsUiAction.OnDynamicColorPreferenceChange(it))
                    showThemeSettingsDialog = false
                },
                onChangeDarkThemeConfig = {
                    uiAction(SettingsUiAction.OnDarkThemeConfigChange(it))
                    showThemeSettingsDialog = false
                }
            )
        }
    }
}

@Composable
private fun ColumnScope.SettingLoadingView(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun ColumnScope.SettingsListView(
    settingsItemsHolder: SettingsItemHolder = SettingsItemHolder(emptyList()),
    onOpenWebPage: (url: String) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Looking for Answers?", style = MaterialTheme.typography.bodyMedium)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(insetSmall)
    ) {
        items(settingsItemsHolder.items, key = { it.id }) { item ->
            SettingsItemRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                settingsItem = item
            ) { settingsItem ->
                when (SettingsIds.fromId(settingsItem.id)) {
                    SettingsIds.Faq -> {
                        onOpenWebPage(Constant.FAQ_URL)
                    }

                    SettingsIds.Feedback -> {
                        // gotoFeedback()
                    }

                    SettingsIds.HelpAndSupport -> {
                        onOpenWebPage(Constant.SUPPORT_URL)
                    }

                    SettingsIds.Terms -> {
                        onOpenWebPage(Constant.TERMS_URL)
                    }

                    SettingsIds.Logout -> {
                        onLogout()
                    }

                    else -> {
                        onOpenWebPage(Constant.LANDING_URL)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    modifier: Modifier = Modifier,
    settingsItem: SettingsItem,
    onClick: (settingsItem: SettingsItem) -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(settingsItem) },
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (settingsItem.icon != null) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = settingsItem.icon),
                        contentDescription = settingsItem.title.asString(
                            LocalContext.current
                        ),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row {
                    Text(
                        text = settingsItem.title.asString(LocalContext.current),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                settingsItem.description?.let { uiText ->
                    Row {
                        Text(
                            text = uiText.asString(LocalContext.current),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            if (settingsItem.hasMore) {
                Spacer(modifier = modifier.weight(1f))
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(
                        imageVector = HandbookIcons.ChevronRight,
                        contentDescription = "View more",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ThemeSettingsDialog(
    themeSettingsUiState: ThemeSettingsUiState,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
    onDismiss: () -> Unit,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    val configuration = LocalConfiguration.current

    /**
     * usePlatformDefaultWidth = false is use as a temporary fix to allow
     * height recalculation during recomposition. This, however, causes
     * Dialog's to occupy full width in Compact mode. Therefore max width
     * is configured below. This should be removed when there's fix to
     * https://issuetracker.google.com/issues/221643630
     */
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.widthIn(max = configuration.screenWidthDp.dp - 80.dp),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(string.feature_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (themeSettingsUiState) {
                    ThemeSettingsUiState.Loading -> {
                        Text(
                            text = stringResource(string.feature_settings_loading),
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }

                    is ThemeSettingsUiState.Success -> {
                        ThemeSettingsPanel(
                            settings = themeSettingsUiState.settings,
                            supportDynamicColor = supportDynamicColor,
                            onChangeThemeBrand = onChangeThemeBrand,
                            onChangeDynamicColorPreference = onChangeDynamicColorPreference,
                            onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(string.feature_settings_dismiss_dialog_button_text),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Composable
private fun ColumnScope.ThemeSettingsPanel(
    settings: ThemeSettings,
    supportDynamicColor: Boolean,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    SettingsDialogSectionTitle(text = stringResource(string.feature_settings_theme))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_brand_default),
            selected = settings.brand == DEFAULT,
            onClick = { onChangeThemeBrand(DEFAULT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_brand_android),
            selected = settings.brand == ANDROID,
            onClick = { onChangeThemeBrand(ANDROID) },
        )
    }
    AnimatedVisibility(visible = settings.brand == DEFAULT && supportDynamicColor) {
        Column {
            SettingsDialogSectionTitle(text = stringResource(string.feature_settings_dynamic_color_preference))
            Column(Modifier.selectableGroup()) {
                SettingsDialogThemeChooserRow(
                    text = stringResource(string.feature_settings_dynamic_color_yes),
                    selected = settings.useDynamicColor,
                    onClick = { onChangeDynamicColorPreference(true) },
                )
                SettingsDialogThemeChooserRow(
                    text = stringResource(string.feature_settings_dynamic_color_no),
                    selected = !settings.useDynamicColor,
                    onClick = { onChangeDynamicColorPreference(false) },
                )
            }
        }
    }
    SettingsDialogSectionTitle(text = stringResource(string.feature_settings_dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_system_default),
            selected = settings.darkThemeConfig == FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_light),
            selected = settings.darkThemeConfig == LIGHT,
            onClick = { onChangeDarkThemeConfig(LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(string.feature_settings_dark_mode_config_dark),
            selected = settings.darkThemeConfig == DARK,
            onClick = { onChangeDarkThemeConfig(DARK) },
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hey wait",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Are you sure to log out?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { onDismiss() },
                            interactionSource = remember { MutableInteractionSource() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Text(
                                text = "No",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialColor.Grey400
                            )
                        }

                        TextButton(
                            onClick = { onSuccess() },
                            interactionSource = remember { MutableInteractionSource() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialColor.Red700)
                        ) {
                            Text(
                                text = "Yes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialColor.Red700
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun gotoMarket(context: Context) {
    Intent(Intent.ACTION_VIEW, Constant.MARKET_URI.toUri()).also { intent ->
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            throw IllegalStateException("Cannot perform this action!")
        }
    }
}

@Composable
@Preview(device = "id:pixel_3a", showBackground = true)
private fun SettingsItemPreview() {
    val aboutSettingsItem = SettingsItem(
        settingsListType = SettingsListType.SIMPLE,
        id = SettingsIds.About.id,
        title = UiText.DynamicString("About"),
        icon = R.drawable.ic_info_outline,
        description = UiText.DynamicString("Know more about us"),
        true
    )

    val logoutSettingsItem = SettingsItem(
        settingsListType = SettingsListType.SIMPLE,
        id = SettingsIds.Logout.id,
        title = UiText.DynamicString("Logout"),
        icon = R.drawable.ic_logout_outline,
        description = null,
        hasMore = false
    )

    val sampleSettingsItems = listOf(
        aboutSettingsItem,
        logoutSettingsItem
    )

    HandbookTheme(darkTheme = false) {
        Column {
            sampleSettingsItems.onEach { item ->
                SettingsItemRow(settingsItem = item)
            }
        }
    }
}

// @Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(group = "screen")
@ThemePreviews
@Composable
private fun SettingsPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        SettingsScreen(
            uiState = SettingsUiState.SettingsList(
                settingsItems = settingsListData
                    .map { it.settingsItem },
                false,
                UiText.DynamicString("Something went wrong.")
            ),
            themeSettingsUiState = ThemeSettingsUiState.Success(
                settings = ThemeSettings()
            ),
            uiAction = {}
        )
    }
}
