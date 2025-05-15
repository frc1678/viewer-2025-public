package org.citruscircuits.viewer.fragments.match_details

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
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getAllianceInMatchObjectByKey
import org.citruscircuits.viewer.data.getTIMDataValueByMatch
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.fragments.match_schedule.Match

@Composable
fun MatchDetailsPage(
    matchNumber: Int,
    match: Match,
    datapoints: List<String>,
    hasTBAData: Boolean,
    onOpenRankings: () -> Unit,
    onOpenDatapointRankings: (String) -> Unit,
    onOpenAutoPath: (String) -> Unit,
    onOpenTeamDetails: (String) -> Unit,
    onOpenGraphs: (String, String) -> Unit,
    targetDatapoint: String? = null
) {
    Surface {
        val essentialsList =
            if (hasTBAData) Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_HEADER_PLAYED
            else Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_HEADER_NOT_PLAYED

        Column(modifier = Modifier.fillMaxSize()) {
            // Populate header essentials and teams
            Column {
                Essentials(matchNumber.toString(), essentialsList, hasTBAData)
                HorizontalDivider(thickness = 4.dp, color = Color.Black)
                Teams(match, hasTBAData, onOpenTeamDetails)
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
                            MatchDetail(
                                datapoint = datapoint,
                                matchNumber = matchNumber,
                                match = match,
                                onOpenRankings = onOpenRankings,
                                onOpenDatapointRankings = onOpenDatapointRankings,
                                onOpenAutoPath = onOpenAutoPath,
                                onOpenGraphs = { teamNumber, datapoint ->
                                    onOpenGraphs(
                                        teamNumber,
                                        datapoint
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**Displays teams, winning teams are bolded and underlined */
@Composable
fun Teams(match: Match, hasTBAData: Boolean, onOpenTeamDetails: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // get red's match score, actual or predicted
        val redScore = getAllianceInMatchObjectByKey(
            Constants.RED,
            match.matchNumber,
            if (hasTBAData) "actual_score" else "predicted_score"
        )?.toFloatOrNull()
        // get blue's match score, actual or predicted
        val blueScore = getAllianceInMatchObjectByKey(
            Constants.BLUE,
            match.matchNumber,
            if (hasTBAData) "actual_score" else "predicted_score"
        )?.toFloatOrNull()
        // check whether red/blue won (both should be false if tied)
        val redWon = hasTBAData && (redScore ?: 0f) > (blueScore ?: 0f)
        val blueWon = hasTBAData && (blueScore ?: 0f) > (redScore ?: 0f)
        Text(
            "Team",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.5F)
                .align(Alignment.CenterVertically)
                .padding(4.dp),
            fontSize = 15.sp
        )
        match.blueTeams.forEach {
            Text(
                it,
                color = colorResource(id = R.color.Blue),
                fontWeight = if (blueWon) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (blueWon) TextDecoration.Underline else TextDecoration.None,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1F)
                    .align(Alignment.CenterVertically)
                    .padding(4.dp)
                    .clickable { onOpenTeamDetails(it) },
                fontSize = 12.sp
            )
        }
        match.redTeams.forEach {
            Text(
                it,
                color = colorResource(id = R.color.Red),
                fontWeight = if (redWon) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (redWon) TextDecoration.Underline else TextDecoration.None,
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

/** Top bar containing essential information: score, RP, and win chance
 * @param matchNumber match number
 * @param datapoints the four essentials
 * @param hasTBAData if there is TBA data*/
@Composable
fun Essentials(matchNumber: String, datapoints: List<String>, hasTBAData: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Blue Alliance
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1F)
        ) {
            // Loop through each header item
            for (datapoint in datapoints) {
                // Get the value
                val value = getAllianceInMatchObjectByKey(
                    Constants.BLUE, matchNumber, datapoint
                )
                // Header
                Translations.ACTUAL_TO_HUMAN_READABLE[datapoint]?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(id = R.color.Blue),
                        fontWeight = FontWeight.Bold
                    )
                }
                // Header value text with proper formatting
                Text(
                    getHeaderValue(datapoint, value, hasTBAData),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.Blue)
                )
            }
        }
        // Match Number
        Box(
            modifier = Modifier
                .weight(.5F)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                matchNumber,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // Red Alliance
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1F)
        ) {
            for (datapoint in datapoints) {
                // Get the value
                val value = getAllianceInMatchObjectByKey(
                    Constants.RED, matchNumber, datapoint
                )
                // Header
                Translations.ACTUAL_TO_HUMAN_READABLE[datapoint]?.let {
                    Text(
                        it,
                        color = colorResource(id = R.color.Red),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Header value text with proper formatting
                Text(
                    getHeaderValue(datapoint, value, hasTBAData),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.Red)
                )
            }
        }

    }
}


/** Row for each datapoint
 * @param datapoint the datapoint name*/
@Composable
fun MatchDetail(
    datapoint: String,
    matchNumber: Int,
    match: Match,
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
                        onOpenRankings
                    } else if (Constants.TIM_TO_TEAM[datapoint] in Constants.RANKABLE_FIELDS || datapoint in Constants.RANKABLE_FIELDS) {
                        // Opens the rankings list for the corresponding team datapoint
                        onOpenDatapointRankings(datapoint)
                    }
                }
                .padding(4.dp),
            fontSize = 15.sp
        )

        // Blue alliance
        match.blueTeams.forEachIndexed { index, it ->
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
                getDataValue(it, datapoint, matchNumber)?.let { it1 ->
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
                TimNotesDialog(
                    onDismiss = { displayPopUps[index] = false },
                    teamNumber = it,
                    datapoint = datapoint,
                    matchNumber = matchNumber,
                    isRedAlliance = false
                )
            }
        }

        // Red alliance
        match.redTeams.forEachIndexed() { index, it ->
            VerticalDivider()
            if (isNote) {
                IconButton(
                    onClick = { displayPopUps[index + 3] = true },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = colorResource(R.color.Red)),
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
                Text(
                    getDataValue(it, datapoint, matchNumber)!!,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(id = R.color.Red),
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

            // Display notes datapoints in a pop-up
            if (displayPopUps[index + 3]) {
                TimNotesDialog(
                    onDismiss = { displayPopUps[index + 3] = false },
                    teamNumber = it,
                    datapoint = datapoint,
                    matchNumber = matchNumber,
                    isRedAlliance = true
                )
            }
        }
    }
}

/** Function to generate the TIM notes dialog pop-up */
@Composable
fun TimNotesDialog(
    onDismiss: () -> Unit,
    teamNumber: String,
    datapoint: String,
    matchNumber: Int,
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
                    "${Translations.ACTUAL_TO_HUMAN_READABLE[datapoint] ?: ""} for $teamNumber in match $matchNumber",
                    textAlign = TextAlign.Center,
                    color = colorResource(id = if (isRedAlliance) R.color.Red else R.color.Blue),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                getDataValue(teamNumber, datapoint, matchNumber)?.let {
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


/** Function to return the String value of an essentials header
 * @param value the numerical value of the header value
 * @param hasTBAData whether there is TBA data*/
fun getHeaderValue(datapoint: String, value: String?, hasTBAData: Boolean): String {
    // value is a string, so we check for the string "null" or if is an actual null then replace the value with "?",
    // if it isn't, then displays data
    if (value == null || value == "null") {
        return Constants.NULL_CHARACTER
    } else {
        val processedValue = (if (hasTBAData) "%.0f" else "%.1f").format(value.toFloat())
        return if (Constants.PERCENT_DATA.contains(datapoint)) "${processedValue.toFloat() * 100}%" else processedValue
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
    matchNumber: Int
): String? {
    when (datapoint) {
        // Exceptions for defense ratings, which can only range from 0 - 5, -1 means no defense in that match
        "defense_rating" -> {
            val value = getTIMDataValueByMatch(
                matchNumber.toString(),
                teamNumber,
                "defense_rating"
            ) ?: Constants.NULL_CHARACTER
            return if (value == "-1") "N/A" else value
        }

        "avg_defense_rating" -> {
            val value =
                formatTeamDataValue(teamNumber, "avg_defense_rating")
            return if (value == "-1.0") "N/A" else value
        }
        // if displaying a team datapoint
        in Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS -> {
            return formatTeamDataValue(teamNumber, datapoint)
        }
        // Otherwise just pull the team in match data with the datapoint name without "tim" in it
        else -> {
            return getTIMDataValueByMatch(
                matchNumber.toString(),
                teamNumber,
                if (datapoint.endsWith("_tim")) {
                    datapoint.replace("_tim", "")
                } else {
                    datapoint
                }.toString()
            ) ?: Constants.NULL_CHARACTER
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