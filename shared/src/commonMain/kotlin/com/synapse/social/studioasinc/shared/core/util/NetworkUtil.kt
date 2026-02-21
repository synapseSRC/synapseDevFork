package com.synapse.social.studioasinc.shared.core.util

expect class NetworkUtil {
    fun isNetworkAvailable(): Boolean
    fun registerNetworkCallback(onAvailable: () -> Unit, onLost: () -> Unit)
    fun unregisterNetworkCallback()
}
