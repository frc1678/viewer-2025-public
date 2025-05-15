package org.citruscircuits.viewer.fragments.auto_paths

import androidx.compose.runtime.mutableIntStateOf
import kotlinx.serialization.Serializable

/**
 * This is a data class to hold auto path data.
 * Because the data can be null from server, all the values need to be nullable too.
 */
@Suppress("PropertyName")
@Serializable
data class AutoPath(
    val match_numbers_played: String? = null,
    val start_position: String? = null,
    val has_preload: Boolean? = null,
    val intake_position_1: String? = null,
    val intake_position_2: String? = null,
    val intake_position_3: String? = null,
    val intake_position_4: String? = null,
    val intake_position_5: String? = null,
    val intake_position_6: String? = null,
    val intake_position_7: String? = null,
    val intake_position_8: String? = null,
    val intake_position_9: String? = null,
    val intake_position_10: String? = null,
    val intake_position_11: String? = null,
    val intake_position_12: String? = null,
    val score_1: String? = null,
    val score_2: String? = null,
    val score_3: String? = null,
    val score_4: String? = null,
    val score_5: String? = null,
    val score_6: String? = null,
    val score_7: String? = null,
    val score_8: String? = null,
    val score_9: String? = null,
    val score_10: String? = null,
    val score_11: String? = null,
    val score_12: String? = null,
    val score_13: String? = null,
    val score_1_successes: Int? = null,
    val score_2_successes: Int? = null,
    val score_3_successes: Int? = null,
    val score_4_successes: Int? = null,
    val score_5_successes: Int? = null,
    val score_6_successes: Int? = null,
    val score_7_successes: Int? = null,
    val score_8_successes: Int? = null,
    val score_9_successes: Int? = null,
    val score_10_successes: Int? = null,
    val score_11_successes: Int? = null,
    val score_12_successes: Int? = null,
    val score_13_successes: Int? = null,
    val num_matches_ran: Int? = null,
    val leave: Boolean? = null,
    val timeline: String? = null
)

val testAutoPath2 = AutoPath(
    match_numbers_played = "[17, 6, 7]",
    start_position = "2",
    has_preload = true,
    intake_position_1 = "ground_1_coral",
    intake_position_2 = "intake_reef_5",
    intake_position_3 = "intake_reef_2",
    intake_position_4 = "ground_1_coral",
    intake_position_5 = "ground_1_coral",
    intake_position_6 = "ground_1_coral",
    intake_position_7 = "ground_1_coral",
    intake_position_8 = "ground_1_coral",
    score_1 = "reef_F4_L4",
    score_2 = "reef_F4_L4",
    score_3 = "net",
    score_4 = "reef_F3_L2",
    score_5 = "reef_F3_L4",
    score_6 = "reef_F3_L1",
    score_7 = "reef_F2_L1", // Scored twice
    score_8 = "reef_F2_L3", // Scored twice
    score_9 = "reef_F2_L4",
    score_1_successes = 1,
    score_2_successes = 1,
    score_3_successes = 1,
    score_4_successes = 1,
    score_5_successes = 1,
    score_6_successes = 1,
    score_7_successes = 1,
    score_8_successes = 1,
    score_9_successes = 1,
    num_matches_ran = 3,
    leave = true,
    timeline = "'reef_F4_L4', " +
            "'ground_1_coral', " +
            "'reef_F4_L4', " +
            "'intake_reef_5', " +
            "'net'," +
            " intake_reef_2'"
)

val testAutoPath = AutoPath(
    match_numbers_played = "[2, 18, 29]",
    start_position = "2",
    has_preload = false,
    intake_position_1 = "intake_mark_1",
    intake_position_2 = "intake_mark_2",
    score_1 = "net",
    score_2 = "processor",
    score_1_successes = 2,
    score_2_successes = 2,
    score_3_successes = 1,
    num_matches_ran = 3,
    leave = true,
    timeline = "'net', 'mark_1_algae', 'processor', 'mark_2_algae', mark_3_algae"
)

val testAutoPath1 = AutoPath(
    match_numbers_played = "[9, 27, 13]",
    start_position = "5",
    has_preload = true,
    intake_position_1 = "station_1",
    intake_position_2 = "station_1",
    intake_position_3 = "station_1",
    intake_position_4 = "station_1",
    intake_position_5 = "station_1",
    intake_position_6 = "station_1",
    intake_position_7 = "station_1",
    intake_position_8 = "station_1",
    score_1 = "reef_F1_L4",
    score_2 = "reef_F4_L1",
    score_3 = "reef_F5_L2",
    score_4 = "reef_F4_L1",
    score_5 = "reef_F1_L4", // Scored twice
    score_6 = "reef_F2_L1",
    score_7 = "reef_F3_L2",
    score_8 = "reef_F4_L3",
    score_9 = "reef_F5_L1", // Scored twice
    score_1_successes = 2,
    score_2_successes = 1,
    score_3_successes = 1,
    score_4_successes = 1,
    score_5_successes = 2,
    score_6_successes = 1,
    score_7_successes = 1,
    score_8_successes = 1,
    score_9_successes = 2,
    num_matches_ran = 3,
    leave = true,
    timeline = "'station_1', 'reef_F1_L4', 'station_2', 'reef_F4_L1', 'ground_1_coral', 'reef_F5_L2', 'ground_2_coral', 'reef_F4_L1', 'mark_1_coral', 'reef_F1_L4'"
)


