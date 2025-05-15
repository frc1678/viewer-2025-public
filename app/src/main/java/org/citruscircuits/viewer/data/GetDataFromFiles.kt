package org.citruscircuits.viewer.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.eventKey
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.getStandStratUsernames
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.isValid
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.mainDataFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.matchScheduleFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.standStratDataFolder
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.standStratUsernamesFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.teamListFile
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.writeStringDataToJson
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import java.io.File

/** Pulls data from saved JSON files.
 * If those files don't exists, creates them with data pulled from Kestrel. */
suspend fun getDataFromFiles() {
    // Creates the main data folder
    MainViewerActivity.MainAppData.create()
    // Creates the stand strat data folder, as well as all the files within it
    /* Checks if the match schedule file exists and is not empty.
    If it's not, pulls the match schedule from Kestrel and creates the file */
    if (!isValid(matchScheduleFile)) {
        if (DataApi.getMatchSchedule(Constants.EVENT_KEY).toString() == "" ||
            DataApi.getMatchSchedule(Constants.EVENT_KEY).isEmpty()
        ) {
            DataApi.getMatchSchedule(Constants.EVENT_KEY)
        } else {
            val newMatchSchedule = DataApi.getMatchSchedule(Constants.EVENT_KEY)
            writeStringDataToJson(matchScheduleFile, Json.encodeToString(newMatchSchedule))
        }
    }
    /* Checks if the team list file exists. If it doesn't, pulls the team list from
    Kestrel and creates the file */
    if (!isValid(teamListFile)) {
        val newTeamList = DataApi.getTeamList(Constants.EVENT_KEY)
        writeStringDataToJson(teamListFile, Json.encodeToString(newTeamList))
    }
    /* Checks if the main data file exists. If it doesn't, pulls the data from
    Kestrel and creates the file */
    if (!isValid(mainDataFile)) {
        val newData = DataApi.getViewerData(Constants.EVENT_KEY)
        writeStringDataToJson(mainDataFile, Json.encodeToString(newData))
    }

    // Reads the main app data and sets that data to the app's main database
    StartupActivity.databaseReference = MainViewerActivity.MainAppData.readViewerDataFromJson()
    // Reads the match schedule file
    MainViewerActivity.MainAppData.getMatchScheduleFromJson()
    // Reads the team list file
    MainViewerActivity.teamList = MainViewerActivity.MainAppData.readTeamListJson()
}
