@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.create

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.SharedViewModel
import com.handbook.app.common.util.loadstate.LoadState
import com.handbook.app.core.designsystem.component.ConfirmBackPressDialog
import com.handbook.app.core.designsystem.component.LoadingButton
import com.handbook.app.core.designsystem.component.LoadingButtonState
import com.handbook.app.core.designsystem.component.LoadingState
import com.handbook.app.core.designsystem.component.TextFieldError
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.net.NoInternetException
import com.handbook.app.ui.cornerSizeMedium
import com.handbook.app.ui.insetMedium
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.insetVerySmall
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val PostContentLength = 280

@Composable
internal fun WritePostRoute(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    viewModel: WritePostViewModel = hiltViewModel(),
    onNavUp: () -> Unit,
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.postUiState.collectAsStateWithLifecycle()

    var confirmBackPress by remember { mutableStateOf(false) }
    BackHandler {
        confirmBackPress = true
    }

    WritePostScreen(
        modifier = modifier,
        uiState = uiState,
        uiAction = viewModel.accept,
        snackBarHostState = snackBarHostState,
        onNavUp = {
            confirmBackPress = true
        }
    )

    ObserverAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is WritePostUiEvent.ShowSnack -> {}
            is WritePostUiEvent.ShowToast -> {}
        }
    }

    if (confirmBackPress) {
        ConfirmBackPressDialog(
            description = "Discard post and go back?"
        ) {
            confirmBackPress = false
            onNavUp()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is PostUiState.Posted) {
            Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show()
            sharedViewModel.setRefreshFeeds(true)
            onNavUp()
            viewModel.accept(WritePostUiAction.Reset)
        }
    }
}

@Composable
internal fun WritePostScreen(
    modifier: Modifier = Modifier,
    uiState: PostUiState,
    uiAction: (WritePostUiAction) -> Unit,
    snackBarHostState: SnackbarHostState = SnackbarHostState(),
    onNavUp: (dirty: Boolean) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { snackBarHostState },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavUp }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, fill = true)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {

                when (uiState) {
                    PostUiState.Idle -> {}
                    is PostUiState.WritingPost -> {
                        WritePostFormLayout(
                            uiState = uiState,
                            uiAction = uiAction,
                            snackBarHostState = snackBarHostState,
                        )
                    }
                    PostUiState.Posted -> {}
                }
            }
        }
    }
}

@Composable
private fun WritePostFormLayout(
    modifier: Modifier = Modifier,
    uiState: PostUiState.WritingPost,
    uiAction: (WritePostUiAction) -> Unit,
    snackBarHostState: SnackbarHostState = SnackbarHostState(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        val contentFocusRequester = remember { FocusRequester() }
        val contentState by rememberSaveable(stateSaver = PostContentStateSaver) {
            mutableStateOf(PostContentState(uiState.content))
        }

        ContentInput(
            contentState = contentState,
            onValueChange = { uiAction(WritePostUiAction.OnContentChange(it)) },
            provideFocusRequester = { contentFocusRequester },
            enableCharacterCounter = true
        )

        uiState.errorMessage?.let { error ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(8.dp))

                val message = error.message?.asString(LocalContext.current)
                    ?: "Something is not working"
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        val enableNext = true
        Footer(
            enabled = enableNext,
            loadState = uiState.loadState,
            isSuccessFul = false,
            postContentState = contentState,
            snackbarHostState = snackBarHostState,
            onClick = {
                var isValid = true
                var shouldRequestFocus = true
                if (!contentState.isValid) {
                    contentState.enableShowErrors()
                    isValid = false
                    contentFocusRequester.requestFocus()
                    shouldRequestFocus = false
                }

                if (isValid) {
                    uiAction(WritePostUiAction.Submit)
                }
            },
            modifier = Modifier.height(IntrinsicSize.Min)
        )

    }
}

@Composable
private fun ContentInput(
    modifier: Modifier = Modifier,
    contentState: TextFieldState,
    onValueChange: (text: String) -> Unit = {},
    enableCharacterCounter: Boolean = false,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val mergedTextStyle = MaterialTheme.typography
        .bodyMedium

    Column(
        modifier = modifier
            .padding(horizontal = insetMedium, vertical = insetSmall),
    ) {
        Text(
            text = buildAnnotatedString {
                append("Write a post")
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = contentState.text,
            onValueChange = { text ->
                contentState.text = text.take(PostContentLength)
                onValueChange(contentState.text)
            },
            placeholder = {
                Text(
                    text = "Start writing..",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            supportingText = {
                if (enableCharacterCounter) {
                    val count = contentState.text.length
                    Text(
                        text = "$count/$PostContentLength",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            },
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W600),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            isError = contentState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    contentState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        contentState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        contentState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    loadState: LoadState,
    isSuccessFul: Boolean,
    postContentState: TextFieldState,
    snackbarHostState: SnackbarHostState,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val loadingButtonState by remember {
        mutableStateOf(LoadingButtonState())
    }
    loadingButtonState.enabled = postContentState.text.isNotBlank() &&
            loadState !is LoadState.Loading

    loadingButtonState.loadingState = when (loadState) {
        is LoadState.Loading -> LoadingState.Loading
        is LoadState.Error -> {
            Timber.e(loadState.error)
            if (loadState.error is NoInternetException) {
                val noInternetErrorText = stringResource(id = R.string.check_your_internet)
                LaunchedEffect(key1 = Unit) {
                    scope.launch {
                        snackbarHostState.showSnackbar(noInternetErrorText)
                    }
                }
            } else {
                val somethingWentWrongText = stringResource(id = R.string.something_went_wrong_try_later)
                LaunchedEffect(key1 = Unit) {
                    scope.launch {
                        snackbarHostState.showSnackbar(somethingWentWrongText)
                    }
                }
            }
            LoadingState.Failed
        }
        else -> {
            if (isSuccessFul) {
                LoadingState.Success
            } else {
                LoadingState.Idle
            }
        }
    }

    var loadingButtonWidth by remember {
        mutableIntStateOf(0)
    }
    LoadingButton(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged {
                loadingButtonWidth = it.width
            },
        loadingButtonState = loadingButtonState,
        text = "Post",
        onClick = onClick,
        resetDelay = 50 /* for haptics */,
        onResetRequest = { currentState ->
            // uiAction(LoginUiAction.ResetLoading)
            scope.launch {
                delay(500)
                //uiAction(LoginUiAction.ResetLoading)
                loadingButtonState.loadingState = LoadingState.Idle
            }
        }
    )
}


@Composable
@Preview(showBackground = true)
private fun CreateDefaultPreview() {
    HandbookTheme(
        darkTheme = false,
        disableDynamicTheming = true
    ) {
        val scope = rememberCoroutineScope()
        var uiState by remember { mutableStateOf(
            PostUiState.WritingPost(
                content = "",
                loadState = LoadState.NotLoading.Complete
            )
        ) }

        WritePostScreen(
            uiState = uiState,
            uiAction = {
                when (it) {
                    is WritePostUiAction.OnContentChange -> {
                        uiState = uiState.copy(content = it.content)
                    }
                    WritePostUiAction.Submit -> {
                        uiState = uiState.copy(loadState = LoadState.Loading())
                        scope.launch {
                            delay(1000)
                            uiState = uiState.copy(loadState = LoadState.NotLoading.Complete)
                        }
                    }
                    WritePostUiAction.Reset -> {}
                }
            }
        )
    }
}