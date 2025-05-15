package org.citruscircuits.viewer.fragments.alliance_details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.DataApi
import org.citruscircuits.viewer.data.getTeamDataValue

@Composable
fun AllianceDetailsPage(
    allianceDetails: List<DataApi.ElimAlliance>,
    allianceNum: Int,
    teams: List<String>,
    onChangeAlliance: (Int) -> Unit,
    datapoints: List<String>,
    onOpenRankings: () -> Unit,
    onOpenDatapointRankings: (String) -> Unit,
    onOpenAutoPath: (String) -> Unit,
    onOpenTeamDetails: (String) -> Unit,
    onOpenGraphs: (String, String) -> Unit,
    targetDatapoint: String?,
) {
    Surface {

        Column(modifier = Modifier.fillMaxSize()) {
            // Populate header essentials and teams
            Column {
                AllianceNumber(
                    allianceNum = allianceNum,
                    elimAlliances = allianceDetails,
                    onChangeAlliance = onChangeAlliance
                )
                Teams(teams, onOpenTeamDetails)
            }
            //Scrolling to datapoint from search
            val listState = rememberLazyListState()

            LaunchedEffect(targetDatapoint) {
                targetDatapoint?.let {
                    val index = datapoints.indexOf(it)
                    if (index != -1) listState.animateScrollToItem(index)
                }
            }

            LazyColumn(state = listState) {

                itemsIndexed(datapoints) { index, datapoint ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (index != 0) {
                            HorizontalDivider()
                        }
                        if (datapoint in Constants.CATEGORY_NAMES) {
                            Category(datapoint)
                        } else {
                            AllianceDetails(
                                datapoint = datapoint,
                                teams = teams,
                                onOpenRankings = onOpenRankings,
                                onOpenDatapointRankings = onOpenDatapointRankings,
                                onOpenAutoPath = onOpenAutoPath,
                                onOpenGraphs = onOpenGraphs
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Top bar containing essential information: score, RP, and win chance
 * @param allianceNum alliance number*/
@Composable
fun AllianceNumber(
    allianceNum: Int,
    elimAlliances: List<DataApi.ElimAlliance>,
    onChangeAlliance: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // Alliance Number
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Text(
                "Alliance $allianceNum",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Drop-down Arrow"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            elimAlliances.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Alliance ${it.allianceNum} Teams: ${
                                it.picks.toString().replace("]", "").replace("[", "")
                            }"
                        )
                    },
                    onClick = { onChangeAlliance(it.allianceNum) })
            }
        }
    }
}

/**Displays teams, winning teams are bolded and underlined */
@Composable
fun Teams(teams: List<String>, onOpenTeamDetails: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {

        Text(
            "Team",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.5F)
                .align(Alignment.CenterVertically)
                .padding(4.dp),
            fontSize = 15.sp
        )
        teams.forEach {
            Text(
                it,
                color = colorResource(id = R.color.Blue),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1F)
                    .align(Alignment.CenterVertically)
                    .padding(4.dp)
                    .clickable { onOpenTeamDetails(it) },
                fontSize = 12.sp
            )
        }
    }
}

/** Categories, e.g. Auto, Tele, etc. */
@Composable
fun Category(name: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), colors = CardDefaults.cardColors(
            containerColor = Color.LightGray
        )
    ) {
        Text(
            name,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}


/** Row for each datapoint
 * @param datapoint the datapoint name*/
@Composable
fun AllianceDetails(
    datapoint: String,
    teams: List<String>,
    onOpenRankings: () -> Unit,
    onOpenDatapointRankings: (String) -> Unit,
    onOpenAutoPath: (String) -> Unit,
    onOpenGraphs: (String, String) -> Unit
) {
    /** List of pop-up statuses for notes datapoints for every team */
    var displayPopUps = remember { mutableStateListOf(false, false, false, false, false, false) }
    val isNote = Constants.STAND_STRAT_NOTES_DATA.contains(datapoint)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(4.dp)
    ) {
        // Datapoint name
        Text(
            Translations.ACTUAL_TO_HUMAN_READABLE[datapoint] ?: "",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .weight(1.5F)
                .clickable {
                    if (datapoint == "current_avg_rps_tim" || datapoint == "current_avg_rps") {
                        // Opens the Rankings page
                        onOpenRankings()
                    } else if (Constants.TIM_TO_TEAM[datapoint] in Constants.RANKABLE_FIELDS || datapoint in Constants.RANKABLE_FIELDS) {
                        // Opens the rankings list for the corresponding team datapoint
                        onOpenDatapointRankings(datapoint)
                    }
                }
                .padding(4.dp),
            fontSize = 15.sp
        )

        // Blue alliance
        teams.forEachIndexed { index, it ->
            VerticalDivider()
            if (isNote) {
                IconButton(
                    onClick = { displayPopUps[index] = true },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = colorResource(R.color.Blue)),
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                        .padding(2.dp)
                ) {
                    Icon(
                        Icons.Filled.RemoveRedEye,
                        "view notes"
                    )
                }
            } else {
                getDataValue(it, datapoint)?.let { it1 ->
                    Text(
                        it1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(id = R.color.Blue),
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.CenterVertically)
                            .clickable {

                                if (datapoint in Constants.AUTO_TIMS_DATA || datapoint in Constants.AUTO_TEAMS_DATA) onOpenAutoPath(
                                    it
                                ) else {
                                    if (datapoint in Constants.GRAPHABLE.keys) {
                                        onOpenGraphs(

                                            it,
                                            Constants.TIM_TO_TEAM.entries.find { entry -> entry.value == datapoint }?.key
                                                ?: datapoint
                                        )

                                    } else if (datapoint in Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED && datapoint !in Constants.STAND_STRAT_DATA_TIMS) {
                                        onOpenGraphs(it, datapoint)
                                    }

                                }
                            }
                            .padding(4.dp),
                        fontSize = 12.sp
                    )
                }
            }

            // Display notes datapoints in a pop-up
            if (displayPopUps[index]) {
                TeamNotesDialog(
                    onDismiss = { displayPopUps[index] = false },
                    teamNumber = it,
                    datapoint = datapoint,
                    isRedAlliance = false
                )
            }
        }
    }
}

/** Function to generate the TIM notes dialog pop-up */
@Composable
fun TeamNotesDialog(
    onDismiss: () -> Unit,
    teamNumber: String,
    datapoint: String,
    isRedAlliance: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.width(250.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Text(
                    "${Translations.ACTUAL_TO_HUMAN_READABLE[datapoint] ?: ""} for $teamNumber",
                    textAlign = TextAlign.Center,
                    color = colorResource(id = if (isRedAlliance) R.color.Red else R.color.Blue),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                getDataValue(teamNumber, datapoint)?.let {
                    Text(
                        it,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Close",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


/** Function to return the string value of a given team in that match
 * @param teamNumber team number
 * @param datapoint the datapoint name
 * @param matchNumber the match number
 * @return the proper display value accounting for exceptions*/
fun getDataValue(
    teamNumber: String,
    datapoint: String,
): String? {
    when (datapoint) {
        // Exceptions for defense ratings, which can only range from 0 - 5, -1 means no defense in that match

        "avg_defense_rating" -> {
            val value =
                formatTeamDataValue(teamNumber, "avg_defense_rating")
            return if (value == "-1.0") "N/A" else value
        }
        // if displaying a team datapoint
        else -> {
            return formatTeamDataValue(teamNumber, datapoint)
        }

    }
}

/** Gets the value of a given team's datapoint
 * @param teamNumber the team number
 * @param field the datapoint name*/
private fun formatTeamDataValue(teamNumber: String, field: String): String {
    // If the datafield is a float, round the datapoint.
    // Otherwise, get returned string from getTeamDataValue.
    val regex = Regex("-?\\d+${Regex.escape(".")}\\d+")
    val dataValue = getTeamDataValue(teamNumber, field)
    return if (regex matches dataValue) {
        if (field in Constants.DRIVER_DATA) "%.2f".format(dataValue.toFloat())
        else "%.1f".format(dataValue.toFloat())
    } else getTeamDataValue(teamNumber, field)
}