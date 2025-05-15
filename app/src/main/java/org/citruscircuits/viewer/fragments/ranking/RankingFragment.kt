package org.citruscircuits.viewer.fragments.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.convertToFilteredTeamsList
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment

/**
 * The fragment of the ranking lists 'view' that is one of the options of the navigation bar.
 * Disclaimer: This fragment contains another menu bar which is displayed directly above the main menu bar.
 * This navigation/menu bar does not switch between fragments on each menu's selection like the main menu bar does.
 * This navigation bar only receives the position/ID of the menu selected
 * and then updated the adapter of the list view that is right above it.
 */
class RankingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        // use compose view to embed compose content
        ComposeView(requireContext()).apply {
            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                // call page composable
                RankingPage(
                    // open team details fragment
                    onOpenTeamDetails = { team ->
                        val ft = parentFragmentManager.beginTransaction()
                        val fragment = TeamDetailsFragment().apply {
                            arguments = bundleOf(Constants.TEAM_NUMBER to team, "LFM" to false)
                        }
                        ft.addToBackStack(null)
                            .replace((requireView().parent as ViewGroup).id, fragment).commit()
                    },
                    convertToFilteredTeamsList(MainViewerActivity.teamList),
                    targetTeamNumber = arguments?.getString(Constants.TEAM_NUMBER)
                )
            }
        }
}
