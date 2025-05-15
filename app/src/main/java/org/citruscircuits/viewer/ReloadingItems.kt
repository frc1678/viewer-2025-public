package org.citruscircuits.viewer

import androidx.compose.runtime.mutableStateOf

/** Object with all the functions that set the values related to refreshing*/
object ReloadingItems {
    var showPopup = mutableStateOf(false)
    var isLoading = mutableStateOf(false)
    var stillLoading = mutableStateOf(false)
    var finished = mutableStateOf(false)

    /**Used when the refresh button is first clicked*/
    fun showLoading() {
        showPopup.value = true
        isLoading.value = true
        stillLoading.value = false
        finished.value = false
    }

    /**Used to "disable" the popup*/
    fun hideLoading() {
        showPopup.value = false
    }

    /**Used to mark that the refreshing is complete*/
    fun loadingComplete() {
        isLoading.value = false
        finished.value = true
    }

    /**Used to display that the app is still loading when the refresh button
     * is clicked a second time*/
    fun stillLoading() {
        stillLoading.value = true
    }
}
