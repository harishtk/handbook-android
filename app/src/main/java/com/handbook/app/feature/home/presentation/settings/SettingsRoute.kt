@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.settings

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.handbook.app.BuildConfig
import com.handbook.app.Constant
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.common.util.UiText
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.HandbookTopAppBarState
import com.handbook.app.core.designsystem.component.BestDealsTopAppBar
import com.handbook.app.core.designsystem.component.HandbookTopAppBar
import com.handbook.app.core.designsystem.component.LoadingDialog
import com.handbook.app.feature.home.presentation.util.SettingsItem
import com.handbook.app.feature.home.presentation.util.SettingsListType
import com.handbook.app.showToast
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.MaterialColor
import com.handbook.app.ui.theme.HandbookDarkGreen
import com.handbook.app.ui.theme.HandbookGreen
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

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
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

    val title = stringResource(R.string.label_settings)
    val topAppBarState = remember {
        HandbookTopAppBarState(
            title = title,
            showNavigationIcon = true,
            onNavigationIconClick = onNavUp
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BestDealsTopAppBar(state = topAppBarState)
        },
        bottomBar = {
            val brush = Brush.linearGradient(colors = listOf(HandbookGreen, HandbookDarkGreen))

            val appName = stringResource(R.string.app_name)
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
                        text = stringResource(id = R.string.made_with_from),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVersionNameClick() },
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        style = MaterialTheme.typography.labelSmall,
                        text = stringResource(R.string.version_info, appName, version),
                        color = MaterialColor.Grey400
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

                    val okText = stringResource(id = R.string.label_ok)
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
            SettingsItemRow(settingsItem = item) { settingsItem ->
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
            Divider(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialColor.Grey100
            )
        }
    }
}

@Composable
private fun SettingsItemRow(
    modifier: Modifier = Modifier,
    settingsItem: SettingsItem,
    onClick: (settingsItem: SettingsItem) -> Unit = {},
) {
    val clickableModifier = remember(settingsItem) {
        Modifier.clickable { onClick(settingsItem) }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
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
                            color = MaterialColor.Grey400
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

// @Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
@Preview(group = "screen")
@Composable
fun SettingsPreview() {
    Box {
        HandbookTheme {
            SettingsScreen(
                uiState = SettingsUiState.SettingsList(
                    settingsItems = settingsListData
                        .map { it.settingsItem },
                    false,
                    UiText.DynamicString("Something went wrong.")
                )
            )
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
                Divider(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialColor.Grey100
                )
            }
        }
    }
}

@Composable
@Preview(group = "popup", showBackground = true)
fun LogoutConfirmDialogPreview() {
    Box(
        Modifier.background(Color.White)
    ) {
        HandbookTheme {
            LogoutConfirmDialog(onDismiss = { /*TODO*/ }) {}
        }
    }
}
