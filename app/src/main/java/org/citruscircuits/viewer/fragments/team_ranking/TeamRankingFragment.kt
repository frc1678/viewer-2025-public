package org.citruscircuits.viewer.fragments.team_ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.getRankingList

class TeamRankingFragment : Fragment() {
    companion object {
        const val TEAM_NUMBER = "teamNumber"
        const val DATA_POINT = "dataPoint"
        const val IS_LFM_MODE = "isLFMMode"
    }

    private var dataPoint: String? = null
    var teamNumber: String? = null
    var isLFMMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        // use compose view to embed compose content
        ComposeView(requireContext()).apply {
            populateArguments()
            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                dataPoint?.let {
                    teamNumber?.let { it1 ->
                        TeamRankingPage(
                            // open team details fragment
                            onOpenTeamDetails = { team ->
                                val ft = parentFragmentManager.beginTransaction()
                                val fragment = TeamDetailsFragment().apply {
                                    arguments =
                                        bundleOf(Constants.TEAM_NUMBER to team, "LFM" to false)
                                }
                                ft.addToBackStack(null)
                                    .replace((requireView().parent as ViewGroup).id, fragment)
                                    .commit()
                            }, // import dataPoint, teamNumber, dataValue into the function
                            it, it1, getRankingList(datapoint = dataPoint!!), isLFMMode
                        )
                    }
                }
            }
        }

    private fun populateArguments() {
        arguments?.let {
            dataPoint = it.getString(DATA_POINT, Constants.NULL_CHARACTER)
            teamNumber = it.getString(TEAM_NUMBER, Constants.NULL_CHARACTER)
            isLFMMode = it.getBoolean(IS_LFM_MODE, Constants.NULL_CHARACTER.toBoolean())
        }
    }
}