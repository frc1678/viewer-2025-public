package org.citruscircuits.viewer.fragments.ranking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.convertToFilteredTeamsList
import org.citruscircuits.viewer.convertToPredFilteredTeamsList
import org.citruscircuits.viewer.data.getTeamObjectByKey
import java.util.regex.Pattern

enum class CardSelection { CURRENT, PREDICTED }

// newGetTeamObject retrieves the team rank from the list of team numbers using the field and position
fun newGetTeamObject(teamList: List<String>, field: String, position: Int) =
    getTeamObjectByKey(teamList[position], field)

// The ranking page is a list of all the teams and their ranks, current and predicted.
// It also contains current avg rps, predicted rps, current rps.
@Composable
fun RankingPage(
    onOpenTeamDetails: (String) -> Unit,
    teamList: List<String>,
    targetTeamNumber: String? = null
) {
    var selectedCard by remember { mutableStateOf(CardSelection.CURRENT) }
    val rankingList =
        if (selectedCard == CardSelection.CURRENT) convertToFilteredTeamsList(teamList) else convertToPredFilteredTeamsList(
            teamList
        )
    val regex: Pattern = Pattern.compile("[0-9]+" + Regex.escape(".") + "[0-9]+")
    val customButtonColor = Color(0xFF93C8CC)
    val fontSize = 11.sp
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Row that contains CURRENT and PRED. Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        ) {
            // The CURRENT RANKING button. Upon clicking switches current and predicted ranks in the lazy column row.
            Card(
                onClick = { selectedCard = CardSelection.CURRENT },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == CardSelection.CURRENT) customButtonColor else Color.LightGray
                ),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "CURRENT\nRANKINGS",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            // The PREDICTED RANKING button. Upon clicking switches current and predicted ranks in the lazy column row.
            Card(
                onClick = { selectedCard = CardSelection.PREDICTED },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCard == CardSelection.PREDICTED) customButtonColor else Color.LightGray
                ),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "PREDICTED\nRANKINGS",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        HorizontalDivider(thickness = 5.dp, color = Color.Black)
        // Row below CURRENT and PRED. buttons and above the lazy columns. Labels each sub-column.
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Spacer and boxes with empty boxspace act as spacers to squish other boxes into shape.
            Spacer(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 58.dp),
                contentAlignment = Alignment.Center
            ) {}
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 100.dp),
                contentAlignment = Alignment.Center
            ) {}
            // displays "Current Avg RPs"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[1]].toString(),
                    fontSize = fontSize,
                    modifier = Modifier
                        .padding(2.dp)
                )
            }
            // displays "# Current RPs"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[2]].toString(),
                    fontSize = fontSize,
                    modifier = Modifier.padding(2.dp)
                )
            }
            // displays "# Pred. RPs"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Translations.ACTUAL_TO_HUMAN_READABLE[Constants.FIELDS_TO_BE_DISPLAYED_RANKING[3]].toString(),
                    fontSize = fontSize,
                    modifier = Modifier.padding(2.dp)
                )
            }
            // displays "Pred. Rank" or "Current Rank" depending on the selected card.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedCard == CardSelection.CURRENT) "Pred. Rank" else {
                        "Current Rank"
                    },
                    fontSize = fontSize,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
        HorizontalDivider(thickness = 5.dp, color = Color.Black)
        /* A scrollable column displaying team rankings, including current rank, team number, average and current RP,
        predicted RP, and predicted rank. Switching between CURRENT RANKINGS and PRED. RANKINGS updates the first
        and sixth columns accordingly.*/

        //Scrolling to datapoint from search
        val listState = rememberLazyListState()

        LaunchedEffect(targetTeamNumber, teamList) {
            if (targetTeamNumber != null) {
                val index = teamList.indexOf(targetTeamNumber)
                if (index != -1) listState.scrollToItem(index)
            }
        }


        LazyColumn(state = listState) {
            itemsIndexed(rankingList) { index, item ->
                val currentAvgRps = getTeamObjectByKey(item, "current_avg_rps")
                val predictedRps = getTeamObjectByKey(item, "predicted_rps")
                val currentRps = getTeamObjectByKey(item, "current_rps")
                val predictedRank = getTeamObjectByKey(item, "predicted_rank")
                val currentRank = getTeamObjectByKey(item, "current_rank")
                // upon clicking each row, sends you to the respective teams teamdetails page.
                Row(
                    modifier = Modifier
                        .clickable(onClick = { onOpenTeamDetails(item) })
                        .height(IntrinsicSize.Max)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Spacer(
                        modifier = Modifier
                            .padding(12.dp)
                    )
                    // displays current or predicted rank
                    Box(
                        modifier = Modifier
                            .weight(.5f)
                    ) {
                        Text(
                            text = if (selectedCard == CardSelection.CURRENT) currentRank
                                ?: "?" else predictedRank ?: "?",
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .padding(6.dp)
                    )
                    // displays team number
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                    ) {
                        Text(
                            text = item,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    // displays current Avg RPs
                    Box(
                        modifier = Modifier
                            .weight(.75f)
                    ) {
                        Text(
                            text = if (currentAvgRps !in setOf(null, "null")) {
                                if (regex.matcher(currentAvgRps!!).matches()) {
                                    "%.2f".format(currentAvgRps.toFloat())
                                } else {
                                    currentAvgRps
                                }
                            } else "?",
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    // displays the Current RPs
                    Box(
                        modifier = Modifier
                            .weight(.75f)
                    ) {
                        Text(
                            text = "${currentRps ?: 0}",
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    //displays # Pred. RPs
                    Box(
                        modifier = Modifier
                            .weight(.85f)
                    ) {
                        Text(
                            text = if (predictedRps != null) "%.1f".format(predictedRps.toFloat()) else "?",
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    // displays Pred. or Current Rank
                    Box(
                        modifier = Modifier
                            .weight(.8f)
                    ) {
                        Text(
                            text = if (selectedCard == CardSelection.CURRENT) predictedRank
                                ?: "?" else {
                                currentRank ?: "?"
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .padding(6.dp)
                    )
                }
                HorizontalDivider()
            }
        }
    }
}