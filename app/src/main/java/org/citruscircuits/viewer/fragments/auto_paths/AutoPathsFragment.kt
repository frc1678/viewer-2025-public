package org.citruscircuits.viewer.fragments.auto_paths

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.hsv
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.fragments.match_details.MatchDetailsFragment

/**
 * [Fragment] for showing the AutoPaths of a given team.
 */
class AutoPathsFragment : Fragment() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        // Get the team number from the arguments
        val teamNumber = requireArguments().getString(Constants.TEAM_NUMBER)!!
        var autoPaths =
            mutableListOf<Triple<MutableState<String>, MutableState<AutoPath>, MutableState<Int>>>()
//        var autoPaths = mutableListOf(
//            Triple(
//                mutableStateOf("1"), mutableStateOf(testAutoPath), mutableIntStateOf(25)
//            ),
//            Triple(
//                mutableStateOf("1"), mutableStateOf(testAutoPath1), mutableIntStateOf(25)
//            ),
//            Triple(
//                mutableStateOf("1"), mutableStateOf(testAutoPath2), mutableIntStateOf(25)
//            ),
//        )
//      Add each auto path to the list of auto paths
        StartupActivity.databaseReference!!.auto_paths[teamNumber]?.forEach { (pathNum, path) ->
            autoPaths.add(
                Triple(
                    mutableStateOf(pathNum),
                    mutableStateOf(path),
                    mutableIntStateOf(0)
                )
            )
        }
        // Sort the auto paths by the number of times they were ran
        autoPaths.sortByDescending { it.second.value.num_matches_ran }
        autoPaths = autoPaths.resetToMax()
        setContent {
            RefreshPopup()
            if (ReloadingItems.finished.value) {
                RefreshPopup()
            }
            // Only show the Auto Paths UI if there are AutoPaths
            if (autoPaths.isNotEmpty()) {
                BoxWithConstraints {
                    var showAutoStrategiesPopup by remember { mutableStateOf(false) }
                    // To adjust auto path padding, only change what maxWidth.value is being multiplied by
                    Column(
                        modifier = Modifier.padding(horizontal = (15 * maxWidth.value / 392.72726).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val scope = rememberCoroutineScope()
                        // The header for the page
                        Row {
                            Text(
                                "Auto Paths for $teamNumber",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            // Button to pull up auto path details
                            Button(onClick = { scope.launch { showAutoStrategiesPopup = true } }) {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        }
                        ViewAutoStrategies(
                            visible = showAutoStrategiesPopup,
                            teamNumber = teamNumber,
                            title = "View Auto Path Notes",
                            datapoint = "auto_strategies_team"
                        ) {
                            showAutoStrategiesPopup = !showAutoStrategiesPopup
                        }
                        // Displays each AutoPath
                        val state = rememberLazyListState()
                        LazyRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            state = state,
                            flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
                        ) {
                            autoPaths.forEachIndexed { index, path ->
                                item {
                                    var showAutoPathDetailsPopup by remember {
                                        mutableStateOf(
                                            false
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Show which matches this AutoPath was ran in
                                        val matchNums =
                                            path.second.value.match_numbers_played?.split(",")
                                        val timeline = path.second.value.timeline?.split(",")
                                            ?.filter { it.isNotEmpty() }
                                        Row {
                                            Text(text = "Match number(s): ", fontSize = 11.sp)
                                            if (matchNums != null) {
                                                for (matchNum in matchNums.sortedBy { it.toInt() }) {
                                                    Text(
                                                        text = "$matchNum, ",
                                                        modifier = Modifier.clickable {
                                                            // Opens the corresponding match details page
                                                            parentFragmentManager.beginTransaction()
                                                                .addToBackStack(null)
                                                                .replace(R.id.nav_host_fragment,
                                                                    MatchDetailsFragment().apply {
                                                                        matchNum.toIntOrNull()
                                                                            ?.let { int ->
                                                                                arguments =
                                                                                    bundleOf(
                                                                                        Constants.MATCH_NUMBER to int
                                                                                    )
                                                                            }
                                                                    }).commit()
                                                        },
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                        // Show how many times this AutoPath was ran
                                        Text(
                                            text = "Ran ${path.second.value.num_matches_ran} time(s)",
                                            fontSize = 11.sp
                                        )
                                        // Show whether the team scores leave points in this AutoPath
                                        Text(
                                            text = "Leave: " + if (path.second.value.leave == true) "Yes" else "No",
                                            fontSize = 11.sp
                                        )
                                        // What the numbers that are displayed mean
                                        Text(
                                            text = "# of Successes / # of Attempts",
                                            fontSize = 11.sp
                                        )
                                        // AutoPath display
                                        AutoPath(
                                            timeline = timeline,
                                            autoPath = path.second.value,
                                            animationStep = path.third
                                        )
                                        // AutoPath number label
                                        Row(horizontalArrangement = Arrangement.Center) {
                                            Button(
                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                onClick = {
                                                    autoPaths[index].third.value =
                                                        timeline?.size ?: 0
                                                    scope.launch {
                                                        state.scrollToItem(state.firstVisibleItemIndex - 1)
                                                    }
                                                },
                                                enabled = state.firstVisibleItemIndex > 0
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowCircleLeft,
                                                    contentDescription = null
                                                )
                                            }
                                            Text(
                                                text = "Path: #${path.first.value}/${autoPaths.size}",
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Button(
                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                onClick = {
                                                    autoPaths[index].third.value =
                                                        timeline?.size ?: 0
                                                    scope.launch {
                                                        state.scrollToItem(state.firstVisibleItemIndex + 1)
                                                    }
                                                },
                                                enabled = state.firstVisibleItemIndex < autoPaths.size - 1
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowCircleRight,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.Center) {
                                            // Button to play forward auto-paths
                                            Button(modifier = Modifier.padding(horizontal = 10.dp),
                                                onClick = {
                                                    if (autoPaths[index].third.value > 0) {
                                                        autoPaths[index].third.value -= 1
                                                    }
                                                }) {
                                                Icon(
                                                    Icons.Default.Remove,
                                                    contentDescription = null
                                                )
                                            }
                                            Button(modifier = Modifier.padding(horizontal = 1.dp),
                                                onClick = {
                                                    autoPaths[index].third.value = 0
                                                }) {
                                                Icon(
                                                    Icons.Default.Replay,
                                                    contentDescription = null
                                                )
                                            }
                                            Button(modifier = Modifier.padding(horizontal = 10.dp),
                                                onClick = { showAutoPathDetailsPopup = true }) {
                                                Icon(
                                                    Icons.Default.RemoveRedEye,
                                                    contentDescription = null
                                                )
                                            }
                                            // Button to play back auto-paths
                                            Button(modifier = Modifier.padding(horizontal = 1.dp),
                                                onClick = {
                                                    if (timeline != null) {
                                                        if (autoPaths[index].third.value < timeline.size)
                                                            autoPaths[index].third.value += 1
                                                    }

                                                }) {
                                                Icon(
                                                    Icons.Default.Add, contentDescription = null
                                                )
                                            }
                                            if (showAutoPathDetailsPopup) {
                                                ViewAutoPathDetails(
                                                    path.first.value, path.second.value
                                                ) {
                                                    showAutoPathDetailsPopup =
                                                        !showAutoPathDetailsPopup
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Column {
                            val text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontSize = 13.sp)) {
                                    append("earlier in order = ")
                                    withStyle(style = SpanStyle(color = 0f.autoPathColor())) {
                                        append(
                                            "red"
                                        )
                                    }
                                    append("\n")

                                    append("later in order = ")
                                    withStyle(style = SpanStyle(color = (7f / 8f).autoPathColor())) {
                                        append(
                                            "purple"
                                        )
                                    }
                                    append("\n")

                                    append("has preload = ")
                                    withStyle(style = SpanStyle(color = Color.Green)) { append("green") }
                                    append("\n")

                                    append("no preload = ")
                                    withStyle(style = SpanStyle(color = Color.Gray)) { append("gray") }
                                }
                            }

                            Text(
                                text = text,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            } else {
                Text("No Auto Paths")
            }
        }
    }
}

private fun MutableList<Triple<MutableState<String>, MutableState<AutoPath>, MutableState<Int>>>.resetToMax(): MutableList<Triple<MutableState<String>, MutableState<AutoPath>, MutableState<Int>>> {
    return this.map {
        // Remove the square brackets from the match numbers played and timeline
        val newMatchNumbers = it.second.value.match_numbers_played?.replace("[", "")
            ?.replace("]", "")?.replace(" ", "")

        val newTimeline = it.second.value.timeline?.replace("[", "")
            ?.replace("]", "")?.replace("'", "")?.replace(" ", "")

        // Create a new AutoPath object with updated values
        val newAutoPath = it.second.value.copy(
            match_numbers_played = newMatchNumbers,
            timeline = newTimeline
        )

        // Return the modified Triple
        Triple(
            it.first,
            mutableStateOf(newAutoPath),
            mutableIntStateOf(
                newAutoPath.timeline?.split(",")?.filter { item -> item.isNotEmpty() }?.size
                    ?: 0
            )
        )
    }.toMutableList()
}

@Composable
        /**
         * The dialog/popup that contains a timeline of all auto path actions for a given auto path
         * @param path The auto path being displayed
         */
fun ViewAutoPathDetails(pathNumber: String, path: AutoPath, onCancelRequest: () -> Unit) {
    // Creates the dialog/popup that displays the details for the auto paths
    Dialog(onDismissRequest = { onCancelRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.91f)
                .wrapContentHeight()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            // Displays the details
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                fun getIntakePosition(index: Int) = when (index) {
                    1 -> path.intake_position_1
                    2 -> path.intake_position_2
                    3 -> path.intake_position_3
                    4 -> path.intake_position_4
                    5 -> path.intake_position_5
                    6 -> path.intake_position_6
                    7 -> path.intake_position_7
                    8 -> path.intake_position_8
                    9 -> path.intake_position_9
                    else -> throw IllegalArgumentException("Invalid intake index: $index")
                }

                fun getScore(index: Int) = when (index) {
                    1 -> path.score_1
                    2 -> path.score_2
                    3 -> path.score_3
                    4 -> path.score_4
                    5 -> path.score_5
                    6 -> path.score_6
                    7 -> path.score_7
                    8 -> path.score_8
                    9 -> path.score_9
                    else -> throw IllegalArgumentException("Invalid score index: $index")
                }

                fun getScoreSuccesses(index: Int) = when (index) {
                    1 -> path.score_1_successes
                    2 -> path.score_2_successes
                    3 -> path.score_3_successes
                    4 -> path.score_4_successes
                    5 -> path.score_5_successes
                    6 -> path.score_6_successes
                    7 -> path.score_7_successes
                    8 -> path.score_8_successes
                    9 -> path.score_9_successes
                    else -> throw IllegalArgumentException("Invalid score successes index: $index")
                }

                val orderedActionList = mutableListOf<Pair<String?, Pair<String, String>>>()
                val hasPreload = path.has_preload == true

                for (i in 1..9) {
                    orderedActionList.add(
                        Pair(
                            Translations.ACTUAL_TO_HUMAN_READABLE[if (hasPreload && i == 1) "preload"
                            else if (hasPreload) getIntakePosition(i - 1)
                            else getIntakePosition(i)],
                            Pair(
                                Translations.ACTUAL_TO_HUMAN_READABLE[getScore(i)] ?: "?",
                                getScoreSuccesses(i).toString()
                            )
                        )
                    )
                }

                Text(
                    "Action Timeline:",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(5.dp)
                )
                Text(
                    "Path Number: $pathNumber",
                    fontSize = 15.sp,
                    modifier = Modifier.padding(3.dp)
                )
                orderedActionList.filter { it.first != "none" }.forEach { (item, score) ->
                    if (item == "Preload") Text(
                        text = "Had Preload",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(3.dp)
                    ) else Text(
                        text = "Intake: $item",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(3.dp)
                    )
                    if (score.first != "none") {
                        Text(
                            text = "Score: ${score.first}, ${score.second}/${path.num_matches_ran}",
                            fontSize = 15.sp,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Tap outside to close",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ViewAutoStrategies(
    visible: Boolean,
    teamNumber: String,
    title: String,
    datapoint: String,
    onCancelRequest: () -> Unit
) {
    if (visible)
    // Creates the dialog/popup that displays the details for the auto paths
    {
        Dialog(onDismissRequest = { onCancelRequest() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.91f)
                    .wrapContentHeight()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Displays the details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(5.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    if (Constants.USE_TEST_DATA) BodyText(
                        username = "Nathan",
                        teamNumber = teamNumber,
                        datapoint = datapoint
                    )
                    Text(
                        text = "Tap outside to close",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }

}

@Composable
fun BodyText(username: String, teamNumber: String, datapoint: String) {
}

fun Float.autoPathColor(): Color {
    return hsv((this * 360f) % 360f, 1f, 1f)
}