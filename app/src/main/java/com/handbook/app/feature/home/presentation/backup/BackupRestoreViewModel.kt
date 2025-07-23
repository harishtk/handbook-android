package com.handbook.app.feature.home.presentation.backup // Adjust package as needed

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appDatabase: AccountsDatabase,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupRestoreState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BackupRestoreEffect>()
    val effect = _effect.asSharedFlow()

    private val requiredPermissionsPreQ = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val requiredWritePermissionPreQ = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val requiredReadPermissionPreQ = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)


    fun onEvent(event: BackupRestoreEvent) {
        viewModelScope.launch { // Ensure processing happens within viewModelScope
            when (event) {
                is BackupRestoreEvent.CreateBackupClicked -> handleCreateBackup()
                is BackupRestoreEvent.ViewBackupsClicked -> handleViewBackups()
                is BackupRestoreEvent.BackupItemSelected -> {
                    _state.update {
                        it.copy(
                            selectedBackupForRestore = event.backupItem,
                            showRestoreConfirmDialog = true,
                            showBackupSelectionDialog = false // Close selection dialog
                        )
                    }
                }
                is BackupRestoreEvent.DeleteBackupClicked -> handleDeleteBackupRequest(event.backupItem)
                is BackupRestoreEvent.ConfirmRestoreClicked -> handleConfirmRestore()
                is BackupRestoreEvent.CancelRestoreClicked -> {
                    _state.update { it.copy(showRestoreConfirmDialog = false, selectedBackupForRestore = null) }
                }
                is BackupRestoreEvent.DismissBackupSelectionDialog -> {
                    _state.update { it.copy(showBackupSelectionDialog = false) }
                }
                is BackupRestoreEvent.DismissPostRestoreDialog -> {
                    _state.update { it.copy(showPostRestoreDialog = false) }
                }
                is BackupRestoreEvent.RestartAppClicked -> restartApplication()
                is BackupRestoreEvent.PermissionsResult -> handlePermissionsResult(event.grantedPermissions)
                is BackupRestoreEvent.PermissionsRequestDismissed -> {
                    _state.update { it.copy(permissionRequestQueue = emptyList()) } // Clear queue
                    _effect.emit(BackupRestoreEffect.ShowToast("Permissions required to proceed."))
                }
                is BackupRestoreEvent.DeletePermissionResult -> handleDeletePermissionResult(event.success, event.itemToDelete)
            }
        }
    }

    private fun checkPermissionsAndExecute(
        permissions: List<String>,
        action: () -> Job
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && permissions.any { it == Manifest.permission.WRITE_EXTERNAL_STORAGE || it == Manifest.permission.READ_EXTERNAL_STORAGE }) {
            // For Android 10+, MediaStore handles its own access to Downloads for owned content.
            // No explicit storage permission needed for backup/listing via MediaStore.
            action()
            return
        }

        val missingPermissions = permissions.filterNot {
            appContext.checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            action()
        } else {
            _state.update { it.copy(permissionRequestQueue = missingPermissions) }
        }
    }

    private fun handlePermissionsResult(grantedPermissions: Map<String, Boolean>) {
        val currentQueue = _state.value.permissionRequestQueue
        val allGrantedInQueue = currentQueue.all { grantedPermissions[it] == true }

        _state.update { it.copy(permissionRequestQueue = emptyList()) } // Clear queue regardless

        if (allGrantedInQueue) {
            // Re-trigger the original action based on which permissions were requested.
            // This is a bit simplistic; a more robust way might store the pending action.
            if (currentQueue.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                handleCreateBackup(skipPermissionCheck = true)
            } else if (currentQueue.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                handleViewBackups(skipPermissionCheck = true)
            }
        } else {
            viewModelScope.launch {
                _effect.emit(BackupRestoreEffect.ShowToast("Required permissions were not granted."))
            }
        }
    }

    private fun handleCreateBackup(skipPermissionCheck: Boolean = false) {
        val action = {
            viewModelScope.launch {
                _state.update { it.copy(isBackingUp = true) }
                val success = DatabaseBackupUtility.backupDatabase(appContext, appDatabase)
                if (success) {
                    _effect.emit(BackupRestoreEffect.ShowToast("Backup created successfully."))
                    // Optionally refresh backup list automatically
                    // val backups = DatabaseBackupUtility.getAvailableBackups(appContext)
                    // _state.update { s -> s.copy(availableBackups = mapToBackupItems(backups)) }
                } else {
                    _effect.emit(BackupRestoreEffect.ShowToast("Backup failed."))
                }
                _state.update { it.copy(isBackingUp = false) }
            }
        }
        if (skipPermissionCheck) action() else checkPermissionsAndExecute(requiredWritePermissionPreQ, action)
    }

    private fun handleViewBackups(skipPermissionCheck: Boolean = false) {
        val action = {
            viewModelScope.launch {
                _state.update { it.copy(isLoadingBackups = true) }
                val backups = DatabaseBackupUtility.getAvailableBackups(appContext)
                _state.update {
                    it.copy(
                        availableBackups = mapToBackupItems(backups),
                        isLoadingBackups = false,
                        showBackupSelectionDialog = backups.isNotEmpty()
                    )
                }
                if (backups.isEmpty()) {
                    _effect.emit(BackupRestoreEffect.ShowToast("No backups found."))
                }
            }
        }
        if (skipPermissionCheck) action() else checkPermissionsAndExecute(requiredReadPermissionPreQ, action)
    }

    private suspend fun handleDeleteBackupRequest(backupItem: BackupItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+ requires user consent for deleting via MediaStore if not solely app-owned
            try {
                // DatabaseBackupUtility needs to be adapted to create and return the PendingIntent
                // For now, let's assume it can directly provide the IntentSenderRequest
                val pendingIntent = MediaStore.createDeleteRequest(appContext.contentResolver, listOf(backupItem.uri))
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                _effect.emit(BackupRestoreEffect.LaunchIntentSender(request, backupItem))
            } catch (e: Exception) {
                Log.e("BackupRestoreVM", "Failed to create delete request for ${backupItem.name}", e)
                _effect.emit(BackupRestoreEffect.ShowToast("Could not prepare delete action."))
                // Fallback or direct delete attempt for older versions / owned files
                performDirectDelete(backupItem.uri)
            }
        } else {
            // Direct delete for older versions or if the MediaStore.createDeleteRequest is not applicable/fails
            performDirectDelete(backupItem.uri)
        }
    }
    private fun handleDeletePermissionResult(success: Boolean, itemToDelete: BackupItem?) {
        viewModelScope.launch {
            if (success && itemToDelete != null) {
                _effect.emit(BackupRestoreEffect.ShowToast("Backup '${itemToDelete.name}' deleted."))
                _state.update { s ->
                    s.copy(availableBackups = s.availableBackups.filterNot { it.uri == itemToDelete.uri })
                }
                if (_state.value.availableBackups.isEmpty()) {
                    _state.update { it.copy(showBackupSelectionDialog = false) }
                }
            } else if (itemToDelete != null) {
                _effect.emit(BackupRestoreEffect.ShowToast("Could not delete '${itemToDelete.name}'."))
            }
        }
    }


    private suspend fun performDirectDelete(backupUri: Uri) {
        try {
            val deletedRows = appContext.contentResolver.delete(backupUri, null, null)
            if (deletedRows > 0) {
                _effect.emit(BackupRestoreEffect.ShowToast("Backup deleted."))
                _state.update { s ->
                    s.copy(availableBackups = s.availableBackups.filterNot { it.uri == backupUri })
                }
                if (_state.value.availableBackups.isEmpty()) {
                    _state.update { it.copy(showBackupSelectionDialog = false) }
                }
            } else {
                _effect.emit(BackupRestoreEffect.ShowToast("Failed to delete backup or file not found."))
            }
        } catch (e: SecurityException) {
            _effect.emit(BackupRestoreEffect.ShowToast("Permission denied to delete backup."))
            Log.e("BackupRestoreVM", "SecurityException deleting backup", e)
        } catch (e: Exception) {
            _effect.emit(BackupRestoreEffect.ShowToast("Error deleting backup: ${e.message}"))
            Log.e("BackupRestoreVM", "Error deleting backup", e)
        }
    }


    private fun handleConfirmRestore() {
        val backupToRestore = _state.value.selectedBackupForRestore ?: return
        _state.update { it.copy(showRestoreConfirmDialog = false, isRestoring = true) }

        viewModelScope.launch {
            DatabaseBackupUtility.restoreDatabase(
                context = appContext,
                database = appDatabase,
                backupFileUri = backupToRestore.uri
            ) { success ->
                // This callback is on the main thread from DatabaseBackupUtility
                viewModelScope.launch { // Switch back to viewModelScope if needed
                    if (success) {
                        _effect.emit(BackupRestoreEffect.ShowToast("Database restored."))
                        _state.update {
                            it.copy(
                                isRestoring = false,
                                showPostRestoreDialog = true,
                                selectedBackupForRestore = null
                            )
                        }
                    } else {
                        _effect.emit(BackupRestoreEffect.ShowToast("Database restore failed."))
                        _state.update { it.copy(isRestoring = false, selectedBackupForRestore = null) }
                        // Consider re-initializing the DB instance if it failed mid-way
                        // YourAppDatabase.getInstance(appContext) // Re-fetch or re-initialize
                    }
                }
            }
        }
    }

    private fun restartApplication() {
        val packageManager = appContext.packageManager
        val intent = packageManager.getLaunchIntentForPackage(appContext.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        appContext.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    private fun mapToBackupItems(backupsFromUtil: List<Pair<Uri, String>>): List<BackupItem> {
        return backupsFromUtil.mapNotNull { (uri, name) ->
            // Attempt to parse timestamp from name if possible, otherwise use 0 or current time
            val timestamp = try {
                // Assuming format like "backup_dbname_yyyyMMdd_HHmmss.db"
                val parts = name.split("_")
                if (parts.size >= 3) {
                    val dateStr = parts[parts.size - 2] // yyyyMMdd
                    val timeStr = parts.last().substringBefore(".db") // HHmmss
                    // SimpleDateFormat is not ideal here due to potential locale issues and complexity
                    // For a robust solution, store timestamp in MediaStore or use a more reliable parsing.
                    // This is a simplified example.
                    0L // Placeholder, implement proper parsing or get from MediaStore if available
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
            BackupItem(uri, name, timestamp)
        }.sortedByDescending { it.timestamp } // Or sort by name if timestamp isn't reliable
    }
}



// --- State ---
data class BackupRestoreState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isLoadingBackups: Boolean = false,
    val availableBackups: List<BackupItem> = emptyList(),
    val selectedBackupForRestore: BackupItem? = null, // URI of the backup selected for confirmation
    val showRestoreConfirmDialog: Boolean = false,
    val showBackupSelectionDialog: Boolean = false,
    val showPostRestoreDialog: Boolean = false,
    val permissionRequestQueue: List<String> = emptyList() // For managing runtime permissions
)

data class BackupItem(
    val uri: Uri,
    val name: String,
    val timestamp: Long // For sorting or display, derived from name or MediaStore
)

// --- Events (User Intents) ---
sealed interface BackupRestoreEvent {
    data object CreateBackupClicked : BackupRestoreEvent
    data object ViewBackupsClicked : BackupRestoreEvent
    data class BackupItemSelected(val backupItem: BackupItem) : BackupRestoreEvent
    data class DeleteBackupClicked(val backupItem: BackupItem) : BackupRestoreEvent
    data object ConfirmRestoreClicked : BackupRestoreEvent
    data object CancelRestoreClicked : BackupRestoreEvent
    data object DismissBackupSelectionDialog : BackupRestoreEvent
    data object DismissPostRestoreDialog : BackupRestoreEvent
    data object RestartAppClicked : BackupRestoreEvent // After restore
    data class PermissionsResult(val grantedPermissions: Map<String, Boolean>) : BackupRestoreEvent
    data object PermissionsRequestDismissed : BackupRestoreEvent // If user denies via system dialog
    data class DeletePermissionResult(val success: Boolean, val itemToDelete: BackupItem?) : BackupRestoreEvent // For Android 11+ delete launcher
}

// --- Effects (Side Effects, like Navigation or Toasts) ---
sealed interface BackupRestoreEffect {
    data class ShowToast(val message: String) : BackupRestoreEffect
    data class LaunchIntentSender(val intentSenderRequest: androidx.activity.result.IntentSenderRequest, val itemToDelete: BackupItem) : BackupRestoreEffect
    // Could add navigation effects if needed
}
