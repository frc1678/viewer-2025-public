package org.citruscircuits.viewer.fragments.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.citruscircuits.viewer.RefreshPopup
import org.citruscircuits.viewer.ReloadingItems

/**
 * [Fragment] for the [GroupsPage].
 */
class GroupsFragment : Fragment() {
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
                GroupsPage()
            }
        }
}
