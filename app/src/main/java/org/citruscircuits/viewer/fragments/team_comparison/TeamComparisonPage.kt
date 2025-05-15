package org.citruscircuits.viewer.fragments.team_comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTeamDataValue
import java.util.Locale

/**
 * Displays a side-by-side comparison between two teams for selected datapoints.
 *
 * @param teamList The list of all available team numbers.
 * @param leftTeam The currently selected left team.
 * @param rightTeam The currently selected right team.
 * @param onClickDatapoint Callback to handle navigation when a datapoint is clicked.
 */
@Composable
fun TeamComparisonPage(
    teamList: List<String>,
    leftTeam: MutableState<String>,
    rightTeam: MutableState<String>,
    onClickDatapoint: (teamNumber: String, otherTeam: String, datapoint: String) -> Unit
) {
    val datapoints = listOf(
        "auto_avg_total_points",
        "tele_avg_total_points",
        "avg_expected_cycles",
        "cage_successes_deep",
        "avg_total_intakes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Team Comparison",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Dropdowns for selecting teams
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            TeamDropdown("Left Team", leftTeam.value, teamList) { leftTeam.value = it }
            TeamDropdown("Right Team", rightTeam.value, teamList) { rightTeam.value = it }
        }

        // Row header for column labels
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(leftTeam.value, style = MaterialTheme.typography.titleMedium)
            Text(
                "Datapoint",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                rightTeam.value,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End
            )
        }

        // Each row shows a datapoint value comparison
        datapoints.forEach { datapoint ->
            ComparisonRow(datapoint, leftTeam.value, rightTeam.value) { clickedTeam, field ->
                val other = if (clickedTeam == leftTeam.value) rightTeam.value else leftTeam.value
                onClickDatapoint(clickedTeam, other, field)
            }
        }
    }
}

/**
 * Dropdown for selecting a team.
 */
@Composable
fun TeamDropdown(
    label: String,
    selectedTeam: String,
    teamList: List<String>,
    onTeamSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $selectedTeam")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            teamList.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }.forEach { team ->
                DropdownMenuItem(
                    text = { Text(team) },
                    onClick = {
                        onTeamSelected(team)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Row that compares two teams for a single datapoint.
 */
@Composable
fun ComparisonRow(
    datapoint: String,
    leftTeam: String,
    rightTeam: String,
    onClickTeam: (String, String) -> Unit
) {
    val leftValue = formatValue(getTeamDataValue(leftTeam, datapoint))
    val rightValue = formatValue(getTeamDataValue(rightTeam, datapoint))
    val humanReadable = Translations.ACTUAL_TO_HUMAN_READABLE[datapoint] ?: datapoint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ClickableStat(leftValue) { onClickTeam(leftTeam, datapoint) }
        Text(text = humanReadable, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        ClickableStat(rightValue) { onClickTeam(rightTeam, datapoint) }
    }
}

/**
 * Stat value that looks like a button and can be clicked.
 */
@Composable
fun ClickableStat(value: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .background(color = Color(0xFFEEEEEE), shape = RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(text = value, textAlign = TextAlign.Center)
    }
}

/**
 * Formats a value to 2 decimal places if it's a number.
 */
fun formatValue(value: String): String {
    return value.toDoubleOrNull()?.let {
        String.format(Locale.US, "%.2f", it)
    } ?: value
}
