package org.citruscircuits.viewer.fragments.alliance_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.StartupActivity
import org.citruscircuits.viewer.constants.onOpenAutoPath
import org.citruscircuits.viewer.constants.onOpenDatapointRankingsFromMatchDetails
import org.citruscircuits.viewer.constants.onOpenGraphs
import org.citruscircuits.viewer.constants.onOpenRankings
import org.citruscircuits.viewer.constants.onOpenTeamDetails
import org.citruscircuits.viewer.fragments.team_details.generateDataPointsDisplayed

/**
 * The fragment for the page that displays information about the elims alliances.
 * Reimplmeneted in 2025.
 * @see R.layout.fragment_alliance_details
 */
class AllianceDetailsFragment : Fragment() {

    private var lfmMode by mutableStateOf(false)
    private var targetDatapoint: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val alliances = StartupActivity.databaseReference?.alliances

        var allianceNumber by mutableIntStateOf(1)
        arguments?.let {
            targetDatapoint = arguments?.getString("targetDatapoint")
        }
        val datapoints = generateDataPointsDisplayed(lfmMode) - listOf("Notes Label", "See Matches")
        // composable page
        val view = ComposeView(requireContext()).apply {
            setContent {
                AllianceDetailsPage(
                    allianceDetails = alliances ?: emptyList(),
                    allianceNum = allianceNumber,
                    teams = alliances?.firstOrNull() { it.allianceNum == allianceNumber }?.picks
                        ?: emptyList(),
                    onChangeAlliance = { allianceNumber = it },
                    onOpenRankings = { onOpenRankings() },
                    onOpenDatapointRankings = { onOpenDatapointRankingsFromMatchDetails(it) },
                    onOpenAutoPath = { onOpenAutoPath(it) },
                    onOpenTeamDetails = { onOpenTeamDetails(it) },
                    onOpenGraphs = { teamNumber, datapoint -> onOpenGraphs(teamNumber, datapoint) },
                    datapoints = datapoints,
                    targetDatapoint = targetDatapoint
                )
            }
        }
        return view
    }
}
