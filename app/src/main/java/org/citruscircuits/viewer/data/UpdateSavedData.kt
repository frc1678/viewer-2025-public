package org.citruscircuits.viewer.data

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.eventKey
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.getStandStratUsernames
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.mainDataFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.matchScheduleFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.standStratDataFolder
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.standStratUsernamesFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.teamListFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.writeStringDataToJson
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.createLeaderboard
import java.io.File

/** Updates all of the app's saved data files */
suspend fun updateSavedData() {
    Log.d("data-refresh", "updating data")
    // If a file is missing, creates a blank version of that file
    MainViewerActivity.MainAppData.create()
    // Updates match schedule
    val newMatchSchedule = DataApi.getMatchSchedule(Constants.EVENT_KEY)
    writeStringDataToJson(matchScheduleFile, Json.encodeToString(newMatchSchedule))
    Log.d("data-refresh", "updated match schedule")
    // Updates team list
    val newTeamList = DataApi.getTeamList(Constants.EVENT_KEY).map { it }
    writeStringDataToJson(teamListFile, Json.encodeToString(newTeamList))
    Log.d("data-refresh", "updated team list")
    // Updates main data
    val newViewerData = DataApi.getViewerData(Constants.EVENT_KEY)
    writeStringDataToJson(mainDataFile, Json.encodeToString(newViewerData))
    Log.d("data-refresh", "updated main data")
    // Reads the main app data and sets that data to the app's main database
    StartupActivity.databaseReference = MainViewerActivity.MainAppData.readViewerDataFromJson()
    // Refresh rankings
    (Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS + Constants.FIELDS_TO_BE_DISPLAYED_LFM).forEach {
        if (it !in Constants.CATEGORY_NAMES) createLeaderboard(it)
    }
    // Pull Pit Collection images
    PitImagesApi.fetchImages(eventKey)
    Log.d("data-refresh", "updated pit images")
}
