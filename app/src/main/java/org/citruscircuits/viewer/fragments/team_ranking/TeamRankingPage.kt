package org.citruscircuits.viewer.fragments.team_ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.citruscircuits.viewer.TeamRankingItem
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTeamDataValue
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun TeamRankingPage(
    onOpenTeamDetails: (team: String) -> Unit,
    dataPoint: String,
    teamNumber: String,
    items: List<TeamRankingItem>,
    isLFMMode: Boolean
) {
    // get team list sorted
    val teams = items.map { it.teamNumber }
    val values = items.map { it.value }
    val placements = items.map { it.placement }
    val teamNumbers = items.map { it.teamNumber }

    //background
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            // title box on top
            Row(Modifier.fillMaxWidth()) {
                Box(
                    // centered
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row() {
                        val textValue = Translations.ACTUAL_TO_HUMAN_READABLE[dataPoint]
                            ?: Translations.ACTUAL_TO_HUMAN_READABLE[dataPoint.removePrefix("lfm_")]
                            ?: dataPoint
                        Text(
                            text = if (isLFMMode) {
                                "LFM $textValue"
                            } else {
                                textValue
                            },
                            modifier = Modifier.weight(0.75f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        //code for average defense rating
                        if (dataPoint == "avg_defense_rating") {
                            Text(
                                "Matches Played Defense",
                                modifier = Modifier.weight(0.22f),
                                textAlign = TextAlign.Center,
                                fontSize = 17.5.sp,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 4.dp, color = Black
            )
            LazyColumn() {
                itemsIndexed(teams) { index, team ->
                    if (index != 0) HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            //highlight row of the selected team number
                            .background(
                                if (teamNumbers[index] == teamNumber) Color(
                                    red = 39, green = 174, blue = 96, alpha = 255
                                ) else Color.Transparent
                            )
                            .clickable(
                                onClick = ({ onOpenTeamDetails(team) }),
                            )
                    ) {
                        Text(
                            //code for average defense rating
                            text = placements[index].toString(),
                            modifier = Modifier
                                .weight(0.25f)
                                .align(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        //Team Number text
                        Text(
                            text = teamNumbers[index],
                            modifier = Modifier
                                .weight(0.5f)
                                .align(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        //Data Value text
                        Text(
                            text = roundTwoDecimals(
                                values[index].toString()
                            ),
                            modifier = Modifier
                                .weight(0.25f)
                                .align(Alignment.CenterVertically),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        //code for average defense rating
                        if (dataPoint == "avg_defense_rating") {
                            Text(
                                text = getTeamDataValue(team, "matches_played_defense"),
                                modifier = Modifier
                                    .weight(0.2f)
                                    .align(Alignment.CenterVertically),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Round data values to 2 decimal places
fun roundTwoDecimals(float: String): String {
    if (float == Constants.NULL_CHARACTER) {
        return Constants.NULL_CHARACTER
    }

    // Regex to match valid floating-point numbers (including negatives and decimals)
    val floatRegex = """^-?\d+(\.\d+)?$""".toRegex()

    if (!float.matches(floatRegex)) {
        return float // Return the original string if invalid
    }

    val roundedNumber = BigDecimal(float).setScale(2, RoundingMode.HALF_UP).toDouble()

    return if (roundedNumber % 1 == 0.0) {
        "%d".format(roundedNumber.toInt()) // Whole number -> Remove .0
    } else {
        "%.2f".format(roundedNumber) // Ensure two decimal places
    }
}
