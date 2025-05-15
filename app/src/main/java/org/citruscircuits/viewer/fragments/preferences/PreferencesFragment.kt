package org.citruscircuits.viewer.fragments.preferences

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems
import org.citruscircuits.viewer.MainViewerActivity
import org.citruscircuits.viewer.fragments.user_preferences.UserPreferencesFragment

/**
 * Page for editing app preferences.
 */
class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        // embed compose content in the fragment
        ComposeView(requireContext()).apply {
            setContent {
                RefreshPopup()
                if (ReloadingItems.finished.value) {
                    RefreshPopup()
                }
                PreferencesPage(
                    onOpenUserPreferences = {
                        // navigate to fragment
                        parentFragmentManager.beginTransaction().addToBackStack(null)
                            .replace(
                                (requireView().parent as ViewGroup).id,
                                UserPreferencesFragment()
                            )
                            .commit()
                    },
                    onRestart = {
                        // restart activity
                        with(MainViewerActivity.UserDataPoints) {
                            write() // Ensure data is saved before restart
                        }
                        context.startActivity(
                            Intent.makeRestartActivityTask(
                                context?.packageManager?.getLaunchIntentForPackage(context.packageName)!!.component
                            )
                        )
                        Runtime.getRuntime().exit(0)

                    }
                )
            }
        }
}
