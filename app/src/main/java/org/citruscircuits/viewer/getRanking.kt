package org.citruscircuits.viewer

import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.data.getTeamDataValue

/**
 * Initializes a leaderboard for a given [datapoint]. The order of the leaderboard (ascending or
 * descending) is determined by [`Constants.RANKABLE_FIELDS`][Constants.RANKABLE_FIELDS]. Once the
 * leaderboard is generated, it is added to the [cache][MainViewerActivity.leaderboardCache]. This
 * function should be called for every datapoint on startup.
 */
fun createLeaderboard(datapoint: String) {
    val data = mutableListOf<TeamRankingItem>()

    // For the given datapoint, add a TeamRankingItem for each team containing the team's value, number, and null for placement
    MainViewerActivity.teamList.forEach {
        data.add(TeamRankingItem(it, getTeamDataValue(it, datapoint), null))
    }
    // Whether the leaderboard should be descending
    val descending = Constants.RANKABLE_FIELDS[datapoint] ?: true
    // Teams that have a null value for that datapoint
    val nullTeams = data.filter { it.value == Constants.NULL_CHARACTER }
    // Nonnull teams
    val nonNullTeams = data.filter { it.value != Constants.NULL_CHARACTER }
    // Sort nonNull teams by team number
    var sorted = nonNullTeams.sortedBy { it.teamNumber.toIntOrNull() }
    // Reverse if descending
    if (descending) sorted = sorted.reversed()
    // Sort again for exceptions
    sorted = sorted.sortedBy {
        when (datapoint) {
            // if has a pit data data translation
            in Constants.PIT_DATA -> {
                (Constants.RANK_BY_PIT[it.value] ?: it.value?.toFloatOrNull() ?: 0).toFloat()
            }
            // if fraction value
            "matches_with_data" -> {
                val itemValue = it.value ?: "0/1"
                val parts = itemValue.split("/")
                val numerator = parts[0].toDoubleOrNull()
                val denominator = parts[1].toDoubleOrNull()
                (numerator?.div(denominator ?: 1.0))?.toFloat()
            }
            // if boolean value, sort by the boolean value to string
            else -> {
                if (it.value in listOf("True", "False")) {
                    if (it.value == "True") 1.0f else 0.0f
                } else it.value?.toFloatOrNull()
            }
        }
    }
    // Reverse again after the above sorts in ascending order
    if (descending) sorted = sorted.reversed()
    // Make sure the placement property is correct
    sorted.forEachIndexed { index, teamRankingItem ->
        // If it isn't the first item, check to set the same placements for teams with the same values
        if (index != 0 && sorted[index - 1].value == teamRankingItem.value) {
            sorted[index].placement = sorted[index - 1].placement
        } else {
            // Set the placement of the first item to 1
            sorted[index].placement = index + 1
        }
    }
    // Add teams with null values to bottom
    MainViewerActivity.leaderboardCache[datapoint] = sorted + nullTeams
}

/** Gets the leaderboard for a given datapoint */
fun getRankingList(datapoint: String) =
    MainViewerActivity.leaderboardCache[datapoint] ?: emptyList()

/** Gets the value of a team for a given datapoint leaderboard */
fun getRankingTeam(teamNumber: String, datapoint: String) =
    MainViewerActivity.leaderboardCache[datapoint]?.find { it.teamNumber == teamNumber }

data class TeamRankingItem(val teamNumber: String, val value: String?, var placement: Int?)
typealias Leaderboard = List<TeamRankingItem>
