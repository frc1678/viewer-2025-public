package org.citruscircuits.viewer.fragments.team_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.onOpenAutoPath
import org.citruscircuits.viewer.constants.onOpenDatapointRankingFromTeamDetails
import org.citruscircuits.viewer.constants.onOpenGraphs
import org.citruscircuits.viewer.constants.onOpenMatchSchedule
import org.citruscircuits.viewer.constants.onOpenNotes
import org.citruscircuits.viewer.constants.onOpenPickability
import org.citruscircuits.viewer.constants.onOpenRankings
import org.citruscircuits.viewer.constants.onOpenRobotPic

class TeamDetailsFragment : Fragment() {

    private var teamNumber: String? = null
    private var isLFMMode by mutableStateOf(false)

    private fun populateTeamDetailsEssentials() {
        // If a fragment intent (bundle arguments) exists from the previous activity (MainViewerActivity),
        // then set the team number display on TeamDetails to the team number provided with the intent.

        // If the team number from the MainViewerActivity's match schedule list view cell position
        // is null, the default display will show '0' for the team number on TeamDetails.

        arguments?.let {
            teamNumber = it.getString(Constants.TEAM_NUMBER, Constants.NULL_CHARACTER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            RefreshPopup()
            if (ReloadingItems.finished.value) {
                RefreshPopup()
            }
            val targetDatapoint =
                arguments?.getString("targetDatapoint") //set auto scroll for datapoint
            populateTeamDetailsEssentials()

            TeamDetailsPage(
                // the team number
                teamNumber = teamNumber ?: "",
                isLFMMode = isLFMMode,
                //for search scrolling
                targetDatapoint = targetDatapoint,
                // this is setLFM() in teamdetailspage
                setLFM = { isLFMMode = !isLFMMode },
                // uses a variable defined in TeamDetailsPage.kt to attempt navigation to robot pics
                robotPicNav = { onOpenRobotPic(teamNumber ?: "") },
                // automatically navigates to auto paths fragment with the pre-defined teamNumber
                autoPathNav = { onOpenAutoPath(teamNumber ?: "") },
                // automatically navigates to match schedule fragment sorted by teamNumber
                matchListNav = { onOpenMatchSchedule(teamNumber ?: "") },
                // automatically opens notes fragment
                notesNav = { onOpenNotes(teamNumber ?: "") },
                // automatically opens pickability fragment
                pickabilityNav = { number, mode -> onOpenPickability(number, mode) },
                // opens graphs fragment with respect to a particular datapoint passed in TeamDetailsPage.kt
                graphsNav = { onOpenGraphs(teamNumber ?: "", it ?: "") },
                // opens team ranking fragment with respect to a particular datapoint passed in TeamDetailsPage.kt
                datapointRankingNav = { field ->
                    onOpenDatapointRankingFromTeamDetails(
                        field,
                        teamNumber ?: "",
                        isLFMMode
                    )
                },
                // opens all ranking page showing ranks and predicted ranks of all teams
                rankingNav = { onOpenRankings() }
            )
        }
    }
}