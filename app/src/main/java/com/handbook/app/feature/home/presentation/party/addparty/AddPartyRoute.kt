@file:OptIn(ExperimentalMaterial3Api::class)

package com.handbook.app.feature.home.presentation.party.addparty

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.handbook.app.ObserverAsEvents
import com.handbook.app.core.designsystem.component.ConfirmBackPressDialog
import com.handbook.app.core.designsystem.component.CustomConfirmDialog
import com.handbook.app.core.designsystem.component.DialogActionType
import com.handbook.app.core.designsystem.component.TextFieldError
import com.handbook.app.core.designsystem.component.ThemePreviews
import com.handbook.app.core.designsystem.component.text.TextFieldState
import com.handbook.app.core.designsystem.component.text.TextFieldStateHandler
import com.handbook.app.feature.home.presentation.party.components.form.AddressLength
import com.handbook.app.feature.home.presentation.party.components.form.AddressState
import com.handbook.app.feature.home.presentation.party.components.form.AddressStateSaver
import com.handbook.app.feature.home.presentation.party.components.form.DescriptionLength
import com.handbook.app.feature.home.presentation.party.components.form.DescriptionState
import com.handbook.app.feature.home.presentation.party.components.form.DescriptionStateSaver
import com.handbook.app.feature.home.presentation.party.components.form.DisplayNameLength
import com.handbook.app.feature.home.presentation.party.components.form.DisplayNameState
import com.handbook.app.feature.home.presentation.party.components.form.PhoneNumberLength
import com.handbook.app.feature.home.presentation.party.components.form.PhoneNumberState
import com.handbook.app.feature.home.presentation.party.components.form.PhoneNumberStateSaver
import com.handbook.app.showToast
import com.handbook.app.ui.cornerSizeMedium
import com.handbook.app.ui.insetMedium
import com.handbook.app.ui.insetSmall
import com.handbook.app.ui.insetVerySmall
import com.handbook.app.ui.theme.HandbookTheme
import com.handbook.app.ui.theme.TextSecondary

@Composable
internal fun AddPartyRoute(
    viewModel: AddPartyViewModel = hiltViewModel(),
    onNextPage: () -> Unit = {},
) {
    val context = LocalContext.current

    val onNextPageLatest by rememberUpdatedState(onNextPage)

    var confirmBackPress by remember { mutableStateOf(false) }
    BackHandler {
        confirmBackPress = true
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editPartyId by viewModel.partyId.collectAsStateWithLifecycle()

    AddPartyScreen(
        uiState = uiState,
        uiAction = viewModel.accept,
        isInEditMode = editPartyId != 0L,
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
        if (uiState == AddPartyUiState.AddPartySuccess) {
            context.showToast("Party added")
            // viewModel.accept(AddPartyUiAction.Reset)
            onNextPageLatest()
        }
    }

    ObserverAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is AddPartyUiEvent.ShowToast -> {
                context.showToast(event.message.asString(context))
            }
            AddPartyUiEvent.OnNavUp -> {
                onNextPageLatest()
            }
        }
    }
}

@Composable
private fun AddPartyScreen(
    modifier: Modifier = Modifier,
    uiState: AddPartyUiState,
    uiAction: (AddPartyUiAction) -> Unit,
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
                        "Edit Party"
                    } else {
                        "Add Party"
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
                    AddPartyUiState.AddPartySuccess -> {
                        // noop
                    }

                    is AddPartyUiState.AddPartyForm -> {
                        AddPartyFormLayout(
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
                title = "Delete party",
                description = "Are you sure you want to delete this party?",
                onDismiss = { confirmDelete = false },
                confirmActionType = DialogActionType.DESTRUCTIVE,
                onConfirm = {
                    confirmDelete = false
                    uiAction(AddPartyUiAction.DeleteParty)
                }
            )
        }
    }
}

@Composable
private fun ColumnScope.AddPartyFormLayout(
    modifier: Modifier = Modifier,
    uiState: AddPartyUiState.AddPartyForm,
    uiAction: (AddPartyUiAction) -> Unit,
    isInEditMode: Boolean = false,
) {
    val displayNameFocusRequester = remember { FocusRequester() }
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }

    val displayNameState = DisplayNameState.createTextFieldStateHandler()

    val phoneNumberState by rememberSaveable(stateSaver = PhoneNumberStateSaver) {
        mutableStateOf(PhoneNumberState(uiState.contact))
    }
    val descriptionState by rememberSaveable(stateSaver = DescriptionStateSaver) {
        mutableStateOf(DescriptionState(uiState.description))
    }
    val addressState by rememberSaveable(stateSaver = AddressStateSaver) {
        mutableStateOf(AddressState(uiState.address))
    }

    var enableDescription by remember { mutableStateOf(false) }
    var enableAddress by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (displayNameState.text != uiState.name) {
            displayNameState.onTextChanged(uiState.name)
        }
        if (phoneNumberState.text != uiState.contact) {
            phoneNumberState.updateText(uiState.contact)
        }
        if (descriptionState.text != uiState.description) {
            descriptionState.updateText(uiState.description)
            enableDescription = descriptionState.text.isNotBlank()
        }
        if (addressState.text != uiState.address) {
            addressState.updateText(uiState.address)
            enableAddress = addressState.text.isNotBlank()
        }
    }

    DisplayNameInput(
        displayNameState = displayNameState,
        provideFocusRequester = { displayNameFocusRequester }
    )
    PhoneNumberInput(
        phoneNumberState = phoneNumberState,
        provideFocusRequester = { phoneNumberFocusRequester }
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

    TextButton(
        modifier = Modifier.padding(horizontal = insetSmall),
        onClick = {
            enableAddress = !enableAddress
            if (!enableAddress) {
                addressState.updateText("")
            }
        }
    ) {
        if (enableAddress) {
            Text("⛔ Remove address")
        } else {
            Text("🏠 Add address")
        }
    }

    AnimatedVisibility(enableAddress) {
        AddressInput(
            addressState = addressState,
            provideFocusRequester = { addressFocusRequester }
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
        text = if (isInEditMode) "Update Party" else "Add Party",
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
            if (!phoneNumberState.isValid) {
                phoneNumberState.enableShowErrors()
                isValid = false
                phoneNumberFocusRequester.requestFocus()
                shouldRequestFocus = false
            }
            if (!descriptionState.isValid) {
                descriptionState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    descriptionFocusRequester.requestFocus()
                }
            }
            if (!addressState.isValid) {
                addressState.enableShowErrors()
                isValid = false
                if (shouldRequestFocus) {
                    addressFocusRequester.requestFocus()
                    shouldRequestFocus = false
                }
            }

            if (isValid) {
                uiAction(
                    AddPartyUiAction.Submit(
                        name = displayNameState.text,
                        contact = phoneNumberState.text,
                        description = descriptionState.text,
                        address = addressState.text
                    )
                )
            }
        },
        modifier = Modifier.height(IntrinsicSize.Min)
    )
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
        Text(
            text = buildAnnotatedString {
                append("Display Name")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = displayNameState.text,
            onValueChange = { text ->
                displayNameState.updateText(text.take(DisplayNameLength))
                onValueChange(displayNameState.text)
            },
            placeholder = {
                Text(
                    text = "Enter name",
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
private fun PhoneNumberInput(
    modifier: Modifier = Modifier,
    phoneNumberState: TextFieldState,
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
                append("Phone Number")
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(0.2f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("*")
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = phoneNumberState.text,
            onValueChange = { text ->
                phoneNumberState.text = text.take(PhoneNumberLength)
                onValueChange(phoneNumberState.text)
            },
            placeholder = {
                Text(
                    text = "Enter number",
                    style = mergedTextStyle.copy(color = TextSecondary)
                )
            },
            keyboardOptions = KeyboardOptions.Default
                .copy(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Phone
                ),
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
            textStyle = mergedTextStyle,
            maxLines = 1,
            shape = RoundedCornerShape(cornerSizeMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            isError = phoneNumberState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    phoneNumberState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        phoneNumberState.enableShowErrors()
                    }
                }
                .padding(vertical = insetVerySmall),
        )

        phoneNumberState.getError()?.let { error ->
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
private fun AddressInput(
    modifier: Modifier = Modifier,
    addressState: TextFieldState,
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
                append("Address")
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
            value = addressState.text,
            onValueChange = { text ->
                addressState.text = text.take(AddressLength)
                onValueChange(addressState.text)
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
                    val count = addressState.text.length
                    Text(
                        text = "$count/$AddressLength",
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
            isError = addressState.showErrors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .focusRequester(provideFocusRequester())
                .onFocusChanged { focusState ->
                    addressState.onFocusChange(focusState.isFocused)
                    if (!focusState.isFocused) {
                        addressState.enableShowErrors()
                    }
                }
        )

        addressState.getError()?.let { error ->
            TextFieldError(textError = error)
        }
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    text: String = "Add Party",
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

@Preview(
    showBackground = false,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@ThemePreviews
@Composable
private fun AddPartyScreenPreview() {
    HandbookTheme(
        androidTheme = true,
        disableDynamicTheming = true
    ) {
        AddPartyScreen(
            uiState = AddPartyUiState.AddPartyForm("", "", "", ""),
            uiAction = {},
            isInEditMode = true
        )
    }
}



