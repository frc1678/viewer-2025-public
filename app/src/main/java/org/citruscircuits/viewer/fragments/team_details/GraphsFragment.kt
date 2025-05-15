package org.citruscircuits.viewer.fragments.team_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import co.yml.charts.ui.barchart.models.SelectionHighlightData
import org.citruscircuits.viewer.*
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.getTIMDataValue
import org.citruscircuits.viewer.data.sortTeamList
import org.citruscircuits.viewer.databinding.FragmentGraphsBinding
import org.citruscircuits.viewer.fragments.match_details.MatchDetailsFragment
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * [Fragment] for displaying a graph of a TIM data point for a given team.
 */
class GraphsFragment : Fragment() {

    private var _binding: FragmentGraphsBinding? = null
    private val binding get() = _binding!!

    /**
     * Converts TIM value string to a float Y value for plotting.
     * Handles special cases like cage levels, booleans, and defense rating.
     */
    private fun calculateY(value: String?, cage: Boolean, bool: Boolean, defense: Boolean): Float {
        return when {
            cage -> Constants.CAGE_LEVELS.indexOf(value).takeIf { it != -1 }?.toFloat() ?: 0f
            bool -> if (value == "T") 1f else 0f
            defense -> (value?.toFloatOrNull() ?: -1f) + 1f
            else -> value?.toFloatOrNull() ?: 0f
        }
    }

    /**
     * Gets the Y-axis label depending on the data type (cage, bool, etc.).
     */
    private fun getYAxisLabel(
        index: Int,
        maxRange: Float,
        steps: Int,
        cage: Boolean,
        bool: Boolean,
        defense: Boolean
    ): String {
        return when {
            cage -> Translations.ENDGAME_VALUES_TO_READABLE[Constants.CAGE_LEVELS.getOrNull(index)
                .toString()] ?: ""

            bool -> if (index == 0) "FALSE" else "TRUE"
            defense -> if (index != 0) (index - 1).toString() else "N/A"
            else -> (index * (maxRange / steps)).toInt().toString()
        }
    }

    /**
     * Generates bar data for a team.
     */
    private fun createBarData(
        team: String,
        dataPoint: String,
        offset: Float,
        color: Color,
        cage: Boolean,
        bool: Boolean,
        defense: Boolean
    ): List<BarData> {
        return getTIMDataValue(team, dataPoint).toList().mapIndexed { i, (matchKey, value) ->
            BarData(
                point = Point(
                    x = i.toFloat() + offset,
                    y = calculateY(value, cage, bool, defense)
                ),
                label = matchKey,
                color = color
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphsBinding.inflate(inflater, container, false)

        val teamNumber =
            requireArguments().getString(Constants.TEAM_NUMBER, Constants.NULL_CHARACTER)
        val dataPoint = requireArguments().getString("datapoint", Constants.NULL_CHARACTER)

        val showingCageLevels = dataPoint.contains("cage")
        val showingBool =
            dataPoint == "played_defense" || dataPoint.contains("park") || dataPoint.contains("leave") || dataPoint.contains(
                "preload"
            ) || dataPoint.contains("broken_mechanism") || dataPoint.contains("compatible_auto")
        val showingDefenseRating = dataPoint.contains("defense_rating")

        binding.composeView.setContent {
            RefreshPopup()
            if (ReloadingItems.finished.value) RefreshPopup()

            val selectedMatch = remember { mutableIntStateOf(0) }
            var expanded by remember { mutableStateOf(false) }
            val allTeams = sortTeamList(MainViewerActivity.teamList).filter { it != teamNumber }
            //Add extra team if it exists
            val selectedTeams = remember {
                mutableStateListOf<String>().apply {
                    val extraTeam = requireArguments().getString("extra_team")
                    if (!extraTeam.isNullOrEmpty() && extraTeam != teamNumber) {
                        add(extraTeam)
                    }
                }
            }

            //Current colors for teams: Red, Blue, Orange, Green
            val colorPalette = listOf(Color.Red, Color.Blue, Color(0xFFFF9800), Color(0xFF21EE35))

            // Dynamically space bar offsets so they stay centered per match
            val spacing = 0.15f
            val numBars = selectedTeams.size + 1
            val centerOffset = spacing * (numBars - 1) / 2f

            val allBarData = mutableListOf<BarData>()

            // Main team
            allBarData += createBarData(
                teamNumber,
                dataPoint,
                -centerOffset,
                colorPalette[0],
                showingCageLevels,
                showingBool,
                showingDefenseRating
            )

            // Comparison teams
            selectedTeams.forEachIndexed { index, team ->
                val offset = spacing * (index + 1) - centerOffset
                allBarData += createBarData(
                    team,
                    dataPoint,
                    offset,
                    colorPalette.getOrElse(index + 1) { Color.Gray },
                    showingCageLevels,
                    showingBool,
                    showingDefenseRating
                )
            }

            // Y-axis scaling
            val maxValue = allBarData.maxOfOrNull { it.point.y }
            val maxRange = when {
                showingCageLevels -> Constants.CAGE_LEVELS.lastIndex.toFloat()
                showingBool -> 1f
                showingDefenseRating -> 6f
                maxValue != null -> if (ceil(maxValue / 5) * 5 == 0f) 30f else ceil(maxValue / 5) * 5
                else -> 30f
            }

            val ySteps = when {
                showingCageLevels -> Constants.CAGE_LEVELS.lastIndex
                showingBool -> 1
                showingDefenseRating -> 6
                else -> 5
            }

            // Axis setup
            val xAxisData = AxisData.Builder()
                .axisStepSize(30.dp)
                .steps(11)
                .bottomPadding(40.dp)
                .axisLabelAngle(0f)
                .labelData { index -> (index + 1).toString() }
                .build()

            val yAxisData = AxisData.Builder()
                .steps(ySteps)
                .labelAndAxisLinePadding(25.dp)
                .axisOffset(15.dp)
                .topPadding(40.dp)
                .labelData { index ->
                    getYAxisLabel(
                        index,
                        maxRange,
                        ySteps,
                        showingCageLevels,
                        showingBool,
                        showingDefenseRating
                    )
                }
                .build()

            // Data label shown when a bar is selected
            val selectionHighlightData = SelectionHighlightData(popUpLabel = { x, y ->
                val matchLabel = allBarData.find { it.point.x == x }?.label
                selectedMatch.intValue = matchLabel?.toIntOrNull()
                    ?: (x + 1).toInt()  // CHANGED: Use the label as match number
                "QM${selectedMatch.intValue}: ${
                    if (showingCageLevels) Constants.CAGE_LEVELS.getOrNull(y.roundToInt())
                    else if (showingBool) if (y == 0f) "FALSE" else "TRUE"
                    else if (showingDefenseRating) if (y.toInt() != 0) "${y - 1}/5" else "N/A"
                    else y
                }"
            })

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Header section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = Translations.ACTUAL_TO_HUMAN_READABLE.getOrDefault(
                                dataPoint,
                                dataPoint
                            ), style = TextStyle(fontSize = 24.sp)
                        )
                        Text(
                            text = teamNumber,
                            style = TextStyle(fontSize = 20.sp, color = Color.Gray)
                        )
                    }
                    // Dropdown for adding comparison teams
                    Column(horizontalAlignment = Alignment.End) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }) {
                            Text(
                                "Add Team", modifier = Modifier
                                    .menuAnchor()
                                    .padding(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                allTeams.forEach { team ->
                                    DropdownMenuItem(text = { Text(team) }, onClick = {
                                        if (team !in selectedTeams && selectedTeams.size < 3) selectedTeams.add(
                                            team
                                        )
                                        expanded = false
                                    })
                                }
                            }
                        }
                        // Display added teams
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .heightIn(max = 150.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp)
                            ) {
                                selectedTeams.forEachIndexed { index, team ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .padding(end = 6.dp)
                                                .background(
                                                    color = colorPalette.getOrElse(index + 1) { Color.Gray },
                                                    shape = MaterialTheme.shapes.small
                                                )
                                        )
                                        Text(team, fontSize = 14.sp)
                                        IconButton(
                                            onClick = { selectedTeams.remove(team) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("X", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //The Bar Chart
                BarChart(
                    modifier = Modifier.fillMaxHeight(0.9f),
                    barChartData = BarChartData(
                        chartData = allBarData,
                        xAxisData = xAxisData,
                        yAxisData = yAxisData,
                        barStyle = BarStyle(selectionHighlightData = selectionHighlightData)
                    )
                )

                // Navigation to match details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Match Number",
                        modifier = Modifier.padding(10.dp),
                        style = TextStyle(fontSize = 24.sp)
                    )
                    if (selectedMatch.intValue != 0) {
                        Button(onClick = {
                            parentFragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.nav_host_fragment, MatchDetailsFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt(Constants.MATCH_NUMBER, selectedMatch.intValue)
                                    }
                                }).commit()
                        }) {
                            Text("Go to match: ${selectedMatch.intValue}")
                        }
                    }
                }
            }
        }
        return binding.root
    }
}
