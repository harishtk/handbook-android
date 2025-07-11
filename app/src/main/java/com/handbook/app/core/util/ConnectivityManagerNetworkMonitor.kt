package com.handbook.app.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.content.getSystemService
import com.handbook.app.core.net.isConnected
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import org.jetbrains.annotations.Contract
import javax.inject.Inject

class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) : NetworkMonitor {
    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        /**
         * The callback's methods are invoked on changes to *any* network, not just the active
         * network. So to check for network connectivity, one must query the active network of the
         * ConnectivityManager.
         */
        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                // channel.trySend(connectivityManager.isCurrentlyConnected())
                channel.trySend(context.isConnected())
            }

            override fun onLost(network: Network) {
                // channel.trySend(connectivityManager.isCurrentlyConnected())
                channel.trySend(context.isConnected())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // channel.trySend(connectivityManager.isCurrentlyConnected())
                channel.trySend(context.isConnected())
            }
        }

        connectivityManager?.registerNetworkCallback(
            android.net.NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback,
        )

        // channel.trySend(connectivityManager.isCurrentlyConnected())
        channel.trySend(context.isConnected())

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }
        .conflate()

    override fun hasNetwork(): Boolean {
        return context.isConnected()
    }

    @Contract("null -> false")
    @Suppress("DEPRECATION", "ObsoleteSdkInt")
    private fun ConnectivityManager?.isCurrentlyConnected() = when (this) {
        null -> false
        else -> when {
            VERSION.SDK_INT >= VERSION_CODES.M ->
                activeNetwork
                    ?.let(::getNetworkCapabilities)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    ?: false
            else -> activeNetworkInfo?.isConnected ?: false
        }
    }
}