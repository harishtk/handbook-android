package com.handbook.app.feature.home.presentation.backup

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.room.RoomDatabase
import com.handbook.app.feature.home.data.source.local.AccountsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseBackupUtility {

    private const val TAG = "DbBackupUtility"
    const val DATABASE_NAME = AccountsDatabase.Companion.DATABASE_NAME
    private const val BACKUP_FILE_PREFIX = "backup_${DATABASE_NAME}_"
    private const val BACKUP_MIME_TYPE = "application/octet-stream" // Or "application/x-sqlite3"

    // Helper to generate a timestamped backup file name
    private fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        return "$BACKUP_FILE_PREFIX${currentTime}.db"
    }

    /**
     * Backs up the current Room database to the device's Downloads folder.
     */
    suspend fun backupDatabase(context: Context, database: RoomDatabase): Boolean {
        // Ensure database is checkpointed
        try {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL);", emptyArray()).close()
        } catch (e: Exception) {
            Log.e(TAG, "Error during WAL checkpoint", e)
            // Continue with backup, but log the issue
        }

        val currentDBFile: File = context.getDatabasePath(DATABASE_NAME)
        if (!currentDBFile.exists()) {
            Log.e(TAG, "Database file not found at: ${currentDBFile.absolutePath}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Database not found for backup.", Toast.LENGTH_LONG).show()
            }
            return false
        }

        val backupFileName = generateBackupFileName()

        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, backupFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, BACKUP_MIME_TYPE)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    var outputStream: OutputStream? = null
                    var backupUri: Uri? = null
                    try {
                        backupUri = resolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        if (backupUri == null) throw IOException("Failed to create new MediaStore entry for backup.")
                        outputStream = resolver.openOutputStream(backupUri)
                        if (outputStream == null) throw IOException("Failed to get output stream for backup.")

                        currentDBFile.inputStream().use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.i(TAG, "Backup successful: $backupFileName in Downloads (MediaStore)")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Backup saved to Downloads.", Toast.LENGTH_LONG)
                                .show()
                        }
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "MediaStore backup failed", e)
                        backupUri?.let { resolver.delete(it, null, null) } // Clean up
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Backup failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        false
                    } finally {
                        try {
                            outputStream?.close()
                        } catch (e: IOException) {
                            Log.e(TAG, "Error closing outputStream", e)
                        }
                    }
                } else {
                    // Android 9 (API 28) and below - Direct file access (requires WRITE_EXTERNAL_STORAGE)
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                        throw IOException("Failed to create Downloads directory.")
                    }
                    val backupFile = File(downloadsDir, backupFileName)

                    FileOutputStream(backupFile).use { outputStream ->
                        FileInputStream(currentDBFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.i(TAG, "Backup successful: ${backupFile.absolutePath}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup saved to Downloads.", Toast.LENGTH_LONG)
                            .show()
                    }
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backup process failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                false
            }
        }
    }

    /**
     * Lists available backup files from the Downloads folder matching our naming convention.
     * Returns a list of Pair<Uri, String> where Uri is the file Uri and String is the display name.
     */
    suspend fun getAvailableBackups(context: Context): List<Pair<Uri, String>> {
        val backups = mutableListOf<Pair<Uri, String>>()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_EXTERNAL_STORAGE permission not granted for pre-Q. Cannot list backups.")
            // Consider requesting permission or informing the user.
            return emptyList()
        }


        return withContext(Dispatchers.IO) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Files.getContentUri("external") // Generic query for older versions
            }

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED
            )

            // Query for files starting with our backup prefix in the Downloads directory
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            } else {
                "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? AND ${MediaStore.MediaColumns.DATA} LIKE ?"
            }
            val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf("$BACKUP_FILE_PREFIX%", "%${Environment.DIRECTORY_DOWNLOADS}%")
            } else {
                val downloadsPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                arrayOf("$BACKUP_FILE_PREFIX%", "%$downloadsPath%")
            }


            val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            var cursor: Cursor? = null

            try {
                cursor = context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                cursor?.use { c ->
                    val idColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val displayNameColumn =
                        c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

                    while (c.moveToNext()) {
                        val id = c.getLong(idColumn)
                        val displayName = c.getString(displayNameColumn)
                        val uri = ContentUris.withAppendedId(collection, id) // Construct item URI
                        // Filter more precisely for .db files if MIME type isn't perfectly reliable
                        if (displayName.endsWith(".db")) {
                            backups.add(Pair(uri, displayName))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying backups", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error listing backups: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                cursor?.close()
            }
            Log.d(TAG, "Found ${backups.size} backups: $backups")
            backups
        }
    }


    /**
     * Restores the database from the given backup URI.
     * WARNING: This closes the current database, replaces its files, and then expects
     * the app to re-initialize the Room database.
     *
     * @param context Context
     * @param database The current RoomDatabase instance (will be closed).
     * @param backupFileUri The URI of the backup file to restore (e.g., from MediaStore or SAF).
     * @param onRestoreCompleted Lambda called on the main thread after attempting restore (true for success, false for failure).
     *                           The caller is responsible for re-initializing the Room database.
     */
    suspend fun restoreDatabase(
        context: Context,
        database: RoomDatabase,
        backupFileUri: Uri,
        onRestoreCompleted: (Boolean) -> Unit
    ) {
        val dbName = database.openHelper.databaseName ?: DATABASE_NAME // Get actual DB name if possible
        val currentDBFile = context.getDatabasePath(dbName)
        val walFile = File(currentDBFile.parent, "$dbName-wal")
        val shmFile = File(currentDBFile.parent, "$dbName-shm")

        Log.i(TAG, "Attempting to restore database from URI: $backupFileUri")
        Log.i(TAG, "Current database file: ${currentDBFile.absolutePath}")

        // --- Critical Step: Close the database ---
        if (database.isOpen) {
            Log.i(TAG, "Closing current database for restore...")
            try {
                database.close() // This should also trigger a checkpoint.
            } catch (e: Exception) {
                Log.e(TAG, "Error closing database before restore", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error closing DB: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    onRestoreCompleted(false)
                }
                return
            }
        } else {
            Log.i(TAG, "Database already closed.")
        }

        var success = false
        withContext(Dispatchers.IO) {
            try {
                // Delete existing database files
                if (currentDBFile.exists() && !currentDBFile.delete()) Log.w(
                    TAG,
                    "Failed to delete current DB file."
                )
                if (walFile.exists() && !walFile.delete()) Log.w(TAG, "Failed to delete WAL file.")
                if (shmFile.exists() && !shmFile.delete()) Log.w(TAG, "Failed to delete SHM file.")

                // Copy backup file to database location
                context.contentResolver.openInputStream(backupFileUri)?.use { inputStream ->
                    FileOutputStream(currentDBFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IOException("Failed to open input stream from backup URI.")

                Log.i(
                    TAG,
                    "Database restore successful from $backupFileUri to ${currentDBFile.absolutePath}"
                )
                success = true
            } catch (e: Exception) {
                Log.e(TAG, "Database restore failed", e)
                // Attempt to restore original files if possible? (Complex, usually not done)
                // For now, we just report failure. The original DB is gone at this point.
                success = false
            } finally {
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            context,
                            "Database restored. Please restart the app or re-initialize data.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Database restore failed.", Toast.LENGTH_LONG)
                            .show()
                    }
                    onRestoreCompleted(success)
                }
            }
        }
    }

    /**
     * For Android 11+ (API 30), if you modify or delete a file obtained via MediaStore
     * that your app did not create, you might need user consent.
     * This is generally NOT needed for files your app *created* (like backups).
     * However, if you allow users to pick ANY .db file, this might become relevant.
     * This function is a placeholder for that more complex scenario.
     * For deleting our OWN backup files, direct MediaStore delete should work.
     */
    suspend fun requestDeletePermissionForBackup(
        activity: Activity,
        backupUri: Uri,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
            try {
                val pendingIntent = MediaStore.createDeleteRequest(activity.contentResolver, listOf(backupUri))
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                launcher.launch(request)
                // The result of this operation will be handled in the ActivityResultCallback for the launcher
                return true // Indicates request was launched
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create delete request", e)
                Toast.makeText(activity, "Failed to request delete permission: ${e.message}", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            // For older versions, direct delete via contentResolver should work if app owns the file
            // or has WRITE_EXTERNAL_STORAGE (pre-Q)
            try {
                val deletedRows = activity.contentResolver.delete(backupUri, null, null)
                if (deletedRows > 0) {
                    Toast.makeText(activity, "Backup deleted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Failed to delete backup or file not found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException deleting backup", e)
                Toast.makeText(activity, "Permission denied to delete backup.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting backup", e)
                Toast.makeText(activity, "Error deleting backup: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            return false // No separate permission flow needed, direct action attempted
        }
    }
}