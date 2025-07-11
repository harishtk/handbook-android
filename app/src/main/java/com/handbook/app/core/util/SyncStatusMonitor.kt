package com.handbook.app.core.util

import kotlinx.coroutines.flow.Flow

/**
 * Reports on if any synchronization is in progress.
 */
interface SyncStatusMonitor {
    val isSyncing: Flow<Boolean>
}