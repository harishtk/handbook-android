package com.handbook.app.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService

/**
 * CAUTION: This returns 'false' on some scenario like,
 * 1. When app's background running is restricted by device policy.
 * 2. When the device is asleep and locked.
 *
 * @return Boolean - A Boolean indicating the connection state.
 * */
@Suppress("DEPRECATION")
fun Context.isConnected(): Boolean {
    return getSystemService<ConnectivityManager>()?.let { connectivityManager ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val nwInfo  = connectivityManager.activeNetworkInfo ?: return false
            if (nwInfo.type == ConnectivityManager.TYPE_WIFI) return true

            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw   = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            nwInfo.isConnected
        }
    } ?: false
}