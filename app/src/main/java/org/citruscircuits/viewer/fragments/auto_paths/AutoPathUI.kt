package org.citruscircuits.viewer.fragments.auto_paths

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import org.citruscircuits.viewer.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val startPosOffsets = mapOf(
    "1" to (Offset(778.5f, 760f) to Size(136.5f, 135f)),
    "2" to (Offset(778.5f, 595f) to Size(136.5f, 135f)),
    "3" to (Offset(778.5f, 430f) to Size(136.5f, 135f)),
    "4" to (Offset(778.5f, 263f) to Size(136.5f, 135f)),
    "5" to (Offset(778.5f, 90f) to Size(136.5f, 135f))
)

// Center of the hexagon
val hexagonCenter = Offset(480f, 480f)

// Button size for all reef elements
val buttonSize = Size(35f, 35f)
val algaeSize = Size(75f, 35f)

// Correct radial distances to the center of each level
// Calculated based on provided data and geometric relationships
val levelRadii = listOf(60.00, 118.00, 163.27, 214.5) // Centers of L1 to L4

// Angles for each face of the hexagon (F1 to F6)
val angles = listOf(-20f, -80f, -140f, 160f, 100f, 40f)

// Angles used when action is completed twice for second offsets in level of face
val otherAngles = listOf(20f, -40f, -100f, 200f, 140f, 80f)

// Algae Angles
val algaeAngles = listOf(10f, -60f, -130f, 180f, 112f, 52f)
val algaeRadius = listOf(89.4, 136.00, 89.4, 136.00, 89.4, 136.00)

// algae position on reef, 6 faces from 0-5
val algaePosition = listOf(0, 1, 2, 3, 4, 5)

fun calculateOffset(center: Offset, angle: Float, radius: Double): Offset {
    val radian = angle * PI.toFloat() / 180f
    return Offset(
        (center.x + radius * cos(radian)).toFloat(), (center.y + radius * sin(radian)).toFloat()
    )
}

val boxPositionOffsets: Map<String, Pair<Offset, Size>> = buildMap {
    for ((faceIndex, face) in listOf("F1", "F2", "F3", "F4", "F5", "F6").withIndex()) {
        for ((levelIndex, level) in listOf("L1", "L2", "L3", "L4").withIndex()) {
            val key = "reef_${face}_${level}"

            val angle = angles[faceIndex]
            // Modify radius slightly to avoid stacking exactly
            val radius = levelRadii[levelIndex]
            val offset = calculateOffset(hexagonCenter, angle, radius)

            put(key, offset to buttonSize)
        }
    }
    for (position in algaePosition) {
        val angle = algaeAngles[position]
        val radius = algaeRadius[position]
        val offset = calculateOffset(hexagonCenter, angle, radius)
        put("intake_reef_${position + 1}", offset to algaeSize)
    }
    // Static elements
    put("station_1", Offset(35f, 830f) to Size(100f, 100f))
    put("station_2", Offset(35f, 35f) to Size(100f, 100f))
    put("ground_1_algae", Offset(150f, 770f) to Size(100f, 150f))
    put("ground_2_algae", Offset(150f, 60f) to Size(100f, 150f))
    put("ground_1_coral", Offset(150f, 770f) to Size(100f, 150f))
    put("ground_2_coral", Offset(150f, 60f) to Size(100f, 150f))
    put("mark_1_coral", Offset(125f, 700f) to Size(82f, 40f))
    put("mark_1_algae", Offset(125f, 652.5f) to Size(82f, 40f))
    put("mark_2_coral", Offset(125f, 495f) to Size(82f, 40f))
    put("mark_2_algae", Offset(125f, 445f) to Size(82f, 40f))
    put("mark_3_coral", Offset(125f, 295f) to Size(82f, 40f))
    put("mark_3_algae", Offset(125f, 245f) to Size(82f, 40f))
    put("net", Offset(940f, 50f) to Size(55f, 425f))
    put("processor", Offset(656f, 925f) to Size(195f, 85f))
    put("drop_algae", Offset(390f, 825f) to Size(200f, 100f))
    put("drop_coral", Offset(390f, 25f) to Size(200f, 100f))
}
val alternateBoxPositionOffsets: Map<String, Pair<Offset, Size>> = buildMap {
    for ((faceIndex, face) in listOf("F1", "F2", "F3", "F4", "F5", "F6").withIndex()) {
        for ((levelIndex, level) in listOf("L1", "L2", "L3", "L4").withIndex()) {
            val key = "reef_${face}_${level}"

            val angle = otherAngles[faceIndex]
            // Modify radius slightly to avoid stacking exactly
            val radius = levelRadii[levelIndex]
            val offset = calculateOffset(hexagonCenter, angle, radius)

            put(key, offset to buttonSize)
        }
    }
}
val textPositionOffsets: Map<String, Offset> = boxPositionOffsets.mapValues { (_, value) ->
    val boxOffset = value.first
    Offset(boxOffset.x, boxOffset.y + value.second.height - 5f)
}
val alternateTextPositionOffsets: Map<String, Offset> =
    alternateBoxPositionOffsets.mapValues { (_, value) ->
        val boxOffset = value.first
        Offset(boxOffset.x, boxOffset.y + value.second.height - 5f)
    }

@Composable
fun AutoPath(
    timeline: List<String>?,
    autoPath: AutoPath,
    animationStep: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    // List of all score success values
    val scoreSuccessMapping = listOf(
        autoPath.score_1 to autoPath.score_1_successes,
        autoPath.score_2 to autoPath.score_2_successes,
        autoPath.score_3 to autoPath.score_3_successes,
        autoPath.score_4 to autoPath.score_4_successes,
        autoPath.score_5 to autoPath.score_5_successes,
        autoPath.score_6 to autoPath.score_6_successes,
        autoPath.score_7 to autoPath.score_7_successes,
        autoPath.score_8 to autoPath.score_8_successes,
        autoPath.score_9 to autoPath.score_9_successes
    )

    // Get the image of the map
    val fieldMapImage = ImageBitmap.imageResource(id = R.drawable.field_map_auto_paths)
    // Canvas to show the field map and all the things on it
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(modifier = modifier
        .fillMaxWidth()
        .onGloballyPositioned { coordinates -> boxSize = coordinates.size }) {
        Image(fieldMapImage, "", modifier = Modifier.fillMaxWidth())
        Canvas(modifier = Modifier.fillMaxWidth()) {
            if (autoPath.start_position != "0") {
                // Path showing the order that they intook and scored gamepieces in
                var path = Path()
                // The Path starts at the center of the start position box
                path.moveTo(
                    (startPosOffsets[autoPath.start_position]!!.first.x * boxSize.width / 1000) + ((startPosOffsets[autoPath.start_position]!!.second.width / 2f) * boxSize.width / 1000),
                    (startPosOffsets[autoPath.start_position]!!.first.y * boxSize.height / 1000) + ((startPosOffsets[autoPath.start_position]!!.second.height / 2f) * boxSize.height / 1000)
                )
                // Draw the starting position rectangle
                drawRect(
                    // Orange if they had a preloaded gamepiece and did not even attempt to score it
                    color = if (autoPath.has_preload == true) Color(
                        0, 185, 0,
                    )
                    else Color.Gray,
                    topLeft = Offset(
                        startPosOffsets[autoPath.start_position]!!.first.x * boxSize.width / 1000,
                        startPosOffsets[autoPath.start_position]!!.first.y * boxSize.height / 1000
                    ),
                    size = Size(
                        startPosOffsets[autoPath.start_position]!!.second.width * boxSize.width / 1000,
                        startPosOffsets[autoPath.start_position]!!.second.height * boxSize.height / 1000
                    ),
                )
                // begin auto path
                var opacity = 1f
                var differentiationOffset = 0
                if (!timeline.isNullOrEmpty()) {
                    var stepsDrawn = 0
                    val reefUsage = mutableListOf<String>()
                    val timesAttemptedMap: MutableMap<String, Int> = mutableMapOf()

                    for ((index, action) in timeline.withIndex()) {
                        val alreadyOccurred = (reefUsage.contains(action))
                        // Update usage count
                        if (action.contains("reef_F")) {
                            reefUsage += action
                        }
                        if (stepsDrawn < animationStep.value) {
                            differentiationOffset = (differentiationOffset + 10) % 30
                            //  Sets the next coordinate for the path to the center of the box corresponding to the current intake
                            if (alreadyOccurred) {
                                path.lineTo(
                                    (alternateBoxPositionOffsets[action]!!.first.x * boxSize.width / 1000) + (10f * boxSize.width / 1000) + differentiationOffset,
                                    (alternateBoxPositionOffsets[action]!!.first.y * boxSize.height / 1000) + (10f * boxSize.height / 1000) + differentiationOffset
                                )
                            } else {
                                path.lineTo(
                                    (boxPositionOffsets[action]!!.first.x * boxSize.width / 1000) + (10f * boxSize.width / 1000) + differentiationOffset,
                                    (boxPositionOffsets[action]!!.first.y * boxSize.height / 1000) + (10f * boxSize.height / 1000) + differentiationOffset
                                )
                            }
                            // draw path previously set path coordinate
                            drawPath(
                                path = path,
                                color = when {
                                    listOf("algae", "net", "processor").any {
                                        action.contains(
                                            it,
                                            ignoreCase = true
                                        )
                                    } -> Color.hsv(
                                        240f,
                                        1f,
                                        1f - stepsDrawn.toFloat() / timeline.size.toFloat() / 2
                                    )

                                    listOf("coral", "reef", "station").any {
                                        action.contains(
                                            it,
                                            ignoreCase = true
                                        )
                                    } -> Color.hsv(
                                        0f,
                                        1f,
                                        1f - stepsDrawn.toFloat() / timeline.size.toFloat() / 2
                                    )

                                    else -> Color(20, 99, 35)
                                },
                                style = Stroke(width = 20f),
                                alpha = opacity
                            )

                            // Sets path to a new Path so that the next line can have a different opacity
                            path = Path()
                            // Sets the start point for path to the center of the box corresponding to the current intake
                            if (alreadyOccurred) {
                                // Move to the alternate position
                                path.moveTo(
                                    (alternateBoxPositionOffsets[action]!!.first.x * boxSize.width / 1000) + (10f * boxSize.width / 1000) + differentiationOffset,
                                    (alternateBoxPositionOffsets[action]!!.first.y * boxSize.height / 1000) + (10f * boxSize.height / 1000) + differentiationOffset
                                )
                            } else {
                                // Move to the normal position
                                path.moveTo(
                                    (boxPositionOffsets[action]!!.first.x * boxSize.width / 1000) + (10f * boxSize.width / 1000) + differentiationOffset,
                                    (boxPositionOffsets[action]!!.first.y * boxSize.height / 1000) + (10f * boxSize.height / 1000) + differentiationOffset
                                )
                            }
                            // Lowers the opacity/alpha for the next path
                            opacity *= 0.90f

                            if (index < animationStep.value) {
                                timesAttemptedMap[action] = (timesAttemptedMap[action] ?: 0) + 1
                                // Update usage count
                                if (action.contains("reef_F")) {
                                    reefUsage += action
                                }
                                // draw box
                                drawAction(
                                    action = action,
                                    boxSize = boxSize,
                                    // if second time through, sum up with the previous instance
                                    // first group by action, filter out singletons, sum up
                                    scoreSuccesses = if (alreadyOccurred) scoreSuccessMapping.last { it.first == action }.second
                                        ?: 0
                                    else scoreSuccessMapping.find { it.first == action }?.second
                                        ?: 0,
                                    scoreAttempts = autoPath.num_matches_ran ?: 0,
                                    isRepeated = alreadyOccurred,
                                    darknessPercent = 1f - stepsDrawn.toFloat() / timeline.size.toFloat() / 2
                                )
                            }
                            // increment number of drawn step drawn
                            stepsDrawn++
                            // break if we have drawn all the steps
                        } else break
                    }
                }
            }
        }
    }
}

/** Helper for drawing text in a [Canvas].
 *
 * @param text The text to draw.
 * @param offset The coordinates to draw the text at.
 * @param size The size of the text.
 */
fun DrawScope.drawText(text: String, offset: Offset, size: Float) = drawIntoCanvas {
    it.nativeCanvas.drawText(text, offset.x, offset.y, Paint().apply { textSize = size })
}

/**
 * Function to draw boxes for intake and scoring actions in a [Canvas]
 *
 * @param action an intake or scoring action
 * @param boxSize size of the box
 * @param scoreSuccesses the number of successes for the [action]
 * @param scoreAttempts the number of attempts for the [action]
 * */
fun DrawScope.drawAction(
    action: String?,
    boxSize: IntSize,
    scoreSuccesses: Int,
    scoreAttempts: Int,
    isRepeated: Boolean,
    darknessPercent: Float = 0f
) = drawIntoCanvas {
    if (action != null) {
        val resultColor = when {
            listOf("coral", "reef", "station").any {
                action.contains(it, ignoreCase = true)
            } -> Color.hsv(0f, 1f, darknessPercent)

            listOf("algae", "net", "processor").any {
                action.contains(it, ignoreCase = true)
            } -> Color.hsv(240f, 1f, darknessPercent)

            else -> Color.Transparent
        }
        drawRoundRect(
            color = resultColor,
            topLeft = if (isRepeated) {
                Offset(
                    alternateBoxPositionOffsets[action]!!.first.x * boxSize.width / 1000,
                    alternateBoxPositionOffsets[action]!!.first.y * boxSize.height / 1000
                )
            } else {
                Offset(
                    boxPositionOffsets[action]!!.first.x * boxSize.width / 1000,
                    boxPositionOffsets[action]!!.first.y * boxSize.height / 1000
                )
            }, size = Size(
                boxPositionOffsets[action]!!.second.width * boxSize.width / 1000,
                boxPositionOffsets[action]!!.second.height * boxSize.height / 1000
            ), cornerRadius = CornerRadius(10f * boxSize.width / 1000)
        )
    }
    val intakeItems = listOf(
        "station_1",
        "station_2",
        "ground_1_coral",
        "ground_2_coral",
        "mark_1_coral",
        "mark_1_algae",
        "mark_2_coral",
        "mark_2_algae",
        "mark_3_coral",
        "mark_3_algae",
        "intake_reef_1",
        "intake_reef_2",
        "intake_reef_3",
        "intake_reef_4",
        "intake_reef_5",
        "intake_reef_6",
        "drop_algae",
        "drop_coral"
    )
    if (action != null) {
        if (!intakeItems.contains(action)) {
            drawText(
                text = "${scoreSuccesses}/${scoreAttempts}",

                offset = if (isRepeated) {
                    Offset(
                        alternateTextPositionOffsets[action]!!.x * boxSize.width / 1000,
                        alternateTextPositionOffsets[action]!!.y * boxSize.height / 1000
                    )
                } else {
                    Offset(
                        textPositionOffsets[action]!!.x * boxSize.width / 1000,
                        textPositionOffsets[action]!!.y * boxSize.height / 1000
                    )
                },
                size = 45f * boxSize.width / 1000
            )
        }
    }
}










