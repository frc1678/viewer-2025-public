package org.citruscircuits.viewer.fragments.match_schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getAllianceInMatchObjectByKey
import org.citruscircuits.viewer.fragments.match_details.MatchDetailsFragment
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.getMatchSchedule

/**
 * Fragment for showing the match schedule, with inline details about each match.
 */
class MatchScheduleFragment : Fragment() {
    companion object {
        var lastPageMatchDetails = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // use compose view to embed compose content
        val view = ComposeView(requireContext()).apply {
            setContent {
                //runs refresh popup
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                // call page composable
                MatchSchedulePage(
                    getMatchSchedule = { filter, search ->
                        getMatchSchedule(
                            // build search query for matches
                            teamNumbers = buildList {
                                if (search.isNotBlank()) add(search)
                                if (filter == MatchScheduleFilter.OUR) add(Constants.MY_TEAM_NUMBER)
                            },
                            starred = filter == MatchScheduleFilter.STARRED
                        )
                    },
                    // set initial search if arguments are given
                    initialSearch = arguments?.getString(Constants.TEAM_NUMBER) ?: "",
                    // open match details fragment
                    onOpenMatchDetails = {
                        lastPageMatchDetails = true
                        parentFragmentManager
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(
                                R.id.nav_host_fragment,
                                MatchDetailsFragment().apply {
                                    it.toIntOrNull()?.let { int ->
                                        arguments = bundleOf(Constants.MATCH_NUMBER to int)
                                    }
                                }
                            ).commit()
                    },
                    // open team details fragment
                    onOpenTeamDetails = {
                        parentFragmentManager
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(
                                R.id.nav_host_fragment,
                                TeamDetailsFragment().apply {
                                    arguments =
                                        bundleOf(Constants.TEAM_NUMBER to it, "LFM" to false)
                                }
                            ).commit()
                    },
                    onOpen = { matchSchedule, state ->
                        if (!lastPageMatchDetails) {
                            var i = 0
                            for (match in matchSchedule.values) {
                                val hasTIMData = hasDataForBothAlliances(match, "has_tim_data")
                                // if there isn't only one match in the filter (i = 0)
                                // then scroll to the most recent unplayed match or the last match if all have been played
                                if ((i != 0) && (!hasTIMData || i == matchSchedule.size)) {
                                    lifecycleScope.launch { state.scrollToItem(i - 1) }
                                    break
                                }
                                i++
                            }
                        }
                        lastPageMatchDetails = false
                    }
                )
            }
        }
        return view
    }
}
