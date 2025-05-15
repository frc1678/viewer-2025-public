package org.citruscircuits.viewer.fragments.welcome

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.MainViewerActivity.StarredMatches
import org.citruscircuits.viewer.MainViewerActivity.StarredTeams
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleFragment
import java.util.Locale

/**
 * The activity that greets the user and asks them to choose a profile.
 */
class WelcomeFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        Constants.DOWNLOADS_FOLDER =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Create/read the user profile file, the starred matches file, and the starred teams file
        return ComposeView(requireContext()).apply {
            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                val context = LocalContext.current
                UserDataPoints.read(context)
                Log.d("UserDataPoints.read", UserDataPoints.contents.toString())
                StarredMatches.read()
                StarredTeams.read()
                //list of all possible users
                val usernames = listOf(
                    "Other",
                    "Austin",
                    "Mike",
                    "Richard",
                    "Mehul",
                    "Steve",
                    "Matt",
                    "Universal"
                )
                WelcomePage(
                    users = usernames,
                    //get datapoints for the chosen user
                    onContinue = { thisSelectedUser ->
                        val selectedUser = thisSelectedUser.uppercase(locale = Locale.ROOT)
                        with(UserDataPoints) {
                            contents?.remove("selected")
                            contents?.addProperty("selected", selectedUser)
                            write()
                        }
                    },
                    //Move on to match schedule after choosing user
                    onOpenMatchSchedule = {
                        parentFragmentManager.beginTransaction().addToBackStack(null)
                            .replace(
                                (requireView().parent as ViewGroup).id,
                                MatchScheduleFragment()
                            )
                            .commit()
                    }
                )
            }
        }
    }
}