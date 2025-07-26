@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.handbook.app.feature.home.presentation.accounts.addaccount

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.handbook.app.BuildConfig
import com.handbook.app.Constant
import com.handbook.app.Constant.MIME_TYPE_ANY
import com.handbook.app.Constant.MIME_TYPE_JPEG
import com.handbook.app.Constant.MIME_TYPE_VIDEO
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.common.util.StorageUtil
import com.handbook.app.core.designsystem.HandbookIcons
import com.handbook.app.core.designsystem.component.ConfirmBackPressDialog
import com.handbook.app.core.designsystem.component.CustomConfirmDialog
import com.handbook.app.core.designsystem.component.DialogActionType
import com.handbook.app.core.designsystem.component.TextFieldError
import com.handbook.app.core.designsystem.component.expandable
import com.handbook.app.core.designsystem.component.forms.MediaType
import com.handbook.app.core.designsystem.component.text.TextFieldStateHandler
import com.handbook.app.core.designsystem.dashedBorder
import com.handbook.app.core.designsystem.exposeBounds
import com.handbook.app.feature.home.domain.model.Attachment
import com.handbook.app.feature.home.domain.model.Bank
import com.handbook.app.feature.home.domain.model.Category
import com.handbook.app.feature.home.domain.model.EntryType
import com.handbook.app.feature.home.domain.model.Party
import com.handbook.app.feature.home.domain.model.TransactionType
import com.handbook.app.feature.home.presentation.accounts.components.SimpleButtonGroup
import com.handbook.app.feature.home.presentation.accounts.components.SimpleDropDownPicker
import com.handbook.app.feature.home.presentation.accounts.components.form.AmountState
import com.handbook.app.feature.home.presentation.party.components.form.DescriptionLength
import com.handbook.app.feature.home.presentation.party.components.form.DescriptionState
import com.handbook.app.feature.home.presentation.party.components.form.DisplayNameLength
import com.handbook.app.feature.home.presentation.party.components.form.DisplayNameState
import com.handbook.app.ifDebug
import com.handbook.app.showToast
import com.handbook.app.ui.cornerSizeMedium
import com.handbook.app.ui.cornerSizeSmall
import com.handbook.app.ui.defaultSpacerSize
import com.handbook.app.ui.insetMedium
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.insetVerySmall
import com.handbook.app.ui.mediumIconSize
import com.handbook.app.ui.smallButtonHeightMax
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.LightGray100
import com.handbook.app.ui.theme.LightGray200
import com.handbook.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

internal const val MAX_IMAGES_LIMIT = 5
internal const val MAX_VIDEOS_LIMIT = 1
internal const val MAX_ATTACHMENTS_LIMIT = 3

private val storagePermissions: Array<String> = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AddAccountRoute(
    navController: NavHostController,
    viewModel: AddAccountViewModel = hiltViewModel(),
    onNextPage: () -> Unit = {},
    onSelectCategoryRequest: (selectedCategoryId: Long, transactionType: TransactionType) -> Unit,
    onSelectPartyRequest: (selectedPartyId: Long) -> Unit = {},
    onSelectBankRequest: (bankId: Long) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val onNextPageLatest by rememberUpdatedState(onNextPage)

    var confirmBackPress by remember { mutableStateOf(false) }
    BackHandler {
        confirmBackPress = true
    }

    var pickerLauncherMediaType = remember { MediaType.Unknown }

    var showDuplicatePhotosAlert by remember {
        mutableStateOf(false to 0)
    }
    var showAttachmentOptionDialog by remember { mutableStateOf(false to MediaType.Unknown) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.getTempCaptureFile()?.let { file ->
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
                scope.launch(Dispatchers.IO) {
                    preProcessUris(context, viewModel, pickerLauncherMediaType, listOf(uri)) { duplicateCount ->
                        if (duplicateCount > 0) {
                            showDuplicatePhotosAlert = true to duplicateCount
                        }
                    }
                    pickerLauncherMediaType = MediaType.Unknown
                }
            } ?: context.showToast("Something went wrong.")
        }
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGES_LIMIT)
    ) { pickedUris ->
        Timber.d("Picked Uris: $pickedUris pendingType=$pickerLauncherMediaType")
        if (pickedUris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                preProcessUris(context, viewModel, pickerLauncherMediaType, pickedUris) { duplicateCount ->
                    if (duplicateCount > 0) {
                        showDuplicatePhotosAlert = true to duplicateCount
                    }
                }
                pickerLauncherMediaType = MediaType.Unknown
            }
        }
    }

    val mediaPickerGenericLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { pickedUris ->
        Timber.d("Picked Uris Generic: $pickedUris pendingType=$pickerLauncherMediaType")
        if (pickedUris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                preProcessUris(context, viewModel, pickerLauncherMediaType, pickedUris) { duplicateCount ->
                    if (duplicateCount > 0) {
                        showDuplicatePhotosAlert = true to duplicateCount
                    }
                }
                pickerLauncherMediaType = MediaType.Unknown
            }
        }
    }

    /* <bool, bool> - (show rationale, openSettings) */
    var showStoragePermissionRationale by remember {
        mutableStateOf(false to false)
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        val deniedList: List<String> = result.filter { !it.value }.map { it.key }
        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permission ->
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                        Constant.PERMISSION_DENIED
                    } else {
                        Constant.PERMISSION_PERMANENTLY_DENIED
                    }
                }
                map[Constant.PERMISSION_DENIED]?.let {
                    // context.showToast("Storage permission is required to upload photos")
                    showStoragePermissionRationale = true to false
                }
                map[Constant.PERMISSION_PERMANENTLY_DENIED]?.let {
                    // context.showToast("Storage permission is required to upload photos")
                    showStoragePermissionRationale = true to true
                }
            }

            else -> {
                // TODO: Handle continuation?
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.getTempCaptureFile()?.let { file ->
                val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
                cameraLauncher.launch(uri)
                pickerLauncherMediaType = showAttachmentOptionDialog.second
            }
        } else {
            showStoragePermissionRationale = true to !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.CAMERA)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editEntryId by viewModel.accountEntryId.collectAsStateWithLifecycle()

    AddAccountScreen(
        uiState = uiState,
        uiAction = viewModel.accept,
        isInEditMode = editEntryId != 0L,
        onNavUp = onNextPageLatest,
        launchMediaPicker = { _, type ->
            Timber.d("Launch media picker: type=$type")
            showAttachmentOptionDialog = true to type
        },
        onDeleteMedia = { _, type, uri ->
            viewModel.deleteMedia(type, uri)
        },
    )

    if (confirmBackPress) {
        ConfirmBackPressDialog(
            description = "Discard changes and go back?"
        ) { result ->
            confirmBackPress = false
            if (result == 1) {
                onNextPageLatest()
            }
        }
    }

    LaunchedEffect(key1 = uiState) {
        if (uiState == AddAccountUiState.AddAccountSuccess) {
            if (editEntryId != 0L) {
                context.showToast("Entry updated")
            } else {
                context.showToast("Entry added")
            }
            // viewModel.accept(AddAccountUiAction.Reset)
            onNextPageLatest()
        }
    }

    ObserverAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is AddAccountUiEvent.ShowToast -> {
                context.showToast(event.message.asString(context))
            }
            AddAccountUiEvent.OnNavUp -> {
                onNextPageLatest()
            }

            is AddAccountUiEvent.NavigateToCategorySelection -> {
                // context.showToast("Navigating to category: ${event.categoryId}")
                onSelectCategoryRequest(event.categoryId, event.transactionType)
            }
            is AddAccountUiEvent.NavigateToPartySelection -> {
                // context.showToast("Navigating to party: ${event.partyId}")
                onSelectPartyRequest(event.partyId)
            }
            is AddAccountUiEvent.NavigateToBankSelection -> {
                onSelectBankRequest(event.bankId)
            }
        }
    }

    if (showStoragePermissionRationale.first) {
        StoragePermissionRationaleDialog(
            openSettings = showStoragePermissionRationale.second,
            onDismiss = { canceled ->
                if (showStoragePermissionRationale.second && !canceled) {
                    context.openSettings()
                } else if (!canceled) {
                    storagePermissionLauncher.launch(storagePermissions)
                }
                showStoragePermissionRationale = false to false
            }
        )
    }

    val currentNavController by rememberUpdatedState(navController)
    DisposableEffect(currentNavController) {
        val navBackStackEntry = currentNavController.currentBackStackEntry
        val savedStateHandle = navBackStackEntry?.savedStateHandle

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { // Or another appropriate lifecycle event
                // Extract slected Category Id
                savedStateHandle?.get<Long>("categoryId")?.let { result ->
                    viewModel.accept(AddAccountUiAction.OnCategoryToggle(result))
                    savedStateHandle.remove<Long>("categoryId")
                }

                // Extract selected Party Id
                savedStateHandle?.get<Long?>("partyId")?.let { result ->
                    viewModel.accept(AddAccountUiAction.OnPartyToggle(result))
                    savedStateHandle.remove<Long>("partyId")
                }

                // Extract selected Bank Id
                savedStateHandle?.get<Long?>("bankId")?.let { result ->
                    viewModel.accept(AddAccountUiAction.OnBankToggle(result))
                    savedStateHandle.remove<Long>("bankId")
                }
            }
        }
        navBackStackEntry?.lifecycle?.addObserver(observer)


        onDispose {
            navBackStackEntry?.lifecycle?.removeObserver(observer)
        }
    }

    if (showAttachmentOptionDialog.first) {
        AttachmentOptionDialog(
            onDismiss = { showAttachmentOptionDialog = false to MediaType.Unknown },
            onOptionSelected = { option ->
                when (option) {
                    "Gallery" -> {
                        val type = showAttachmentOptionDialog.second
                        when (type) {
                            MediaType.Image -> {
                                val maxPick = viewModel.getMaxAttachments()
                                Timber.d("Mx pick: $maxPick")

                                if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.SingleMimeType(MIME_TYPE_JPEG))
                                    )
                                    pickerLauncherMediaType = type
                                } else {
                                    Timber.w("No media picker available. Using generic picker.")
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                        if (!context.checkStoragePermission()) {
                                            /* mStoragePermissionContinuation = {
                                                 photoPickerGenericLauncher.launch(MIME_TYPE_IMAGE)
                                             }*/
                                            storagePermissionLauncher.launch(storagePermissions)
                                        } else {
                                            mediaPickerGenericLauncher.launch(MIME_TYPE_JPEG)
                                            context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                            pickerLauncherMediaType = type
                                        }
                                    } else {
                                        mediaPickerGenericLauncher.launch(MIME_TYPE_JPEG)
                                        context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                        pickerLauncherMediaType = type
                                    }
                                }
                            }
                            MediaType.Video -> {
                                val maxPick = viewModel.getMaxAttachments()
                                Timber.d("Mx pick: $maxPick")

                                Timber.w("No media picker available. Using generic picker.")
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                    if (!context.checkStoragePermission()) {
                                        /* mStoragePermissionContinuation = {
                                             photoPickerGenericLauncher.launch(MIME_TYPE_IMAGE)
                                         }*/
                                        storagePermissionLauncher.launch(storagePermissions)
                                    } else {
                                        mediaPickerGenericLauncher.launch(MIME_TYPE_VIDEO)
                                        context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                        pickerLauncherMediaType = type
                                    }
                                } else {
                                    mediaPickerGenericLauncher.launch(MIME_TYPE_VIDEO)
                                    context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                    pickerLauncherMediaType = type
                                }

                                /*if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                           ActivityResultContracts.PickVisualMedia.VideoOnly
                                        )
                                    )
                                    pickerLauncherMediaType = type
                                } else {

                                }*/
                            }
//                MediaType.Unknown -> {
//                    val t = IllegalStateException("Unable to proceed with media type '*'")
//                    if (BuildConfig.DEBUG) { throw t }
//                    else { Timber.w(t) }
//                }
                            else -> {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                    if (!context.checkStoragePermission()) {
                                        /* mStoragePermissionContinuation = {
                                             photoPickerGenericLauncher.launch(MIME_TYPE_IMAGE)
                                         }*/
                                        storagePermissionLauncher.launch(storagePermissions)
                                    } else {
                                        mediaPickerGenericLauncher.launch(MIME_TYPE_ANY)
                                        context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                        pickerLauncherMediaType = type
                                    }
                                } else {
                                    mediaPickerGenericLauncher.launch(MIME_TYPE_ANY)
                                    context?.showToast(context.getString(R.string.photo_picker_long_press_hint))
                                    pickerLauncherMediaType = type
                                }
                            }
                        }
                    }
                    "Camera" -> {
                        if (!context.checkCameraPermission()) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            viewModel.getTempCaptureFile()?.let { file ->
                                val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
                                cameraLauncher.launch(uri)
                            }
                        }
                    }
                }
                showAttachmentOptionDialog = false to MediaType.Unknown
            }
        )
    }
}

@Composable
private fun AddAccountScreen(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState,
    uiAction: (AddAccountUiAction) -> Unit,
    isInEditMode: Boolean = false,
    onNavUp: () -> Unit = {},
    launchMediaPicker: (productId: Long, type: MediaType) -> Unit = { _, _ -> },
    onDeleteMedia: (productId: Long, type: MediaType, uri: Uri) -> Unit = { _, _, _ -> },
) {
    val snacbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(snacbarHostState, Modifier.navigationBarsPadding()) },
        topBar = {
            TopAppBar(
                title = {
                    val titleMessage = if (isInEditMode) {
                        "Edit Entry"
                    } else {
                        "Add Entry"
                    }
                    Text(text = titleMessage, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavUp
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { confirmDelete = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Vertical
                    )
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {

                when (uiState) {
                    AddAccountUiState.AddAccountSuccess -> {
                        // noop
                    }

                    is AddAccountUiState.AddAccountForm -> {
                        AddAccountFormLayout(
                            uiState = uiState,
                            uiAction = uiAction,
                            isInEditMode = isInEditMode,
                            launchMediaPicker = launchMediaPicker,
                            onDeleteMedia = onDeleteMedia
                        )
                    }
                }
            }
        }

        if (confirmDelete) {
            CustomConfirmDialog(
                title = "Delete entry",
                description = "Are you sure you want to delete this entry?",
                onDismiss = { confirmDelete = false },
                confirmActionType = DialogActionType.DESTRUCTIVE,
                onConfirm = {
                    confirmDelete = false
                    uiAction(AddAccountUiAction.DeleteAccountEntry)
                }
            )
        }
    }
}

@ExperimentalMaterial3ExpressiveApi
@Composable
private fun ColumnScope.AddAccountFormLayout(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState.AddAccountForm,
    uiAction: (AddAccountUiAction) -> Unit,
    isInEditMode: Boolean = false,
    launchMediaPicker: (productId: Long, type: MediaType) -> Unit = { _, _ -> },
    onDeleteMedia: (productId: Long, type: MediaType, uri: Uri) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
    val displayNameFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val categoryFocusRequester = remember { FocusRequester() }
    val dateFocusRequester = remember { FocusRequester() }

    val displayNameState = DisplayNameState.createTextFieldStateHandler(uiState.title)
    val amountState = AmountState.createTextFieldStateHandler(uiState.amount)
    val descriptionState = DescriptionState.createTextFieldStateHandler(uiState.description)

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(uiState.transactionDate) }

    // Initialize the DatePickerState.
    // You can set an initial selected date, year range, etc.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        selectableDates = object : SelectableDates {
            // Optionally, disable past dates
            // override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            //     return utcTimeMillis >= System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Allow today
            // }

            // Optionally, limit selection to specific years
            // override fun isSelectableYear(year: Int): Boolean {
            //    return year >= 2023 && year <= 2025
            // }
        }
    )

    // Derived state to enable the confirm button only when a date is selected
    val confirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }
    var enableDescription by remember { mutableStateOf(false) }
    var enableAttachments by remember { mutableStateOf(BuildConfig.DEBUG) }

    LaunchedEffect(uiState) {
        if (displayNameState.text != uiState.title) {
            displayNameState.updateText(uiState.title)
        }
        if (amountState.text != uiState.amount) {
            amountState.updateText(uiState.amount)
        }
        if (descriptionState.text != uiState.description) {
            descriptionState.updateText(uiState.description)
            enableDescription = descriptionState.text.isNotBlank()
        }
        if (uiState.transactionDate != selectedDateMillis) {
            selectedDateMillis = uiState.transactionDate
        }
        enableAttachments = uiState.mediaFiles
            .filterIsInstance<UploadPreviewUiModel.Item>()
            .isNotEmpty()
    }

    Box {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EntryTypeSelector(
                    uiState = uiState,
                    onToggle = uiAction,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                ToggleButton(
                    checked = uiState.isPinned,
                    onCheckedChange = { uiAction(AddAccountUiAction.OnPinnedChange(it)) },
                    contentPadding = ButtonDefaults.contentPaddingFor(32.dp),
                    shapes = ToggleButtonDefaults.shapes(
                        shape =  ToggleButtonDefaults.roundShape,
                        checkedShape = ToggleButtonDefaults.roundShape,
                        pressedShape = ToggleButtonDefaults.squareShape,
                    ),
                ) {
                    Icon(
                        imageVector = if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (uiState.isPinned) "Pinned" else "Unpinned",
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(45f)
                    )
                }
            }

            TransactionTypeSelector(
                uiState = uiState,
                onToggle = uiAction,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            // Add a date picker and simplepicker for parties
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        .expandable(
                            expanded = showDatePickerDialog,
                            onExpandedChange = { showDatePickerDialog = !showDatePickerDialog },
                            expandedDescription = "Show date picker",
                            collapsedDescription = "Hide date picker",
                            toggleDescription = "Toggle date picker"
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = selectedDateMillis.toFormattedDateString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                PartyInput(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    party = uiState.party,
                    onClick = {
                        uiAction(AddAccountUiAction.OnPartySelectRequest(uiState.party?.id ?: 0L))
                    }
                )
            }

            DisplayNameInput(
                displayNameState = displayNameState,
                onValueChange = {
                    uiAction(AddAccountUiAction.OnTypingTitle(it))
                },
                provideFocusRequester = { displayNameFocusRequester }
            )

            if (uiState.entryType == EntryType.BANK) {
                BankInput(
                    bank = uiState.bank,
                    onClick = {
                        uiAction(AddAccountUiAction.OnBankSelectRequest(uiState.bank?.id ?: 0L))
                    }
                )
            }

            AmountInput(
                amountState = amountState,
                onValueChange = {
                    uiAction(AddAccountUiAction.OnTypingAmount(it))
                },
                provideFocusRequester = { amountFocusRequester }
            )

            CategoryInput(
                category = uiState.category,
                onClick = {
                    Timber.d("onClick: CategoryInput")
                    uiAction(AddAccountUiAction.OnCategorySelectRequest(uiState.category?.id ?: 0L))
                },
                provideFocusRequester = { categoryFocusRequester }
            )

            TextButton(
                modifier = Modifier
                    .padding(horizontal = insetSmall)
                    .align(Alignment.Start),
                onClick = {
                    enableDescription = !enableDescription
                    if (!enableDescription) {
                        descriptionState.updateText("")
                    }
                }
            ) {
                if (enableDescription) {
                    Text("⛔ Remove description")
                } else {
                    Text("✍\uD83C\uDFFB Add description")
                }
            }

            AnimatedVisibility(enableDescription) {
                DescriptionInput(
                    bioState = descriptionState,
                    provideFocusRequester = { descriptionFocusRequester }
                )
            }

            TextButton(
                modifier = Modifier
                    .padding(horizontal = insetSmall)
                    .align(Alignment.Start),
                onClick = {
                    enableAttachments = !enableAttachments
                    if (!enableAttachments) {
                        // descriptionState.updateText("")
                        // TODO: clear attachments
                    }
                }
            ) {
                if (enableAttachments) {
                    Text("⛔ Remove attachment")
                } else {
                    Text("✍\uD83C\uDFFB Add attachment")
                }
            }

            AnimatedVisibility(enableAttachments) {
                AttachmentInput(
                    mediaFiles = uiState.mediaFiles,
                    onPlaceHolderClick = {
                        launchMediaPicker(0, MediaType.Image)

                                           },
                    onDeleteClick = { uri ->
                        onDeleteMedia(0, MediaType.Image, uri)
                    },
                    onItemClick = {
                        context.openFile(it.uri, it.contentMediaType.mimeType)
                    }
                )
            }

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
                text = if (isInEditMode) "Update Entry" else "Add Entry",
                enabled = enableNext,
                onClick = {
                    var isValid = true
                    var shouldRequestFocus = true
                    if (!displayNameState.isValid) {
                        displayNameState.enableShowErrors()
                        isValid = false
                        if (shouldRequestFocus) {
                            displayNameFocusRequester.requestFocus()
                            shouldRequestFocus = false
                        }
                    }
                    if (!amountState.isValid) {
                        amountState.enableShowErrors()
                        isValid = false
                        if (shouldRequestFocus) {
                            amountFocusRequester.requestFocus()
                        }
                    }
                    if (!descriptionState.isValid) {
                        descriptionState.enableShowErrors()
                        isValid = false
                        if (shouldRequestFocus) {
                            descriptionFocusRequester.requestFocus()
                        }
                    }
                    if (uiState.category == null) {
                        isValid = false
                        if (shouldRequestFocus) {
                            categoryFocusRequester.requestFocus()
                        }
                    }
                    if (selectedDateMillis == null) {
                        isValid = false
                        if (shouldRequestFocus) {
                            dateFocusRequester.requestFocus()
                        }
                    }

                    if (isValid) {
                        uiAction(
                            AddAccountUiAction.Submit(
                                name = displayNameState.text,
                                description = descriptionState.text,
                            )
                        )
                    }
                },
                modifier = Modifier.height(IntrinsicSize.Min)
            )
        }

        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = {
                    showDatePickerDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            showDatePickerDialog = false
                            uiAction(AddAccountUiAction.OnDatePicked(selectedDateMillis))
                        },
                        enabled = confirmEnabled // Enable button only if a date is selected
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePickerDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            ) { // Content of the dialog: The DatePicker
                DatePicker(
                    state = datePickerState,
                    // You can customize the DatePicker further here, e.g.,
                    // showModeToggle = true to allow switching between calendar and input mode
                    // title = { Text("Select a date") },
                    // headline = { Text(datePickerState.selectedDateMillis.toFormattedDateString()) }
                )
            }
        }
    }
}

@Composable
private fun AttachmentOptionDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onOptionSelected("Gallery") },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { onOptionSelected("Gallery") },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery")
                    }
                    Text(
                        text = "Gallery",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onOptionSelected("Camera") },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { onOptionSelected("Camera") },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = "Camera")
                    }
                    Text(
                        text = "Camera",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@OptIn(ExperimentalTime::class)
fun Long?.toFormattedDateString(): String {
    if (this == null) return "Not selected"
    val localDate = kotlin.time.Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())

    // Adjust for the local time zone for display purposes only if needed,
    val format = kotlinx.datetime.LocalDate.Format {
        day()
        char('-')
        monthNumber(padding = Padding.ZERO)
        char('-')
        year()
    }
    return localDate.date.format(format)
}


@Composable
private fun DisplayNameInput(
    modifier: Modifier = Modifier,
    displayNameState: TextFieldStateHandler<DisplayNameState>,
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
        OutlinedTextField(
            value = displayNameState.text,
            onValueChange = { text ->
                displayNameState.onTextChanged(text.take(DisplayNameLength))
                onValueChange(displayNameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter title",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W400),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            isError = displayNameState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    displayNameState.onFocusChanged(focusState.isFocused)
                    if (!focusState.isFocused) {
                        displayNameState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        displayNameState.errorMessage?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun AmountInput(
    modifier: Modifier = Modifier,
    amountState: TextFieldStateHandler<AmountState>,
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
        OutlinedTextField(
            value = amountState.text,
            onValueChange = { text ->
                amountState.onTextChanged(text.take(30))
                onValueChange(amountState.text)
            },
            prefix = {
                Icon(painter = painterResource(id = R.drawable.ic_rupee_symbol), contentDescription = "Amount", Modifier.size(16.dp))
            },
            placeholder = {
                Text(
                    text = "Enter amount",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next, keyboardType = KeyboardType.Decimal),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            /*supportingText = {
                if (enableCharacterCounter) {
                    val count = storeNameState.text.length
                    Text(
                        text = "$count/20",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = TextSecondary),
                        textAlign = TextAlign.End,
                        modifier = Modifier.exposeBounds()
                            .fillMaxWidth()
                    )
                }
            },*/
            textStyle = mergedTextStyle.copy(fontWeight = FontWeight.W400),
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            isError = amountState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    amountState.onFocusChanged(focusState.isFocused)
                    if (!focusState.isFocused) {
                        amountState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        amountState.errorMessage?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun DescriptionInput(
    modifier: Modifier = Modifier,
    bioState: TextFieldStateHandler<DescriptionState>,
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
                append("Description")
                withStyle(
                    style = SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    append(" (Optional)")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = bioState.text,
            onValueChange = { text ->
                bioState.onTextChanged(text.take(DescriptionLength))
                onValueChange(bioState.text)
            },
            placeholder = {
                Text(
                    text = "I'm so cool!",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            supportingText = {
                if (enableCharacterCounter) {
                    val count = bioState.text.length
                    Text(
                        text = "$count/$DescriptionLength",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            },
            textStyle = mergedTextStyle,
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            isError = bioState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    bioState.onFocusChanged(focusState.isFocused)
                    if (!focusState.isFocused) {
                        bioState.enableShowErrors()
                    }
                }
        )

        bioState.errorMessage?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun CategoryInput(
    modifier: Modifier = Modifier,
    category: Category? = null,
    onClick: () -> Unit,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    Column(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .expandable(
                expanded = true,
                onExpandedChange = {
                    onClick()
                },
                expandedDescription = "Show category picker",
                collapsedDescription = "Hide category picker",
                toggleDescription = "Toggle category picker"
            )
    ) {
        OutlinedTextField(
            value = category?.name ?: "Select category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
        )
    }
}

@Composable
private fun PartyInput(
    modifier: Modifier = Modifier,
    party: Party? = null,
    onClick: () -> Unit,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    Column(
        modifier
            .expandable(
                expanded = true,
                onExpandedChange = {
                    onClick()
                },
                expandedDescription = "Show party picker",
                collapsedDescription = "Hide party picker",
                toggleDescription = "Toggle party picker"
            )
    ) {
        OutlinedTextField(
            value = party?.name ?: "Select party",
            onValueChange = {},
            readOnly = true,
            label = { Text("Party") },
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
        )
    }
}

@Composable
private fun BankInput(
    modifier: Modifier = Modifier,
    bank: Bank? = null,
    onClick: () -> Unit,
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    Column(
        modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .expandable(
                expanded = true,
                onExpandedChange = {
                    onClick()
                },
                expandedDescription = "Show bank picker",
                collapsedDescription = "Hide bank picker",
                toggleDescription = "Toggle bank picker"
            )
    ) {
        OutlinedTextField(
            value = bank?.name ?: "Select bank",
            onValueChange = {},
            readOnly = true,
            label = { Text("Bank") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
        )
    }
}

@Composable
private fun AttachmentInput(
    modifier: Modifier = Modifier,
    mediaFiles: List<UploadPreviewUiModel> = listOf(
        UploadPreviewUiModel.Placeholder(0)),
    maxFiles: Int = 1,
    onItemClick: (Attachment) -> Unit = {},
    onPlaceHolderClick: () -> Unit = {},
    onDeleteClick: (uri: Uri) -> Unit = {},
    provideFocusRequester: () -> FocusRequester = { FocusRequester() },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = insetMedium, vertical = insetSmall),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            mediaFiles.forEach { mediaFile ->
                if (mediaFile is UploadPreviewUiModel.Placeholder) {
                    AttachmentMediaPlaceHolder(
                        onClick = onPlaceHolderClick
                    )
                } else if (mediaFile is UploadPreviewUiModel.Item) {
                    AttachmentMediaRowItem(
                        attachment = mediaFile.attachment,
                        onDeleteClick = onDeleteClick,
                        onClick = {
                            onItemClick(mediaFile.attachment)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentMediaRowItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    onClick: () -> Unit = {},
    onDeleteClick: (uri: Uri) -> Unit = {},
) {
    Box(
        modifier = modifier
            .padding(insetSmall)
            .widthIn(min = 80.dp, max = 150.dp)
            .aspectRatio(0.7F)
            .background(LightGray100)
            .clip(shape = RoundedCornerShape(cornerSizeMedium))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (attachment.contentMediaType == MediaType.Video) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attachment.uri)
                    .videoFrameMillis(1_000)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product video",
                contentScale = ContentScale.Crop,
            )

            Row(
                modifier = Modifier
                    .padding(insetSmall)
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val minsUntil = TimeUnit.MILLISECONDS.toMinutes(attachment.duration)
                val secondsUntil =
                    attachment.duration - (TimeUnit.MINUTES.toMillis(minsUntil))
                val time = String.format(
                    "%02d:%02d",
                    minsUntil,
                    TimeUnit.MILLISECONDS.toSeconds(secondsUntil)
                )

                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.2F),
                            shape = RoundedCornerShape(cornerSizeSmall),
                        ),
                )
                if ((attachment.height > attachment.width)
                    && attachment.width >= 720 || BuildConfig.DEBUG) {
                    Icon(
                        imageVector = HandbookIcons.Hd,
                        contentDescription = "Video quality",
                        modifier = Modifier
                            .width(mediumIconSize),
                    )
                }
            }

            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = HandbookIcons.Play,
                    contentDescription = "Video Preview",
                    tint = Color.Black.copy(alpha = 0.5F),
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(elevation = 1.dp)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attachment.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product image",
                contentScale = ContentScale.Crop,
            )
        }
        Button(
            onClick = { onDeleteClick(attachment.uri) },
            modifier = Modifier
                .padding(insetVerySmall)
                .align(Alignment.TopEnd)
                .heightIn(max = smallButtonHeightMax),
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.White,
                containerColor = Color(0x78000000),
            )
        ) {
            Text(
                text = "Remove",
                style = MaterialTheme.typography.labelMedium
                    .copy(fontWeight = FontWeight.W600)
            )
        }


        ifDebug {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${attachment.width}x${attachment.height}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(insetVerySmall)
                        .background(
                            color = Color.Black.copy(alpha = 0.2F),
                            shape = RoundedCornerShape(cornerSizeSmall),
                        ),
                )
                Text(
                    text = "Remote File: ${attachment.uri}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(insetVerySmall)
                        .background(
                            color = Color.Black.copy(alpha = 0.2F),
                            shape = RoundedCornerShape(cornerSizeSmall),
                        ),
                )
            }
        }
    }
}

@Composable
private fun AttachmentMediaPlaceHolder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .padding(insetSmall)
            .widthIn(min = 80.dp, max = 150.dp)
            .aspectRatio(0.7F)
            .background(LightGray100)
            .dashedBorder(
                border = BorderStroke(1.dp, LightGray200),
                shape = RoundedCornerShape(cornerSizeMedium),
                on = 10.dp,
                off = 10.dp,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = HandbookIcons.Id_PickMedia),
                contentDescription = "Pick from gallery",
                modifier = Modifier
                    .width(48.dp)
                    .aspectRatio(1F)
            )
            Text(
                text = "Tap to open Gallery",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun StoragePermissionRationaleDialog(
    openSettings: Boolean = false,
    onDismiss: (canceled: Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss(true) },
        icon = {
            Icon(
                painter = painterResource(id = HandbookIcons.Id_FilePermission),
                contentDescription = "Storage Permission Required",
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxWidth(0.2F),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss(false)
            }) {
                if (openSettings) {
                    Text(text = stringResource(id = R.string.settings))
                } else {
                    Text(text = stringResource(id = R.string.label_ok))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss(true) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text(text = stringResource(id = R.string.label_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.permissions_required)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.files_permission_des),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
            )
        }
    )
}

private fun Context.checkStoragePermission(): Boolean =
    storagePermissions.all {
        ContextCompat.checkSelfPermission(this, it) ==
                PackageManager.PERMISSION_GRANTED
    }

private fun Context.checkCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

private fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri

    try {
        val resolveInfo: ResolveInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_ALL
            )
        }
        if (resolveInfo != null) {
            startActivity(intent)
        } else {
            showToast("No apps can perform this action.")
        }
    } catch (e: Exception) {
        ifDebug { Timber.e(e) }
        showToast(getString(R.string.unable_to_perform_this_action))
    }
}

private fun Context.openFile(uri: Uri, type: String) {
    val intent = Intent(Intent.ACTION_VIEW)

    val fileToShare: File
    if (uri.scheme == "file") {
        fileToShare = File(uri.path!!) // path will be like /data/user/0/com.handbook.app/files/attachments/...
        try {
            // Replace "com.handbook.app.provider" with the authority you defined in AndroidManifest.xml
            val authority = "${applicationContext.packageName}.provider"
            val contentUri = FileProvider.getUriForFile(this, authority, fileToShare)
            intent.apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, type)
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "FileProvider URI generation failed. Check your file_paths.xml and authority.")
            showToast("Error generating file URI for sharing.")
            return
        }
    } else if (uri.scheme == "content") {
        intent.apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, type)
        }
    } else {
        // If the URI is already a content URI or some other scheme you can handle directly
        showToast("Cannot open this type of URI with FileProvider directly.")
        // Or handle it differently
        return
    }

    try {
        val resolveInfo: ResolveInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_ALL
            )
        }
        if (resolveInfo != null) {
            startActivity(intent)
        } else {
            showToast("No apps can perform this action.")
        }
    } catch (e: Exception) {
        ifDebug { Timber.e(e) }
        showToast(getString(R.string.unable_to_perform_this_action))
    }
}

private fun preProcessUris(
    context: Context,
    viewModel: AddAccountViewModel,
    mediaType: MediaType,
    pickedUris: List<Uri>,
    onComplete: (duplicateCount: Int) -> Unit = {}
) {
    Timber.d("preProcessUris() called with: context = [$context], viewModel = [$viewModel], contentMediaType = [$mediaType], pickedUris = [$pickedUris], onComplete = [$onComplete]")
    val maxPick = viewModel.getMaxAttachments()
    viewModel.removeDuplicateMedia(
        mediaType,
        pickedUris.take(maxPick)
    ) { duplicateCount, newUris ->
        Timber.d("Duplicate result: $duplicateCount $newUris")
        onComplete(duplicateCount)

        val newPreviewModelList = mutableListOf<UploadPreviewUiModel.Item>()

        val retriever = MediaMetadataRetriever()
        newUris.forEach { uri ->
            try {
                val sellerMediaFile = when (mediaType) {
                    MediaType.Video -> {
                        retriever.setDataSource(context, uri)
                        val thumbnail = retriever.frameAtTime
                        val durationMillis = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLongOrNull() ?: 0L
                        Timber.d("Duration: $durationMillis")
                        Attachment(
                            uri = uri,
                            entryId = 0,
                            filePath = uri.toString(),
                            contentMediaType = mediaType,
                            width = thumbnail?.width ?: 0,
                            height = thumbnail?.height ?: 0,
                            duration = durationMillis
                        )
                    }
                    MediaType.Image -> {
                        val imageRes = StorageUtil.getImageResolution(context, uri)
                        Attachment(
                            uri = uri,
                            entryId = 0,
                            filePath = uri.toString(),
                            contentMediaType = mediaType,
                            width = imageRes.width,
                            height = imageRes.height,
                        )
                    }
                    else -> {
                        Attachment(
                            uri = uri,
                            entryId = 0,
                            filePath = uri.toString(),
                            contentMediaType = mediaType,
                            width = 0,
                            height = 0,
                        )
                    }
                }
                newPreviewModelList.add(UploadPreviewUiModel.Item(sellerMediaFile))
            } catch (ignore: Exception) { Timber.e(ignore) }
        }
        retriever.close()

        Timber.d("New uris: size = ${newPreviewModelList.size}")
        viewModel.setPickedMediaUris(
            mediaType = mediaType,
            newPreviewModelList = newPreviewModelList
        )
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    text: String = "Add Entry",
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    // block:start:button
    Box(
        modifier = modifier
            .padding(insetMedium),
    ) {
        // TODO: replace with loading button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(cornerSizeMedium),
            enabled = enabled,
            onClick = onClick,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
                    .copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
    // block:end:button
}

@Composable
private fun EntryTypeSelector(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState.AddAccountForm,
    onToggle: (AddAccountUiAction.OnEntryTypeToggle) -> Unit,
) {
    val options = EntryType.entries.map { it.name }
    var selectedOption by remember(uiState.entryType) {
        mutableIntStateOf(
            options.indexOf(uiState.entryType.name)
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val checkedIcons = listOf(
            Icons.Default.AttachMoney,
            Icons.Default.AccountBalance,
            Icons.Default.AccountBalanceWallet
        )
        val unCheckedIcons = listOf(
            Icons.Outlined.AttachMoney,
            Icons.Outlined.AccountBalance,
            Icons.Outlined.AccountBalanceWallet
        )
        SimpleButtonGroup(
            options = options,
            unCheckedIcons = unCheckedIcons,
            checkedIcons = checkedIcons,
            selectedOptionIndex = selectedOption,
            onOptionSelected = {
                selectedOption = it
                onToggle(AddAccountUiAction.OnEntryTypeToggle(EntryType.entries[it]))
            }
        )
    }
}

@Composable
private fun TransactionTypeSelector(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState.AddAccountForm,
    onToggle: (AddAccountUiAction.OnTransactionTypeToggle) -> Unit,
) {
    val options = TransactionType.entries.map { it.name }
    var selectedOption by remember(uiState.transactionType) { mutableStateOf(uiState.transactionType.name) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleDropDownPicker(
            modifier = Modifier.fillMaxWidth(),
            label = "Transaction Type",
            options = options,
            selectedOption = selectedOption,
            selectedOptionContent = {
                TransactionTypeView(transactionType = TransactionType.fromString(selectedOption))
            },
            onOptionSelected = {
                selectedOption = it
                onToggle(AddAccountUiAction.OnTransactionTypeToggle(TransactionType.fromString(it)))
            },
            dropDownContentForOption = { option ->
                TransactionTypeView(Modifier.fillMaxWidth(), transactionType = TransactionType.fromString(option))
            }
        )
    }
}

@Composable
private fun TransactionTypeSelector2(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState.AddAccountForm,
    onToggle: (AddAccountUiAction.OnTransactionTypeToggle) -> Unit,
) {
    val options = TransactionType.entries.map { it.name }
    var selectedOption by remember { mutableStateOf(uiState.transactionType.name) }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SimpleDropDownPicker(
            label = "Transaction Type",
            options = options,
            selectedOption = selectedOption,
            selectedOptionContent = {
                TransactionTypeView(transactionType = TransactionType.fromString(selectedOption))
            },
            onOptionSelected = {
                selectedOption = it
                onToggle(AddAccountUiAction.OnTransactionTypeToggle(TransactionType.fromString(it)))
            },
            dropDownContentForOption = { option ->
                TransactionTypeView(Modifier.fillMaxWidth(), transactionType = TransactionType.fromString(option))
            },
        )
    }
}

@Preview(
    showBackground = false,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, group = "screen"
)
@Composable
private fun AddAccountScreenPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        AddAccountScreen(
            uiState = AddAccountUiState.AddAccountForm("", "", mediaFiles = listOf(
                UploadPreviewUiModel.Placeholder(0))),
            uiAction = {},
            isInEditMode = true
        )
    }
}

@Composable
private fun TransactionTypeView(
    modifier: Modifier = Modifier,
    transactionType: TransactionType,
) {
    when (transactionType) {
        TransactionType.INCOME -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_outward_24),
                    contentDescription = "Income",
                    tint = Color.Green,
                    modifier = Modifier
                        .rotate(-180f)
                )
                Text(text = transactionType.name)
            }
        }

        TransactionType.EXPENSE -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_outward_24),
                    contentDescription = "Expense",
                    tint = Color.Red,
                )
                Text(text = transactionType.name)
            }
        }
    }
}

@Preview(showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, group = "components")
@Composable
private fun TransactionTypeSelecorPreview() {
    val options = TransactionType.entries.map { it.name }
    var selectedOption by remember { mutableStateOf(options[0]) }

    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SimpleDropDownPicker(
                label = "Transaction Type",
                options = options,
                selectedOption = selectedOption,
                selectedOptionContent = {
                    TransactionTypeView(transactionType = TransactionType.fromString(selectedOption))
                },
                onOptionSelected = {
                    selectedOption = it
                },
                dropDownContentForOption = {
                    TransactionTypeView(transactionType = TransactionType.fromString(it))
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, group = "components")
@Composable
private fun EntryTypeSelectorPreview() {
    val options = EntryType.entries.map { it.name }
    var selectedOption by remember { mutableIntStateOf(0) }

    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val checkedIcons = listOf(
                Icons.Default.AttachMoney,
                Icons.Default.AccountBalance,
                Icons.Default.AccountBalanceWallet
            )
            val unCheckedIcons = listOf(
                Icons.Outlined.AttachMoney,
                Icons.Outlined.AccountBalance,
                Icons.Outlined.AccountBalanceWallet
            )
            SimpleButtonGroup(
                options = options,
                unCheckedIcons = unCheckedIcons,
                checkedIcons = checkedIcons,
                selectedOptionIndex = selectedOption,
                onOptionSelected = { selectedOption = it }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, group = "components")
@Composable
private fun TransactionTypeViewPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TransactionTypeView(Modifier.fillMaxWidth(), transactionType = TransactionType.INCOME)
            TransactionTypeView(Modifier.fillMaxWidth(), transactionType = TransactionType.EXPENSE)

            Spacer(Modifier.height(16.dp))

            TransactionTypeSelector2(
                uiState = AddAccountUiState.AddAccountForm("", "", transactionType = TransactionType.INCOME),
            ) {

            }
        }
    }
}