@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.accounts.addaccount

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.handbook.app.ObserverAsEvents
import com.handbook.app.R
import com.handbook.app.core.designsystem.component.ConfirmBackPressDialog
import com.handbook.app.core.designsystem.component.CustomConfirmDialog
import com.handbook.app.core.designsystem.component.DialogActionType
import com.handbook.app.core.designsystem.component.TextFieldError
import com.handbook.app.core.designsystem.component.expandable
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.TextFieldStateHandler
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
import com.handbook.app.showToast
import com.handbook.app.ui.cornerSizeMedium
import com.handbook.app.ui.insetMedium
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.insetVerySmall
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.TextSecondary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AddAccountRoute(
    navController: NavHostController,
    viewModel: AddAccountViewModel = hiltViewModel(),
    onNextPage: () -> Unit = {},
    onSelectCategoryRequest: (selectedCategoryId: Long) -> Unit,
    onSelectPartyRequest: (selectedPartyId: Long) -> Unit = {}
) {
    val context = LocalContext.current

    val onNextPageLatest by rememberUpdatedState(onNextPage)

    var confirmBackPress by remember { mutableStateOf(false) }
    BackHandler {
        confirmBackPress = true
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editEntryId by viewModel.accountEntryId.collectAsStateWithLifecycle()

    AddAccountScreen(
        uiState = uiState,
        uiAction = viewModel.accept,
        isInEditMode = editEntryId.isNotBlank(),
        onNavUp = onNextPageLatest
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
            context.showToast("Entry added")
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
                context.showToast("Navigating to category: ${event.categoryId}")
                onSelectCategoryRequest(event.categoryId)
            }
            is AddAccountUiEvent.NavigateToPartySelection -> {
                context.showToast("Navigating to party: ${event.partyId}")
                onSelectPartyRequest(event.partyId)
            }
        }
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
            }
        }
        navBackStackEntry?.lifecycle?.addObserver(observer)


        onDispose {
            navBackStackEntry?.lifecycle?.removeObserver(observer)
        }
    }
}

@Composable
private fun AddAccountScreen(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState,
    uiAction: (AddAccountUiAction) -> Unit,
    isInEditMode: Boolean = false,
    onNavUp: () -> Unit = {},
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
                            isInEditMode = isInEditMode
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

@Composable
private fun ColumnScope.AddAccountFormLayout(
    modifier: Modifier = Modifier,
    uiState: AddAccountUiState.AddAccountForm,
    uiAction: (AddAccountUiAction) -> Unit,
    isInEditMode: Boolean = false,
) {
    val displayNameFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val categoryFocusRequester = remember { FocusRequester() }
    val dateFocusRequester = remember { FocusRequester() }

    val displayNameState = DisplayNameState.createTextFieldStateHandler(uiState.title)

    val amountState by rememberSaveable {
        mutableStateOf(AmountState(uiState.amount.toString()))
    }
    val descriptionState by rememberSaveable {
        mutableStateOf(DescriptionState(uiState.description))
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long>(uiState.transactionDate) }

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
    }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            EntryTypeSelector(
                uiState = uiState,
                onToggle = uiAction,
            )

            TransactionTypeSelector(
                uiState = uiState,
                onToggle = uiAction,
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
                    )
                }

                PartyInput(
                    modifier = Modifier.weight(1f)
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
                modifier = Modifier.padding(horizontal = insetSmall),
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
    amountState: TextFieldState,
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
                amountState.text = text.take(30)
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
                    amountState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        amountState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        amountState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun DescriptionInput(
    modifier: Modifier = Modifier,
    bioState: TextFieldState,
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
                bioState.text = text.take(DescriptionLength)
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
                    bioState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        bioState.enableShowErrors()
                    }
                }
        )

        bioState.getError()?.let { error ->
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
    var selectedOption by remember {
        mutableIntStateOf(
            options.indexOf(uiState.entryType.name)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
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
// @ThemePreviews
@Composable
private fun AddAccountScreenPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        AddAccountScreen(
            uiState = AddAccountUiState.AddAccountForm("", ""),
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

        TransactionType.TRANSFER -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_currency_exchange_24),
                    contentDescription = "Transfer",
                    tint = Color.Blue,
                    modifier = Modifier.size(16.dp)
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
            TransactionTypeView(Modifier.fillMaxWidth(), transactionType = TransactionType.TRANSFER)

            Spacer(Modifier.height(16.dp))

            TransactionTypeSelector2(
                uiState = AddAccountUiState.AddAccountForm("", "", transactionType = TransactionType.INCOME),
            ) {

            }
        }
    }
}