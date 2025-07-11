package com.handbook.app.core.net

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

private const val DEFAULT_NETWORK_MONITOR_DELAY = 3000L
private const val DEFAULT_PACKET_MONITOR_INTERVAL = 500L

class NetworkSpeedMonitor private constructor(private val intervalMs: Long = DEFAULT_NETWORK_MONITOR_DELAY) {

    private var _monitor = MutableStateFlow(NetworkSpeed.empty)
    private val monitor: StateFlow<NetworkSpeed>
        get() = _monitor

    private var _networkTxBytes = MutableStateFlow(longArrayOf(0, 0))
    val networkTxBytes = _networkTxBytes.asStateFlow()

    private val _exceptionHandler = CoroutineExceptionHandler { _, t ->
        Timber.tag("NetworkSpdMonitor").e(t)
    }
    private val context: CoroutineContext =
        Dispatchers.IO + SupervisorJob() + _exceptionHandler

    private val myScope =
        CoroutineScope(context = context)

    private var job: Job? = null
    private var dataUsageJob: Job? = null

    private var startTxByte: Long = 0
    private var startRxByte: Long = 0
    private var lastTxByte: Long = 0
    private var lastRxByte: Long = 0

    private var uid: Int = 0

    @Synchronized
    fun start() {
        if (job?.isActive == true) {
            val t = IllegalStateException("Network monitor job is already active. Ignoring start()")
            Timber.w(t)
        } else {
            job = myScope.launch {
                while (true) {
                    delay(intervalMs)
                    val bytesPerSecond = TrafficUtils.getNetworkSpeed()
                    _monitor.update { NetworkSpeed(System.currentTimeMillis(), bytesPerSecond) }
                    setCurrentSpeed(bytesPerSecond)
                }
            }
        }
    }

    private fun startDataUsageMonitor() {
        Timber.tag("NetworkSpdMonitor").d("Start call received")
        if (dataUsageJob?.isActive == true) {
            val t = IllegalStateException("Data usage job is already active. Ignoring start()")
            Timber.w(t)
        } else {
            Timber.tag("NetworkSpdMonitor").d("Starting..")
            dataUsageJob = myScope.launch {
                while (true) {
                    delay(DEFAULT_PACKET_MONITOR_INTERVAL)
                    val xBytes = TrafficUtils.getCurrentNetworkBytes(uid)
                    if (xBytes[0] != lastTxByte || xBytes[1] != lastRxByte) {
                        val xTBytes = xBytes[0] + startTxByte
                        val xRBytes = xBytes[1] + startRxByte
                        lastTxByte = xBytes[0]
                        lastRxByte = xBytes[1]
                        Timber.tag("NetworkSpdMonitor").d("lastRxByte: $lastRxByte")
                        _networkTxBytes.update { longArrayOf(xTBytes, xRBytes) }
                    }
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        if (job?.isActive == false) {
            val t = IllegalStateException("No active monitor job is running.")
            Timber.w(t)
        } else {
            val t = CancellationException("Stop request received")
            job?.cancel(t)
        }
        if (dataUsageJob?.isActive == false) {
            val t = IllegalStateException("No active data usage job is running.")
            Timber.w(t)
        } else {
            val t = CancellationException("Stop request received")
            dataUsageJob?.cancel(t)
        }
    }

    fun setAppUid(uid: Int) {
        this.uid = uid
        startDataUsageMonitor()
    }

    fun monitor(): StateFlow<NetworkSpeed> {
        return monitor
    }

    companion object {
        @Volatile
        var currentSpeed: Long = 0
            private set

        @JvmName("setCurrentSpeed1")
        @Synchronized
        fun setCurrentSpeed(bps: Long) {
            synchronized(this) {
                currentSpeed = bps
            }
        }

        private var INSTANCE: NetworkSpeedMonitor? = null

        @Synchronized
        fun getInstance(intervalMs: Long): NetworkSpeedMonitor {
            return INSTANCE ?: synchronized(this) { createInstance(intervalMs) }
        }

        private fun createInstance(intervalMs: Long) =
            Factory(intervalMs).create().also { INSTANCE = it }
    }

    class Factory(
        private val intervalMs: Long = DEFAULT_NETWORK_MONITOR_DELAY
    ) {
        fun create(): NetworkSpeedMonitor {
            return NetworkSpeedMonitor(intervalMs)
        }
    }
}

data class NetworkSpeed(
    val measuredAt: Long,
    val bytesPerSecond: Long
) {
    internal companion object {
        val empty = NetworkSpeed(0, 0)
    }
}