package org.citruscircuits.viewer.fragments.user_preferences

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import java.util.Locale

@Composable
fun UserPreferencesPage(
    datapointsDisplayed: List<String>,
    chosenDatapoints: SnapshotStateList<String>,
    onDatapointsChanged: (List<String>) -> Unit,
    userName: String
) {

    // Local context for accessing resources
    val context = LocalContext.current

    // Compute if all selectable datapoints are currently selected
    val allSelected = datapointsDisplayed.filter { it !in Constants.CATEGORY_NAMES }
        .all { it in chosenDatapoints }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header and Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Text(
                text = when (userName) {
                    "OTHER" -> "User's Datapoints"
                    "UNIVERSAL" -> "User's Datapoints"
                    else -> userName.lowercase(Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + "'s Datapoints"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            )

            // Reset to Default Button
            Button(
                onClick = {
                    Log.d(
                        "UserPreferences",
                        "UserDataPoints.contents: ${MainViewerActivity.UserDataPoints.contents}"
                    )


                    var selectedUser =
                        MainViewerActivity.UserDataPoints.contents?.get("selected")?.asString
                            ?: "OTHER"
                    selectedUser = selectedUser.uppercase()
                    Log.d("UserPreferences", selectedUser)
                    // Load default preferences from resources
                    val defaultPreferences =
                        context.resources.openRawResource(R.raw.default_prefs).use { inputStream ->
                            JsonParser.parseReader(inputStream.reader()).asJsonObject.get(
                                selectedUser
                            )?.asJsonArray
                                ?: JsonArray()
                        }
                    Log.d("UserPreferences", defaultPreferences.toString())

                    // Update preferences
                    chosenDatapoints.clear()
                    chosenDatapoints.addAll(defaultPreferences.map { it.asString }
                        .filter { it in datapointsDisplayed })
                    onDatapointsChanged(chosenDatapoints.toList())

                    // Update JSON data
                    MainViewerActivity.UserDataPoints.contents?.remove(selectedUser)
                    MainViewerActivity.UserDataPoints.contents?.add(
                        selectedUser,
                        JsonArray().apply {
                            chosenDatapoints.forEach { add(it) }
                        })
                    MainViewerActivity.UserDataPoints.write()
                }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF009C8A),
                    contentColor = Color.White
                )
            ) {
                Text("Reset to Default")
            }

            // Select All / Deselect All Button
            Button(
                onClick = {
                    if (allSelected) {
                        // Deselect all
                        chosenDatapoints.clear()
                    } else {
                        // Select all
                        chosenDatapoints.clear()
                        chosenDatapoints.addAll(datapointsDisplayed.filter { it !in Constants.CATEGORY_NAMES })
                    }
                    onDatapointsChanged(chosenDatapoints)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF009C8A),
                    contentColor = Color.White
                )
            ) {
                Text(text = if (allSelected) "Deselect All" else "Select All")
            }


        }

        // Datapoints List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            datapointsDisplayed.forEachIndexed { index, datapoint ->
                if (datapoint in Constants.CATEGORY_NAMES) {
                    // Render category headers
                    Text(
                        text = Translations.ACTUAL_TO_HUMAN_READABLE[datapoint] ?: datapoint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when (datapoint) {
                                    "TEAM", "TIM" -> Color.Gray
                                    else -> Color.LightGray
                                }
                            )
                            .padding(10.dp),
                        fontSize = 25.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Render individual datapoints
                    val isSelected = datapoint in chosenDatapoints
                    val isTIM = datapointsDisplayed.indexOf("TIM") <= index
                    Text(
                        text = Translations.ACTUAL_TO_HUMAN_READABLE[datapoint] ?: datapoint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) {
                                    if (isTIM) Color(0xFF587CFF) else Color(0xFF27AE60)
                                } else Color.White
                            )
                            .clickable {
                                if (isSelected) {
                                    chosenDatapoints.remove(datapoint)
                                } else {
                                    chosenDatapoints.add(datapoint)
                                }
                                onDatapointsChanged(chosenDatapoints)
                            }
                            .padding(8.dp),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
