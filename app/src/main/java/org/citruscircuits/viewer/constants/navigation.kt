package org.citruscircuits.viewer.constants

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.R
import org.citruscircuits.viewer.fragments.auto_paths.AutoPathsFragment
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleFragment
import org.citruscircuits.viewer.fragments.notes.NotesFragment
import org.citruscircuits.viewer.fragments.pickability.PickabilityFragment
import org.citruscircuits.viewer.fragments.ranking.RankingFragment
import org.citruscircuits.viewer.fragments.team_details.GraphsFragment
import org.citruscircuits.viewer.fragments.team_details.RobotPicFragment
import org.citruscircuits.viewer.fragments.team_details.StartPositionFragment
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.fragments.team_ranking.TeamRankingFragment

fun Fragment.onOpenRankings() {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            RankingFragment()
        )
        .commit()
}

fun Fragment.onOpenMatchSchedule(teamNumber: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            MatchScheduleFragment().apply {
                arguments = bundleOf(
                    Constants.TEAM_NUMBER to teamNumber
                )
            }
        )
        .commit()
}

fun Fragment.onOpenDatapointRankingsFromMatchDetails(datapoint: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            TeamRankingFragment().apply {
                arguments = bundleOf(
                    TeamRankingFragment.DATA_POINT to
                            (if (Constants.TIM_TO_TEAM[datapoint] in Constants.RANKABLE_FIELDS) Constants.TIM_TO_TEAM[datapoint]
                            else datapoint),
                    TeamRankingFragment.TEAM_NUMBER to null
                )
            }
        )
        .commit()
}

fun Fragment.onOpenAutoPath(teamNumber: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            AutoPathsFragment()
                .apply {
                    arguments = bundleOf(Constants.TEAM_NUMBER to teamNumber)
                }
        )
        .commit()
}

fun Fragment.onOpenTeamDetails(teamNumber: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            TeamDetailsFragment()
                .apply {
                    arguments = bundleOf(Constants.TEAM_NUMBER to teamNumber)
                }
        )
        .commit()
}

fun Fragment.onOpenRobotPic(teamNumber: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            RobotPicFragment()
                .apply {
                    arguments = bundleOf(Constants.TEAM_NUMBER to teamNumber)
                }
        )
        .commit()
}

fun Fragment.onOpenNotes(teamNumber: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            NotesFragment()
                .apply {
                    arguments = bundleOf(Constants.TEAM_NUMBER to teamNumber)
                }
        )
        .commit()
}

fun Fragment.onOpenPickability(teamNumber: String, mode: String) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            PickabilityFragment()
                .apply {
                    arguments = bundleOf(
                        Constants.TEAM_NUMBER to teamNumber,
                        Constants.PICKABILITY_MODE to mode
                    )
                }
        ).commit()
}

fun Fragment.onOpenDatapointRankingFromTeamDetails(
    datapoint: String,
    teamNumber: String,
    isLFMMode: Boolean
) {
    parentFragmentManager
        .beginTransaction()
        .addToBackStack(null)
        .replace(
            R.id.nav_host_fragment,
            TeamRankingFragment()
                .apply {
                    arguments = bundleOf(
                        TeamRankingFragment.DATA_POINT to datapoint,
                        TeamRankingFragment.TEAM_NUMBER to teamNumber,
                        TeamRankingFragment.IS_LFM_MODE to isLFMMode
                    )
                }
        )
        .commit()
}


/** First argument team number, second is datapoint, third is other team (optional) */
fun Fragment.onOpenGraphs(teamNumber: String, datapoint: String, otherTeam: String? = null) {
    val graphsFragment = GraphsFragment()
    val modeStartPositionFragment = StartPositionFragment()
    val graphsFragmentArguments = Bundle()
    val modeStartPositionFragmentArguments = Bundle()

    if (Constants.STARTING_POSITION_GRAPHING.contains(datapoint)) {
        // Show start position map instead of graphs
        modeStartPositionFragmentArguments.putString(Constants.TEAM_NUMBER, teamNumber)
        modeStartPositionFragmentArguments.putString("datapoint", datapoint)
        modeStartPositionFragment.arguments = modeStartPositionFragmentArguments

        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.nav_host_fragment, modeStartPositionFragment, "mode_start_position")
            .commit()
    } else {
        graphsFragmentArguments.putString(Constants.TEAM_NUMBER, teamNumber)
        graphsFragmentArguments.putString("datapoint", datapoint)
        if (!otherTeam.isNullOrEmpty()) {
            graphsFragmentArguments.putString("extra_team", otherTeam)
        }
        graphsFragment.arguments = graphsFragmentArguments

        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.nav_host_fragment, graphsFragment, "graphs")
            .commit()
    }
}
