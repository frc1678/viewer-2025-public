package org.citruscircuits.viewer.fragments.match_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.onOpenAutoPath
import org.citruscircuits.viewer.constants.onOpenDatapointRankingsFromMatchDetails
import org.citruscircuits.viewer.constants.onOpenGraphs
import org.citruscircuits.viewer.constants.onOpenRankings
import org.citruscircuits.viewer.constants.onOpenTeamDetails
import org.citruscircuits.viewer.fragments.match_schedule.Match
import org.citruscircuits.viewer.fragments.match_schedule.hasDataForBothAlliances
import org.citruscircuits.viewer.getMatchSchedule

class MatchDetailsFragment : Fragment() {

    private var match: Match = Match(0.toString())
    private var datapoints: MutableList<String> = mutableListOf()
    private var matchNumber: Int = 0
    private var targetDatapoint: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get match number from previous fragment
        arguments?.let {
            matchNumber = it.getInt(Constants.MATCH_NUMBER, 0)
            targetDatapoint = arguments?.getString("targetDatapoint")
        }
        // Get match object
        val matchKey = matchNumber.toString()
        val matchSchedule = getMatchSchedule()

        if (!matchSchedule.containsKey(matchKey)) {
            return ComposeView(requireContext()) // prevent crash from search
        }

        match = matchSchedule[matchKey]!!

        // Check if there is actual data
        val hasTBAData = hasDataForBothAlliances(match, "has_tba_data")
        val hasTIMData = hasDataForBothAlliances(match, "has_tim_data")

        // Get user's datapoints
        val user = MainViewerActivity.UserDataPoints.contents?.get("selected")?.asString
        val userDataPoints = MainViewerActivity.UserDataPoints.contents?.get(user)?.asJsonArray
        // Get specific datapoints depending on a user's preferences
        if (userDataPoints != null) {
            for (i in userDataPoints) {
                if (Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(i.asString) && !datapoints.contains(
                        i.asString
                    )
                ) {
                    datapoints.add(i.asString)
                }
            }
        }

        // Set the datapoints to their corresponding team datapoint if the match hasn't been played
        if (!hasTIMData) {
            for (i in 0 until datapoints.size) {
                datapoints[i] = Constants.TIM_TO_TEAM[datapoints[i]] ?: datapoints[i]
            }
        }

        // use compose view to embed compose content
        val view = ComposeView(requireContext()).apply {

            // Get match from match schedule

            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                MatchDetailsPage(
                    matchNumber, match, datapoints, hasTBAData,
                    // To Rankings fragment
                    onOpenRankings = { onOpenRankings() },
                    // To Team ranking fragment for a datapoint, goes to Team datapoint if
                    onOpenDatapointRankings = { datapoint ->
                        onOpenDatapointRankingsFromMatchDetails(
                            datapoint
                        )
                    },
                    onOpenAutoPath = { teamNumber -> onOpenAutoPath(teamNumber) },
                    onOpenTeamDetails = { teamNumber -> onOpenTeamDetails(teamNumber) },
                    onOpenGraphs = { teamNumber, datapoint -> onOpenGraphs(teamNumber, datapoint) },
                    targetDatapoint = targetDatapoint
                )
            }
        }
        return view
    }


}