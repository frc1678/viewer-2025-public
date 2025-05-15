package org.citruscircuits.viewer.fragments.team_comparison

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.onOpenGraphs

/**
 * Hosts the team comparison page with saved state and handles navigation.
 */
class TeamComparisonFragment : Fragment() {
    private val teamList by lazy { MainViewerActivity.teamList.sorted() }

    // Remember the selected teams while navigating between pages
    private var leftTeam = mutableStateOf("1678")
    private var rightTeam = mutableStateOf(
        teamList
            .filter { it != "1678" }
            .minByOrNull { it.toIntOrNull() ?: Int.MAX_VALUE }
            ?: teamList.getOrNull(1).orEmpty()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TeamComparisonPage(
                        teamList = teamList,
                        leftTeam = leftTeam,
                        rightTeam = rightTeam,
                        onClickDatapoint = { mainTeam, otherTeam, datapoint ->
                            onOpenGraphs(
                                mainTeam,
                                Constants.GRAPHABLE[datapoint] ?: datapoint,
                                otherTeam
                            )
                        }
                    )
                }
            }
        }
    }
}
