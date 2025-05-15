package org.citruscircuits.viewer.fragments.user_preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.google.gson.JsonArray
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants

class UserPreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                // Collect all datapoints, excluding unwanted ones
                val excludedDatapoints = listOf(
                    "See Matches", "Stand Strat Notes", "Notes Label", "Notes"
                )
                val dataPointsDisplay = (listOf("TEAM") +
                        Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS +
                        listOf("TIM") +
                        Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED)
                    .filterNot { it in excludedDatapoints }


                // Retrieve the current user
                val userNameJsonElement =
                    MainViewerActivity.UserDataPoints.contents?.get("selected")

                // Ensure the correct user is extracted from the JSON object
                val userName =
                    if (userNameJsonElement != null && userNameJsonElement.isJsonPrimitive) {
                        userNameJsonElement.asString
                    } else {
                        "OTHER"
                    }

                val chosenDatapoints = remember {
                    mutableStateListOf<String>().apply {
                        UserDataPoints.contents?.get(userName.uppercase())?.asJsonArray?.map { it.asString }
                            ?.let { addAll(it) }
                    }
                }
                // Displays the UserPreferencesPage and saves updated user preferences to storage when changed.
                UserPreferencesPage(
                    datapointsDisplayed = dataPointsDisplay,
                    chosenDatapoints = chosenDatapoints,
                    onDatapointsChanged = { updatedDatapoints ->
                        // Update user preferences in storage
                        val jsonArray = JsonArray().apply {
                            updatedDatapoints.forEach { add(it) }
                        }
                        UserDataPoints.contents?.remove(userName)
                        UserDataPoints.contents?.add(userName, jsonArray)
                        UserDataPoints.write()
                    },
                    userName = userName
                )
            }
        }
    }
}
