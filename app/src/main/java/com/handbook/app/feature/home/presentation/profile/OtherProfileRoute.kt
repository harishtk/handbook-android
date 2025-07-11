package com.handbook.app.feature.home.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.core.designsystem.shimmerBackground
import com.handbook.app.core.util.ErrorMessage
import com.handbook.app.feature.home.domain.model.UserProfile
import com.handbook.app.showToast
import com.handbook.app.ui.theme.HandbookTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun OtherProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onOptionSettingsRequest: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.profileUiState.collectAsStateWithLifecycle()
    val uiAction = viewModel.accept

    OtherProfileScreen(
        modifier = modifier,
        uiState = uiState,
        uiAction = uiAction,
        onSettingsClick = onOptionSettingsRequest
    )

    ObserverAsEvents(flow = viewModel.uiEvent) { event ->
        when (event) {
            is ProfileUiEvent.ShowToast -> {
                context.showToast(event.message.asString(context))
            }

            is ProfileUiEvent.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }
}


@Composable
private fun OtherProfileScreen(
    modifier: Modifier,
    uiState: ProfileUiState = ProfileUiState.Idle,
    uiAction: (ProfileUiAction) -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopStart)
                ) {
                    IconButton(
                        onClick = { expanded = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More"
                        )
                    }
                    DropdownMenu(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.settings)) },
                            onClick = {
                                expanded = false
                                onSettingsClick()
                            }
                        )
                    }
                }
            }
        }
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
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
            ) {

                when (uiState) {
                    is ProfileUiState.Error -> FullScreenErrorLayout(
                        errorMessage = uiState.errorMessage,
                        onClick = { uiAction(ProfileUiAction.Refresh) }
                    )
                    ProfileUiState.Idle -> Box {}
                    ProfileUiState.Loading -> LoadingScreen()
                    is ProfileUiState.Success -> OtherProfileContent(
                        profile = uiState.profile,
                        uiAction = uiAction
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherProfileContent(
    modifier: Modifier = Modifier,
    profile: UserProfile,
    uiAction: (ProfileUiAction) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        // OtherProfile Image, Display Name, Username
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!profile.profilePictureId.isBlank()) {
                    // In a real app, you'd use a library like Coil or Glide here
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder
                        contentDescription = "OtherProfile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val initials = profile.displayName.split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .joinToString("")
                    Text(
                        text = initials.ifEmpty { "?" },
                        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bio
        Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Followers and Following
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* TODO: Navigate to followers screen */ }) {
                Text(text = "${profile.followersCount} Followers")
            }
            Button(onClick = { /* TODO: Navigate to following screen */ }) {
                Text(text = "${profile.followingCount} Following")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Placeholder for Posts
        Text(text = "Posts (Coming Soon...)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        // OtherProfile Image, Display Name, Username (Shimmer)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shimmerBackground(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(28.dp)
                    .shimmerBackground()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp)
                    .shimmerBackground()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bio (Shimmer)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Approximate height for bio
                .shimmerBackground()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons (Shimmer) - Could be more elaborate if needed
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Box(modifier = Modifier
                .width(120.dp)
                .height(40.dp)
                .shimmerBackground())
            Box(modifier = Modifier
                .width(120.dp)
                .height(40.dp)
                .shimmerBackground())
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun OtherProfileScreenPreview() {
    HandbookTheme {
        Column {
            OtherProfileScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = ProfileUiState.Success(
                    profile = UserProfile(
                        id = "dummy-id",
                        username = "johndoe",
                        displayName = "John Doe",
                        bio = "This is a sample bio.",
                        profilePictureId = "",
                        createdAt = Clock.System.now().toString(),
                        followersCount = 100,
                        followingCount = 50,
                        postsCount = 20,
                        isFollowing = true,
                        isSelf = false,
                    )
                ),
                uiAction = {},
                onSettingsClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun OtherProfileLoadingPreview() {
    HandbookTheme {
        LoadingScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun FullScreenErrorPreview() {
    HandbookTheme {
        Column {
            FullScreenErrorLayout(
                errorMessage = ErrorMessage.unknown(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}