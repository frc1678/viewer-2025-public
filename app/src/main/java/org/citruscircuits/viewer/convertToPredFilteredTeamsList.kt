package org.citruscircuits.viewer

import org.citruscircuits.viewer.data.getTeamObjectByKey

/**
 * @param teamsList The list of teams to be converted to a map of predicted rankings
 */
fun convertToPredFilteredTeamsList(teamsList: List<String>): List<String> {
    val unsortedMap = mutableMapOf<String, Double?>()
    for (team in teamsList) {
        unsortedMap[team] =
            if (getTeamObjectByKey(team, "current_rank") != null) getTeamObjectByKey(
                team,
                "predicted_rank"
            )?.toDouble()
            else 1000.0
    }
    return unsortedMap.toList().sortedBy { (_, value) -> value }.toMap().keys.toList()
}