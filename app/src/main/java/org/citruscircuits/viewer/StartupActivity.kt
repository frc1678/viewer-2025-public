package org.citruscircuits.viewer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.DataApi
import org.citruscircuits.viewer.data.getDataFromFiles
import org.citruscircuits.viewer.data.loadTestData

/**
 * Splash screen activity that waits for the data to pull from Grosbeak until it
 * begins the other Viewer activities.
 * AKA once MainViewerActivity.databaseReference is not null,
 * it will begin the actual viewer activity to ensure that all data is accessible before the viewer
 * activity begins.
 */

class StartupActivity : ComponentActivity() {

    companion object {
        var databaseReference: DataApi.ViewerData? by mutableStateOf(null)

        //var standStratData = mutableMapOf<String?, StandStratApi.StandStratData?>()
        var standStratUsernames: List<String>? = null
        var popUpVisible = mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Permission is not granted
                // Request permission from the user
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                // Permission is granted
                // Access the downloads folder here
                Constants.DOWNLOADS_FOLDER =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                MainViewerActivity.refreshManager.start(lifecycleScope, this)
                // Gets data
                lifecycleScope.launch(Dispatchers.IO) { getData() }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        100
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // If all permissions are granted, start the refresh coroutine and get data from grosbeak
                Constants.DOWNLOADS_FOLDER =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                MainViewerActivity.refreshManager.start(lifecycleScope, this)

                // Runs getData, sets popup visibility to true if the function returns false
                lifecycleScope.launch(Dispatchers.IO) { getData() }
            }
        }


        setContent {
            // Displays popup if visible is true
            Surface(modifier = Modifier.fillMaxSize()) {
                RetryPopUp(visible = popUpVisible.value, retry = {
                    btnRetryOnClick(it)
                    popUpVisible.value = false
                })
            }
        }
    }

    /** Gets data from the data files */
    private suspend fun getData() {
        try {
            // If using test data
            if (Constants.USE_TEST_DATA) {
                loadTestData(this.resources)

            } else {
                // Gets all datapoints from file if it exists or copies defaults if not
                MainViewerActivity.UserDataPoints.read(this)
                if (MainViewerActivity.UserDataPoints.contents?.get(
                        "default_key"
                    )!!.asString != Constants.DEFAULT_KEY || MainViewerActivity.UserDataPoints.contents?.get(
                        "default_schedule"
                    )!!.asString != Constants.DEFAULT_SCHEDULE
                ) {
                    // Delete file if default key for the file is not updated with that of the code
                    MainViewerActivity.UserDataPoints.file.delete()
                    MainViewerActivity.UserDataPoints.copyDefaults(this)
                }
                Constants.SCHEDULE_KEY =
                    MainViewerActivity.UserDataPoints.contents?.get("schedule")!!.asString
                Constants.EVENT_KEY =
                    MainViewerActivity.UserDataPoints.contents?.get("key")!!.asString
                // Tries to get data from the JSON files when starting the app,  throws an error if fails
                getDataFromFiles()
                if (!MainViewerActivity.MainAppData.mainDataFile.exists()) MainViewerActivity.MainAppData.createFolder()
            }
            ContextCompat.startActivity(this, Intent(this, MainViewerActivity::class.java), null)
        } catch (e: Throwable) {
            Log.e(
                "data",
                "Error fetching data from ${if (Constants.USE_TEST_DATA) "files" else "website"}: ${
                    Log.getStackTraceString(e)
                }"
            )

            popUpVisible.value = true
        }
    }

    /** Function to pull data again with a new event and schedule key
     * @param keys the event and schedule keys
     *
     * */
    private fun btnRetryOnClick(keys: Pair<String, String>) {
        MainViewerActivity.UserDataPoints.contents?.remove("schedule")
        MainViewerActivity.UserDataPoints.contents?.addProperty("schedule", keys.first)
        MainViewerActivity.UserDataPoints.contents?.remove("key")
        MainViewerActivity.UserDataPoints.contents?.addProperty("key", keys.second)
        MainViewerActivity.UserDataPoints.write()
        MainViewerActivity.refreshManager.start(lifecycleScope, this)
        lifecycleScope.launch(Dispatchers.IO) { getData() }
    }

    /** Popup to change event and schedule keys
     * @param visible if the popup is visible
     * @param retry the function that will reload the app with new keys
     * **/
    @Composable
    fun RetryPopUp(visible: Boolean, retry: (Pair<String, String>) -> Unit) {
        // Shows if visible
        if (visible) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    var eventKey by remember { mutableStateOf(Constants.EVENT_KEY) }
                    var scheduleKey by remember { mutableStateOf(Constants.SCHEDULE_KEY) }

                    var isLinked by remember { mutableStateOf(true) }
                    Text("Please Reenter Valid Keys", modifier = Modifier.padding(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = eventKey,
                                onValueChange = {
                                    eventKey = it
                                    if (isLinked) scheduleKey = it // Sync when linked
                                },
                                label = { Text("Event Key") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = scheduleKey,
                                onValueChange = {
                                    scheduleKey = it
                                    if (isLinked) eventKey = it // Sync when linked
                                },
                                label = { Text("Schedule Key") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Link Button
                        Image(
                            painter = painterResource(id = if (isLinked) R.drawable.linklines else R.drawable.notlinklines),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(start = 8.dp)
                                .clickable { isLinked = !isLinked }
                        )
                    }

                    // Retry button
                    IconButton(
                        onClick = { retry(Pair(eventKey.trim(), scheduleKey.trim())) },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = null)
                    }
                }
            }
        }
    }
}
