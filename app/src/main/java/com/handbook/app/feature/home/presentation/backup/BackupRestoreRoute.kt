package com.handbook.app.feature.home.presentation.backup

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreRoute(
    viewModel: BackupRestoreViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Launcher for runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onEvent(BackupRestoreEvent.PermissionsResult(permissions))
    }

    // Launcher for Android 11+ delete confirmation
    val deleteIntentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val itemToDelete = (viewModel.effect.replayCache.lastOrNull() as? BackupRestoreEffect.LaunchIntentSender)?.itemToDelete
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onEvent(BackupRestoreEvent.DeletePermissionResult(success = true, itemToDelete = itemToDelete))
        } else {
            viewModel.onEvent(BackupRestoreEvent.DeletePermissionResult(success = false, itemToDelete = itemToDelete))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BackupRestoreEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is BackupRestoreEffect.LaunchIntentSender -> {
                    deleteIntentSenderLauncher.launch(effect.intentSenderRequest)
                }
            }
        }
    }

    // Handle permission requests queued by the ViewModel
    LaunchedEffect(uiState.permissionRequestQueue) {
        if (uiState.permissionRequestQueue.isNotEmpty()) {
            val permissionsToRequest = uiState.permissionRequestQueue.toTypedArray()
            val allPermissionsGranted = permissionsToRequest.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            if (!allPermissionsGranted) { // Only launch if not already granted
                permissionLauncher.launch(permissionsToRequest)
            } else {
                // If somehow they were already granted, clear the queue by sending an empty result
                // or a specific event. This path should ideally not be hit if logic is correct.
                viewModel.onEvent(BackupRestoreEvent.PermissionsResult(permissionsToRequest.associateWith { true }))
            }
        }
    }

    BackupRestoreScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onRestartApp = {
            // This is a direct way to restart. Consider if this is the best UX.
            // It might be better to show a dialog within BackupRestoreScreen first,
            // then send a RestartAppClicked event.
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            (context as? Activity)?.startActivity(mainIntent) // Check if context is an Activity
            Runtime.getRuntime().exit(0)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    uiState: BackupRestoreState,
    snackbarHostState: SnackbarHostState,
    onEvent: (BackupRestoreEvent) -> Unit,
    onNavigateBack: () -> Unit = {}, // Optional: For back navigation
    onRestartApp: () -> Unit // Callback to restart the app, passed from Route
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    // Assuming you might want a back button
                    if (onNavigateBack != {}) { // Show only if a handler is provided
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onEvent(BackupRestoreEvent.CreateBackupClicked) },
                enabled = !uiState.isBackingUp && !uiState.isRestoring,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isBackingUp) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Backing up...")
                } else {
                    Text("Create New Backup")
                }
            }

            Button(
                onClick = { onEvent(BackupRestoreEvent.ViewBackupsClicked) },
                enabled = !uiState.isLoadingBackups && !uiState.isRestoring && !uiState.isBackingUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoadingBackups) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Loading Backups...")
                } else {
                    Text("View / Restore from Backups")
                }
            }

            if (uiState.isRestoring) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
                Text("Restoring database, please wait...", textAlign = TextAlign.Center)
            }

            // Could add a section to display info about last backup, etc.
            // if (uiState.lastBackupInfo != null) { ... }
        }
    }

    // --- Dialogs ---

    if (uiState.showBackupSelectionDialog && uiState.availableBackups.isNotEmpty()) {
        val dateFormatter = remember {
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            )
        }
        AlertDialog(
            onDismissRequest = { onEvent(BackupRestoreEvent.DismissBackupSelectionDialog) },
            title = { Text("Available Backups") },
            text = {
                if (uiState.isLoadingBackups) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.availableBackups.isEmpty()) {
                    Text("No backups found in the Downloads folder matching the app's naming convention.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.availableBackups, key = { it.uri.toString() }) { backupItem ->
                            ListItem(
                                headlineContent = { Text(backupItem.name) },
                                supportingContent = {
                                    if (backupItem.timestamp > 0) { // Display timestamp if available
                                        Text("Created: ${dateFormatter.format(Date(backupItem.timestamp))}")
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = {
                                        // Optional: Add a confirmation before deleting
                                        onEvent(BackupRestoreEvent.DeleteBackupClicked(backupItem))
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete backup")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(BackupRestoreEvent.BackupItemSelected(backupItem))
                                    }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onEvent(BackupRestoreEvent.DismissBackupSelectionDialog) }) {
                    Text("Close")
                }
            }
        )
    } else if (uiState.showBackupSelectionDialog && uiState.availableBackups.isEmpty() && !uiState.isLoadingBackups) {
        AlertDialog( // Show a different dialog if view backups was clicked but none found
            onDismissRequest = { onEvent(BackupRestoreEvent.DismissBackupSelectionDialog) },
            title = { Text("No Backups Found") },
            text = { Text("There are no backup files in your Downloads folder that match the app's naming convention.") },
            confirmButton = {
                TextButton(onClick = { onEvent(BackupRestoreEvent.DismissBackupSelectionDialog) }) {
                    Text("OK")
                }
            }
        )
    }


    if (uiState.showRestoreConfirmDialog && uiState.selectedBackupForRestore != null) {
        AlertDialog(
            onDismissRequest = { onEvent(BackupRestoreEvent.CancelRestoreClicked) },
            title = { Text("Confirm Restore") },
            text = { Text("Restoring '${uiState.selectedBackupForRestore.name}' will replace ALL current app data. This action cannot be undone. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(BackupRestoreEvent.ConfirmRestoreClicked)
                    },
                    enabled = !uiState.isRestoring
                ) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(BackupRestoreEvent.CancelRestoreClicked) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showPostRestoreDialog) {
        AlertDialog(
            onDismissRequest = {
                onEvent(BackupRestoreEvent.DismissPostRestoreDialog)
                // App will likely be restarted by the confirm button action
            },
            title = { Text("Restore Complete") },
            text = { Text("The database has been restored. The application needs to restart for changes to take full effect.") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(BackupRestoreEvent.DismissPostRestoreDialog) // Dismiss dialog first
                    onRestartApp() // Trigger the restart passed from Route
                }) {
                    Text("Restart App")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onEvent(BackupRestoreEvent.DismissPostRestoreDialog)
                }) {
                    Text("Later") // User might want to manually restart
                }
            }
        )
    }
}
