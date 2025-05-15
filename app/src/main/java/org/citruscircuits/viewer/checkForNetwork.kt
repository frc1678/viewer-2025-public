package org.citruscircuits.viewer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Checks if the device is connected to the internet.
 *  @return True if the device is connected to Wi-Fi, false otherwise.*/
fun Context.isNetworkAvailable(): Boolean {
    // Gets the ConnectivityManager system service
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Gets the active network. If there is no active network, returns false
    val network = connectivityManager.activeNetwork ?: return false

    // Gets the network capabilities of the active network. If there are no capabilities, returns false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    // Checks if the network has internet capability
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.isWifiConnected(): Boolean {
    // Gets the ConnectivityManager system service
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Gets the active network. If there is no active network, returns false
    val network = connectivityManager.activeNetwork ?: return false
    // Gets the network capabilities of the active network. If there are no capabilities, returns false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    // Checks if the network has Wi-Fi transport capability
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}