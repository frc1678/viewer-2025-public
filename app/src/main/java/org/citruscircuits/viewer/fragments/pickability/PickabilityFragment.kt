package org.citruscircuits.viewer.fragments.pickability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.First
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.LFMFirst
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.SecondDefensive
import org.citruscircuits.viewer.fragments.pickability.PickabilityMode.SecondOffensive
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment

class PickabilityFragment : Fragment() {

    private val teamDetailsFragment = TeamDetailsFragment()
    private val teamDetailsFragmentArguments = Bundle()
    private var startMode: PickabilityMode = First
    private var highlightedTeam: String? = null

    private fun populatePickabilityEssentials() {
        // If a fragment intent (bundle arguments) exists from the previous activity (MainViewerActivity),
        // then set the team number display on TeamDetails to the team number provided with the intent.

        // If the team number from the MainViewerActivity's match schedule list view cell position
        // is null, the default display will show '0' for the team number on TeamDetails.

        arguments?.let {
            startMode = when (it.getString(Constants.PICKABILITY_MODE, Constants.NULL_CHARACTER)) {
                "first_pickability" -> First
                "offensive_second_pickability" -> SecondOffensive
                "defensive_second_pickability" -> SecondDefensive
                "lfm_first_pickability" -> LFMFirst
                else -> First
            }
            highlightedTeam = it.getString(Constants.TEAM_NUMBER, Constants.NULL_CHARACTER)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        ComposeView(requireContext()).apply {
            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                populatePickabilityEssentials()
                //loads pickability page, also allows for transfer to team details page
                PickabilityPage(startMode, highlightedTeam) { mode, index ->
                    val list: List<String> = makeData(mode).keys.toList()
                    val pickabilityFragmentTransaction =
                        parentFragmentManager.beginTransaction()
                    teamDetailsFragmentArguments.putString(
                        Constants.TEAM_NUMBER,
                        list[index]
                    )
                    teamDetailsFragmentArguments.putBoolean("LFM", false)
                    teamDetailsFragment.arguments = teamDetailsFragmentArguments
                    pickabilityFragmentTransaction.setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    pickabilityFragmentTransaction.addToBackStack(null).replace(
                        (requireView().parent as ViewGroup).id, teamDetailsFragment
                    ).commit()
                }
            }
        }
}