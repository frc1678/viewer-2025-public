package org.citruscircuits.viewer.fragments.match_schedule

/**
 * Data class for each match object.
 */
data class Match(var matchNumber: String) {
    var redTeams: ArrayList<String> = ArrayList()
    var blueTeams: ArrayList<String> = ArrayList()
}
