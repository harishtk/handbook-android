@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.notification

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.handbook.app.ObserverAsEvents
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.core.Noop
import com.handbook.app.core.designsystem.component.UserAvatar
import com.handbook.app.feature.home.domain.model.HandbookNotification
import com.handbook.app.feature.home.domain.model.HandbookNotificationType
import com.handbook.app.feature.home.presentation.profile.FullScreenErrorLayout
import com.handbook.app.ui.theme.Amber40
import com.handbook.app.ui.theme.HandbookTheme
import java.time.Instant

@Composable
internal fun NotificationRoute(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
    onNavigateToPost: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavUp: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState = viewModel.notificationUiState.collectAsStateWithLifecycle()

    NotificationScreen(
        modifier = modifier,
        uiState = uiState.value,
        uiActions = viewModel.accept,
        onNavUp = onNavUp,
    )

    ObserverAsEvents(viewModel.uiEvent) {
        when (it) {
            is NotificationUiEvent.ShowSnack -> {
                scope.launch {
                    snackbarHostState.showSnackbar(it.message.asString(context))
                }
            }
            is NotificationUiEvent.ShowToast -> {
                Toast.makeText(context, it.message.asString(context), Toast.LENGTH_SHORT).show()
            }
            is NotificationUiEvent.NavigateToPostDetail -> {
                onNavigateToPost(it.postId)
            }
            is NotificationUiEvent.NavigateToProfile -> {
                onNavigateToProfile(
                    if (it.isSelf) "" else it.userId
                )
            }
        }
    }
}

@Composable
private fun NotificationScreen(
    modifier: Modifier,
    uiState: NotificationUiState,
    uiActions: (NotificationUiAction) -> Unit,
    onNavUp: () -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Notifications", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onNavUp) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {}
                    ) {
                        Text(text = "Mark all read", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                )
                .systemBarsPadding()
        ) {

            AnimatedContent(uiState) { state ->
                when (state) {
                    NotificationUiState.Loading -> {
                        LoadingScreen()
                    }
                    NotificationUiState.Idle -> {}
                    is NotificationUiState.Error -> {
                        FullScreenErrorLayout(
                            errorMessage = state.errorMessage,
                            onClick = { uiActions(NotificationUiAction.Refresh) }
                        )
                    }
                    is NotificationUiState.Success -> {
                        NotificationContent(
                            uiState = state,
                            uiActions = uiActions,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationContent(
    uiState: NotificationUiState.Success,
    uiActions: (NotificationUiAction) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        uiState.notifications.forEach { uiModel ->
            when (uiModel) {
                is NotificationUiModel.Separator -> {
                    item {
                        Text(
                            text = uiModel.title,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                is NotificationUiModel.Footer -> {
                    item { Footer(append = uiModel.loadState) }
                }

                is NotificationUiModel.Notification -> {
                    item {
                        NotificationItem(
                            notification = uiModel.banterboxNotification,
                            onUserProfileClick = {
                                uiModel.banterboxNotification.actorId?.let { uiActions(NotificationUiAction.NavigateToProfile(it)) }
                            },
                            onPostClick = {
                                uiModel.banterboxNotification.referenceId?.let { uiActions(NotificationUiAction.NavigateToPostDetail(it)) }
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun NotificationItem(
    modifier: Modifier = Modifier,
    notification: HandbookNotification,
    onUserProfileClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                when (notification.type) {
                    HandbookNotificationType.FOLLOW -> {
                        onUserProfileClick()
                    }
                    HandbookNotificationType.LIKE -> {
                        onPostClick()
                    }
                    HandbookNotificationType.GENERAL -> Noop()
                    HandbookNotificationType.ANNOUNCEMENTS -> Noop()
                    HandbookNotificationType.UNKNOWN -> Noop()
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!notification.read) {
            VerticalDivider(
                thickness = 2.dp,
                color = Amber40,
                modifier = Modifier.background(Amber40)
            )
        }
        Box {

            if (!notification.read) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Unread",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.actor.username,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Optional: Add an action button or icon here if needed
        // For example, a "Follow back" button or a context menu icon
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    append: LoadState,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (append is LoadState.Loading) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun NotificationScreenPreview() {
    HandbookTheme {
        val uiState = NotificationUiState.Success(
            listOf(
                NotificationUiModel.Notification(
                    HandbookNotification(
                        id = "1",
                        type = HandbookNotificationType.FOLLOW,
                        subType = null,
                        message = "Someone followed you",
                        createdAt = Instant.now().toString(),
                        read = false,
                        referenceId = null,
                        recipientId = "1",
                        actorId = "1",
                        actor = com.handbook.app.feature.home.domain.model.UserSummary(
                            id = "1",
                            username = "john",
                            displayName = "John",
                            profilePictureId = "",
                            isFollowing = false
                        )
                    )
                )
            )
        )

        NotificationScreen(
            modifier = Modifier,
            uiState = uiState,
            uiActions = {},
            onNavUp = {},
        )
    }
}
