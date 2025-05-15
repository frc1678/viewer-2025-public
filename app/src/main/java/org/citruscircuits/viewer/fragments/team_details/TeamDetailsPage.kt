package org.citruscircuits.viewer.fragments.team_details

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.robotImagesFolder
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.data.getTeamName
import org.citruscircuits.viewer.getRankingList
import org.citruscircuits.viewer.getRankingTeam
import java.io.File
import java.util.regex.Pattern
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * Generates weight of data bars for given field and team number
 */
class DataBarValues(
    var datapoint: String = "avg_total_points",
    var teamNumber: String = "1678"
) {
    private var dataBarWeight: Float = 0f
    private var dataBarIsNormal: Boolean = false
    val teamDataBarNormal: Float
    val teamDataBarReverse: Float
    val teamDataBarNormalVisible: Boolean
    val teamDataBarReverseVisible: Boolean

    /** Weight (0 if invisible) of normal data bar*/
    private var dataBarNormal: Float? = 0f

    /** Weight (0 if invisible) of reverse data bar*/
    private var dataBarReverse: Float? = 0f

    init {
        val rankingList = getRankingList(datapoint)
        if (rankingList.isNotEmpty() && (rankingList.first().value
                ?: Constants.NULL_CHARACTER) != Constants.NULL_CHARACTER
        ) {
            val teamDataValue = getTeamDataValue(teamNumber, datapoint).toFloatOrNull() ?: 0f
            dataBarNormal =
                teamDataValue / (getRankingList(datapoint).first().value!!.toFloatOrNull() ?: 0f)
            dataBarReverse =
                teamDataValue / (getRankingList(datapoint).last().value!!.toFloatOrNull() ?: 0f)
            //only reverse data bar should be should for incap, foul, fail, cycle_time, and broken mechanism datapoints
            if ("incap" in datapoint || "foul" in datapoint || "fail" in datapoint || "cycle_time" in datapoint || "matches_with_broken_mechanism" in datapoint) {
                this.dataBarWeight = dataBarReverse as Float
                //otherwise, show normal data bar if applicable
            } else if (datapoint in Constants.FIELDS_WITH_DATA_BARS) {
                this.dataBarIsNormal = true
                this.dataBarWeight = dataBarNormal as Float
            }
        }
        when (this.dataBarIsNormal) {
            true -> {
                //data bar normal + data bar reverse should equal 1
                teamDataBarNormal = this.dataBarWeight
                teamDataBarReverse = 1 - this.dataBarWeight
                teamDataBarNormalVisible = true
                teamDataBarReverseVisible = false
            }

            else -> {
                teamDataBarNormal = 1 - this.dataBarWeight
                //1.01f is to prevent this function from returning a weight of zero
                teamDataBarReverse = 1.01f - teamDataBarNormal
                teamDataBarNormalVisible = false
                teamDataBarReverseVisible = this.dataBarWeight > 0
            }
        }
    }
}

/** this function gathers the current user and adds their current datapoints, if applicable, to the output
 * @param isLFMMode whether to display data from the team's last four matches played*/
fun generateDataPointsDisplayed(
    isLFMMode: Boolean
): List<String> {
    val currentUser = MainViewerActivity.UserDataPoints.contents?.get("selected")?.asString
    val currentUserChosenDatapoints =
        MainViewerActivity.UserDataPoints.contents?.get(currentUser)?.asJsonArray
    val dataPoints = mutableListOf<String>()
    if (currentUserChosenDatapoints != null) {
        for (chosenDataPoint in currentUserChosenDatapoints) {
            if (Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(chosenDataPoint.asString)) {
                val datapointLFM = "lfm_" + chosenDataPoint.asString
                if (isLFMMode) {
                    if (Constants.FIELDS_TO_BE_DISPLAYED_LFM.contains(datapointLFM)) {
                        dataPoints.add(datapointLFM)
                    } else {
                        dataPoints.add(chosenDataPoint.asString)
                    }
                } else {
                    dataPoints.add(chosenDataPoint.asString)
                }
            }
        }
    }
    return dataPoints
}

/**
 * Main page for team details
 * @param robotPicNav Navigates to [RobotPicFragment] for team [teamNumber]
 * @param autoPathNav Navigates to [AutoPathsFragment] for team [teamNumber]
 * @param matchListNav Navigates to matches sorted for team [teamNumber]
 * @param notesNav Navigates to notes page for team [teamNumber]
 * @param pickabilityNav Navigates to pickability page
 * @param graphsNav Navigates to [GraphsFragment] for team [teamNumber]
 * */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TeamDetailsPage(
    teamNumber: String,
    targetDatapoint: String? = null,
    isLFMMode: Boolean,
    setLFM: () -> Unit,
    robotPicNav: () -> Unit,
    autoPathNav: () -> Unit,
    matchListNav: () -> Unit,
    notesNav: () -> Unit,
    pickabilityNav: (String, String) -> Unit,
    graphsNav: (String?) -> Unit,
    datapointRankingNav: (String) -> Unit,
    rankingNav: () -> Unit
) {
    val teamName = getTeamName(teamNumber)
    val dataPointsDisplayed by remember(isLFMMode) {
        mutableStateOf(
            generateDataPointsDisplayed(
                isLFMMode
            )
        )
    }
    val teamHasPicsInPhone =
        File(robotImagesFolder, "${teamNumber}_full_robot.jpg").exists() ||
                File(robotImagesFolder, "${teamNumber}_side.jpg").exists()
    Scaffold(modifier = Modifier.padding(5.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top section containing basic team info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f)
                    .padding(3.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // L4M mode switch, team number, pictures button (if found)
                    Row(
                        modifier = Modifier.weight(1.5f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // L4M mode switch
                        Box(modifier = Modifier
                            .background(color = Color(0xFF008577))
                            .clickable { setLFM() }
                            .weight(1f)
                            .fillMaxHeight()) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = if (isLFMMode) "TO ALL MATCHES" else "TO L4M",
                                color = Color.White,
                                fontSize = if (isLFMMode) 18.sp else 22.sp,
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                        // Team number
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            // Properly centers text
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = teamNumber,
                                fontSize = 40.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        // Robot pic navigation, invisible when no pics are found
                        Box(modifier = Modifier
                            .background(color = if (teamHasPicsInPhone) Color(0xFF008577) else Color.White)
                            .clickable {
                                if (teamHasPicsInPhone) {
                                    robotPicNav()
                                }
                            }
                            .weight(if (teamHasPicsInPhone) 1f else 0.001f)
                            .fillMaxHeight()) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = if (teamHasPicsInPhone) "PICTURE" else "",
                                color = Color.White,
                                fontSize = when (teamHasPicsInPhone) {
                                    true -> 20.sp
                                    else -> 0.sp
                                },
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Team name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (teamName != null) {
                                Text(
                                    fontWeight = FontWeight.Bold,
                                    text = teamName,
                                    color = Color.DarkGray,
                                    fontSize = 20.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                    // Auto paths button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = Color(0xFF008577))
                            .clickable { autoPathNav() }) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = "AUTO PATHS",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            HorizontalDivider(thickness = 5.dp, color = Color.Black)
            // List of team details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
            ) {
                //Scrolling to datapoint from search
                val listState = rememberLazyListState()
                LaunchedEffect(targetDatapoint, dataPointsDisplayed) {
                    if (targetDatapoint != null) {
                        val index = dataPointsDisplayed.indexOf(targetDatapoint)
                        if (index >= 0) {
                            listState.scrollToItem(index)
                        }
                    }
                }
                LazyColumn(state = listState) {
                    //generate one item for each datapoint
                    items(count = dataPointsDisplayed.size) { index ->
                        val datapointForItemName by remember(dataPointsDisplayed) {
                            mutableStateOf(
                                dataPointsDisplayed[index]
                            )
                        }


                        //only try to load data cards if the page has fully loaded, to avoid a crash
                        // Handling mode switching logic outside render loop
                        if (isLFMMode) getLFMEquivalent(dataPointsDisplayed)
                        TeamDetailsDataPointRow(
                            teamNumber = teamNumber,
                            index = index,
                            field = datapointForItemName,
                            isLFMMode = isLFMMode,
                            datapointRankingNav = { field -> datapointRankingNav(field) },
                            pickabilityNav = { pickabilityNav(teamNumber, datapointForItemName) },
                        ) {
                            when {
                                "pickability" in (datapointForItemName) -> {
                                    pickabilityNav(teamNumber, datapointForItemName)
                                }

                                datapointForItemName in Constants.RANKING_DATA -> {
                                    rankingNav()
                                }

                                datapointForItemName == "See Matches" -> {
                                    matchListNav()
                                }

                                datapointForItemName == "Notes" -> {
                                    notesNav()
                                }

                                datapointForItemName in Constants.GRAPHABLE -> {
                                    graphsNav(Constants.GRAPHABLE[datapointForItemName])
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

/**
 * An individual row in team details.
 *
 * Certain data points names require separate rendering, such as [Constants.CATEGORY_NAMES].
 * @param index Number row that this item is at
 * */
@Composable
private fun TeamDetailsDataPointRow(
    teamNumber: String,
    index: Int,
    field: String,
    isLFMMode: Boolean,
    datapointRankingNav: (String) -> Unit,
    pickabilityNav: () -> Unit,
    onClick: () -> Unit
) {
    //gets the current team's notes if available, otherwise returns ""
    fun getTeamNotes(teamNumber: String): String? {
        return if (MainViewerActivity.notesCache.containsKey(teamNumber)) {
            MainViewerActivity.notesCache[teamNumber]
        } else {
            ""
        }
    }

    val regex: Pattern = Pattern.compile("-?" + "[0-9]+" + Regex.escape(".") + "[0-9]+")
    //properly formats the data value for the given datapoint and team

    fun formatTeamDataValue(teamDataValue: String): String {
        if (teamDataValue == "?") return teamDataValue

        val isPercentData = Constants.PERCENT_DATA.contains(field)
        return when {
            regex.matcher(teamDataValue)
                .matches() -> "%.1f".format(teamDataValue.toFloat()) + if (isPercentData) {
                "%"
            } else {
                ""
            }

            isPercentData -> "$teamDataValue%"
            else -> teamDataValue
        }
    }

    val teamDataValue = remember(field) {
        formatTeamDataValue(getTeamDataValue(teamNumber, field))
    }

    //for "see matches"
    if (index == 0) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick() }
            .background(Color.LightGray)) {
            Text(
                text = "See Matches →",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else if (index == 1) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .background(Color(0xFFFF9900))
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                text = "Notes (click below to edit)",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // orange notes box
    } else if (index == 2) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .background(Color(0xFFFF9900))
            .clickable { onClick() }) {
            Text(
                fontWeight = FontWeight.Bold,
                text = getTeamNotes(teamNumber)!!,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        //large grey category name boxes
    } else if (field in Constants.CATEGORY_NAMES) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.LightGray)
        ) {
            Text(
                fontWeight = FontWeight.Bold,
                text = "${if (isLFMMode) "LFM" else ""} $field",
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        TeamDataPointCard(
            datapoint = field,
            teamNumber = teamNumber,
            isLFMMode = isLFMMode,
            teamDataValue = teamDataValue,
            onClick = { if (field in Constants.GRAPHABLE || "pickability" in field || field in Constants.RANKING_DATA) onClick() },
            onLongClick = {
                // Use if/else to determine long click action:
                if (field in Constants.FIELDS_TO_BE_DISPLAYED_RANKING || field in Constants.RANKABLE_FIELDS) {
                    datapointRankingNav(field)
                } else if (field.contains("pickability")) {
                    pickabilityNav()
                }
            })
    }
    HorizontalDivider(thickness = 2.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamDataPointCard(
    datapoint: String,
    teamNumber: String,
    isLFMMode: Boolean,
    teamDataValue: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    //easy adjustment of text formatting
    val textSizeForColumn = 20
    //computing the sizes of data bars
    val teamDataBarValues = DataBarValues(datapoint, teamNumber)
    val teamDataBarNormal: Float = teamDataBarValues.teamDataBarNormal
    val teamDataBarReverse: Float = teamDataBarValues.teamDataBarReverse
    val teamDataBarNormalVisible: Boolean = teamDataBarValues.teamDataBarNormalVisible
    val teamDataBarReverseVisible: Boolean = teamDataBarValues.teamDataBarReverseVisible
    //generates the field names for lfm mode
    val fieldName = if (isLFMMode) {
        "L4M " + (Translations.ACTUAL_TO_HUMAN_READABLE[datapoint.removePrefix("lfm_")]
            ?: datapoint)
    } else {
        Translations.ACTUAL_TO_HUMAN_READABLE[datapoint.removePrefix("lfm_")] ?: datapoint
    }

    var isLongString = false
    if (Constants.STAND_STRAT_NOTES_DATA.contains(datapoint) || (Constants.MATT_DATAPOINTS_TEAM.contains(
            datapoint
        ) && teamDataValue.length > 5)
    ) {
        isLongString = true
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongClick() }),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            //normal data bar
            Box(
                modifier = Modifier
                    .weight(
                        if (teamDataBarNormal > 0) {
                            teamDataBarNormal
                        } else {
                            0.01f
                        }
                    )
                    .fillMaxHeight()
                    .background(
                        Color(
                            if (teamDataBarNormalVisible) {
                                0xFFFFCFAF
                            } else {
                                0x00FFFFFF
                            }
                        )
                    )
            )
            //reverse data bar
            Box(
                modifier = Modifier
                    .weight(
                        if (teamDataBarReverse > 0) {
                            teamDataBarReverse
                        } else {
                            0.01f
                        }
                    )
                    .fillMaxHeight()
                    .background(
                        Color(
                            if (teamDataBarReverseVisible) {
                                0xFFFFAFAF
                            } else {
                                0x00FFFFFF
                            }
                        )
                    )
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .weight(7.5f)
                    .fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    //team rank for data point
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(40.dp)
                    ) {
                        var rankText = ""
                        if (datapoint in Constants.RANKABLE_FIELDS) {
                            rankText =
                                if (datapoint in Constants.PIT_DATA || "mode_start_position" in datapoint || (datapoint in Constants.STAND_STRAT_DATA_TEAMS && "rating" !in datapoint) || "compatible_auto" in datapoint) ""
                                else getRankingTeam(
                                    teamNumber,
                                    datapoint
                                )?.placement?.toString()
                                    ?: Constants.NULL_CHARACTER
                        }
                        Text(
                            fontWeight = FontWeight.Bold,
                            text = rankText,
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = textSizeForColumn.sp
                        )
                    }
                    //data point name
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(200.dp)
                    ) {
                        Text(
                            fontWeight = FontWeight.Bold,
                            text = fieldName,
                            fontSize = textSizeForColumn.sp,
                            textAlign = TextAlign.Left
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!isLongString) {
                        //value of data point
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(80.dp)
                        ) {
                            Text(
                                fontWeight = FontWeight.Bold,
                                text = teamDataValue,
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = textSizeForColumn.sp
                            )
                        }
                    }
                }

            }
            if (isLongString) {
                Spacer(modifier = Modifier.weight(1f))
                //value of data point
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = teamDataValue,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = textSizeForColumn.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

//generates the internal data point names in L4M mode for a large list of all matches mode data points
fun getLFMEquivalent(datapointList: List<String>): List<String> {
    val lfmDataPoints: MutableList<String> = mutableListOf()
    for (datapoint in datapointList) {
        if (
            !Constants.TEAM_DATA_POINTS_NOT_IN_LFM.contains(datapoint) &&
            !datapoint.contains("sd_") &&
            !Constants.CATEGORY_NAMES.contains(datapoint)
        ) {
            if (
                !Constants.CATEGORY_NAMES.contains(datapoint) &&
                !Constants.TEAM_AND_LFM_SHARED_DATA_POINTS.contains(datapoint)
            ) {
                lfmDataPoints.add("lfm_$datapoint")
            } else if (Constants.TEAM_AND_LFM_SHARED_DATA_POINTS.contains(datapoint)) {
                lfmDataPoints.add(datapoint)
            } else {
                lfmDataPoints.add(Translations.TEAM_TO_LFM_HEADERS[datapoint] ?: datapoint)
            }
        }
    }
    return lfmDataPoints
}