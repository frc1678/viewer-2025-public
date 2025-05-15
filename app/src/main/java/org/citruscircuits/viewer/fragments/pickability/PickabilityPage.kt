package org.citruscircuits.viewer.fragments.pickability

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.convertToFilteredTeamsList
import org.citruscircuits.viewer.data.getTeamDataValue
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.First
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.LFMFirst
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.SecondOffensive
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.SecondDefensive
import java.lang.Float.parseFloat

//creates the class used for pickability mode
enum class PickabilityMode {
    First, SecondDefensive, SecondOffensive, LFMFirst;

    val stringName
        get() = when (this) {
            First -> "first_pickability"
            SecondDefensive -> "defensive_second_pickability"
            SecondOffensive -> "offensive_second_pickability"
            LFMFirst -> "lfm_first_pickability"
        }

    val shortName
        get() = when (this) {
            First -> "1st"
            SecondDefensive -> "2nd (D)"
            SecondOffensive -> "2nd (O)"
            LFMFirst -> "L4M"
        }
}

//takes in mode and converts it into sorted map of team # -> pickability score for this mode
fun makeData(mode: PickabilityMode): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val rawTeamNumbers = convertToFilteredTeamsList(MainViewerActivity.teamList)
    rawTeamNumbers.forEach { e -> map[e] = getTeamDataValue(e, mode.stringName) }
    return map.toList().sortedBy { (_, v) -> (v.substringAfter("&").toFloatOrNull()) }.reversed()
        .toMap().toMutableMap()
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
//requires passed-in lambda to allow clicked rows to lead to team details page
fun PickabilityPage(
    startMode: PickabilityMode,
    highlightedTeam: String?,
    navigateToTeamDetailsFragment: (PickabilityMode, Int) -> Unit
) {

    //current pickability mode
    var mode by remember { mutableStateOf(startMode) }

    Scaffold {
        Column {
            //holds title text
            Box(
                modifier = Modifier
                    .border(width = 2.dp, color = Color.Black)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    "Pickability",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold
                )
            }
            //holds row of header information
            Box(
                modifier = Modifier
                    .border(width = 2.dp, color = Color.Black)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {

                        Text(
                            "Rank",
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Center),
                        )

                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f)
                    ) {

                        Text(
                            "Team #",
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f)
                    ) {

                        Text(
                            "Pickability",
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Center),
                        )

                    }

                    //necessary for drop-down menu, represents whether or not the menu is shown
                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                        //lets you click this box, causing the drop-down to appear
                        .clickable {
                            expanded = true
                        }) {

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                //displays current mode in a shorter, readable format
                                mode.shortName,
                                fontSize = 15.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            //each of these are individual entries in the drop-down menu, clickable
                            PickabilityMode.values().forEach { thisMode ->
                                DropdownMenuItem(onClick = {
                                    mode = thisMode; expanded = false
                                }, text = { Text(thisMode.shortName) })
                            }
                        }

                    }

                }
            }
            Box(
                modifier = Modifier
                    .border(width = 2.dp, color = Color.Black)
                    .fillMaxWidth()
                    .weight(10f)
            ) {
                //scrollable column with unknown number of children
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    //creates a child of the LazyColumn for every team in the list
                    makeData(mode).keys.forEachIndexed { index, team ->

                        item {
                            Row(modifier = Modifier
                                .border(width = 1.dp, color = Color.LightGray)
                                .background(if (highlightedTeam == team) Color(0xFFFFBF00) else Color.Transparent)
                                .fillMaxWidth()
                                .height(30.dp)
                                //runs the lambda we passed in during PickabilityFragment.kt with params
                                .clickable {
                                    navigateToTeamDetailsFragment(mode, index)
                                }) {
                                //also converts pickability mode to more readable format
                                val pickabilityModeForThisItem: String = when (mode) {
                                    First -> "1st"
                                    SecondOffensive -> "Offensive 2nd"
                                    SecondDefensive -> "Defensive 2nd"
                                    LFMFirst -> "LFM 1st"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                ) {
                                    Text(
                                        (index + 1).toString(),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(30.dp)
                                ) {
                                    Text(
                                        team,
                                        modifier = Modifier.align(Alignment.Center),
                                        fontWeight = FontWeight.Bold
                                    )

                                }
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(30.dp)
                                ) {
                                    Text(
                                        pickabilityModeForThisItem,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(30.dp)
                                ) {
                                    Text(
                                        if (mode != First) parseFloat(
                                            ("%.1f").format(
                                                makeData(mode)[team]?.substringAfter("&")
                                                    ?.toFloatOrNull() ?: 0.0
                                            )
                                        ).toString()
                                        else if (makeData(mode)[team] == "?") "?"
                                        else parseFloat(("%.1f").format(makeData(mode)[team]?.toFloat())).toString(),
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}