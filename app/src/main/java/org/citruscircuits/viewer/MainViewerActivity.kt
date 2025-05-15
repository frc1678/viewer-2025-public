package org.citruscircuits.viewer

//import org.citruscircuits.viewer.fragments.live_picklist.LivePicklistFragment
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.field_map_popup.view.blue_chip
import kotlinx.android.synthetic.main.field_map_popup.view.chip_group
import kotlinx.android.synthetic.main.field_map_popup.view.close_button
import kotlinx.android.synthetic.main.field_map_popup.view.field_map
import kotlinx.android.synthetic.main.field_map_popup.view.none_chip
import kotlinx.android.synthetic.main.field_map_popup.view.red_chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.citruscircuits.viewer.MainViewerActivity.Companion.matchCache
import org.citruscircuits.viewer.MainViewerActivity.Companion.teamList
import org.citruscircuits.viewer.MainViewerActivity.UserDataPoints.contents
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.constants.Translations
import org.citruscircuits.viewer.data.DataApi
import org.citruscircuits.viewer.fragments.match_schedule.Match
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleMatch
import org.citruscircuits.viewer.data.NotesApi
import org.citruscircuits.viewer.data.updateSavedData
import org.citruscircuits.viewer.fragments.alliance_details.AllianceDetailsFragment
import org.citruscircuits.viewer.fragments.groups.GroupsFragment
import org.citruscircuits.viewer.fragments.match_details.MatchDetailsFragment
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleFragment
import org.citruscircuits.viewer.fragments.pickability.PickabilityFragment
import org.citruscircuits.viewer.fragments.preferences.PreferencesFragment
import org.citruscircuits.viewer.fragments.ranking.RankingFragment
import org.citruscircuits.viewer.fragments.team_details.TeamDetailsFragment
import org.citruscircuits.viewer.fragments.team_list.TeamListFragment
import org.citruscircuits.viewer.fragments.welcome.WelcomeFragment
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream
import java.io.OutputStream
import me.xdrop.fuzzywuzzy.FuzzySearch
import android.widget.Toast
import org.citruscircuits.viewer.fragments.team_comparison.TeamComparisonFragment


/**
 * Main activity class that handles navigation.
 */
class MainViewerActivity : ViewerActivity() {
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        var matchCache = mutableMapOf<String, Match>()
        var teamList = listOf<String>()
        var starredMatches = mutableSetOf<String>()
        val refreshManager = RefreshManager()
        val leaderboardCache = mutableMapOf<String, Leaderboard>()
        var notesCache = mutableMapOf<String, String>()
        var mapMode = 1

        /** Update Viewer Notes locally by pulling from Grosbeak*/
        suspend fun updateNotesCache() {
            val notesList = NotesApi.getAll(Constants.EVENT_KEY)
            notesCache = notesList.toMutableMap()
            Log.d("notes", "updated notes cache")
        }
    }

    /**
     * Overrides the back button to go back to last fragment.
     * Disables the back button and returns nothing when in the startup match schedule.
     */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.container)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        if (supportFragmentManager.backStackEntryCount > 1) supportFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        // Creates the files for user data points and starred matches
        UserDataPoints.read(this)
        StarredMatches.read()

        // Pull the set of starred matches from the downloads file viewer_starred_matches.
        val jsonStarred = StarredMatches.contents.get("starredMatches")?.asJsonArray
        if (jsonStarred != null) {
            for (starred in jsonStarred) {
                starredMatches.add(starred.asString)
            }
        }
    }

    /** Creates the main activity, containing the top app bar, nav drawer, and shows by default the match schedule page*/
    override fun onCreate(savedInstanceState: Bundle?) {
        starredMatches = StarredMatches.citrusMatches.toMutableSet()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        setToolbarText(actionBar, supportActionBar)
        val drawerLayout: DrawerLayout = findViewById(R.id.container)
        val navView: NavigationView = findViewById(R.id.navigation)
        // Defaults
        navView.setCheckedItem(R.id.nav_menu_match_schedule)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // Creates leaderboards for each datapoint for the Rankings page
        (Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS + Constants.FIELDS_TO_BE_DISPLAYED_LFM).forEach {
            if (it !in Constants.CATEGORY_NAMES) createLeaderboard(it)
        }
        // Creates a refresher that will call updateNavFooter() every so often
        refreshManager.addRefreshListener {
            Log.d("data-refresh", "Updated: ranking")
            updateNavFooter()
        }
        if (!Constants.USE_TEST_DATA) {
            lifecycleScope.launch {
                if (this@MainViewerActivity.isNetworkAvailable()) updateNotesCache()
            }
        }
        // Make the back button in the top action bar only go back one screen and not to the first screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Creates fragments
        val welcomeFragment = WelcomeFragment()
        val matchScheduleFragment = MatchScheduleFragment()
        val rankingFragment = RankingFragment()
        //val livePicklistFragment = LivePicklistFragment()
        val pickabilityFragment = PickabilityFragment()
        val teamListFragment = TeamListFragment()
        val teamComparisonFragment = TeamComparisonFragment()
        val allianceDetailsFragment = AllianceDetailsFragment()
        val preferencesFragment = PreferencesFragment()
        updateNavFooter()
        //default screen when the viewer starts (after pulling data) - the welcome page
        supportFragmentManager.beginTransaction().addToBackStack(null)
            .replace(R.id.nav_host_fragment, welcomeFragment, "welcomePage").commit()
        // Listener to open the nav drawer
        container.addDrawerListener(NavDrawerListener(navView, supportFragmentManager, this))
        // Set a listener for each item in the drawer
        navView.setNavigationItemSelectedListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(
                GravityCompat.START
            )
            when (it.itemId) {
                R.id.nav_menu_match_schedule -> {
                    supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, matchScheduleFragment, "matchSchedule")
                        .commit()
                }

                R.id.nav_menu_rankings -> {
                    supportFragmentManager.beginTransaction().addToBackStack(null)
                        .replace(R.id.nav_host_fragment, rankingFragment, "rankings").commit()
                }

//                R.id.nav_menu_picklist -> {
//                    supportFragmentManager.beginTransaction().addToBackStack(null)
//                        .replace(R.id.nav_host_fragment, livePicklistFragment, "picklist").commit()
//                }

                R.id.nav_menu_pickability -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "pickability") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, pickabilityFragment, "pickability").commit()
                }

                R.id.nav_menu_team_list -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "teamList") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, teamListFragment, "teamlist").commit()
                }

                R.id.nav_menu_team_comparison -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "teamcomparisonPage") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, teamComparisonFragment, "teamcomparisonPage")
                        .commit()
                }

                R.id.nav_menu_alliance_details -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "allianceDetails") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, allianceDetailsFragment, "allianceDetails")
                        .commit()
                }

                R.id.nav_menu_groups -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "groups") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, GroupsFragment(), "groups").commit()
                }

                R.id.nav_menu_preferences -> {
                    val ft = supportFragmentManager.beginTransaction()
                    if (supportFragmentManager.fragments.last().tag != "preferences") ft.addToBackStack(
                        null
                    )
                    ft.replace(R.id.nav_host_fragment, preferencesFragment, "preferences").commit()
                }
            }
            true
        }
        lifecycleScope.launch(Dispatchers.IO) { Groups.startListener() }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    /** Inflate the top bar, which includes the field map button */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)

        // Refresh button
        val refreshButton = menu.findItem(R.id.refresh_button)
        refreshButton?.setOnMenuItemClickListener {
            if (ReloadingItems.isLoading.value) {
                Log.d("data-refresh", "Still fetching data")
                ReloadingItems.stillLoading()
            } else {
                ReloadingItems.showLoading()
                lifecycleScope.launch {
                    try {
                        // If it has network, updates the data files
                        if (this@MainViewerActivity.isNetworkAvailable()) updateSavedData()
                        Log.i("data-refresh", "Fetched data from website successfully")
                    } catch (e: Throwable) {
                        Log.e("data-refresh", "Error fetching data: $e")
                    } finally {
                        // Refresh UI on the main thread
                        withContext(Dispatchers.Main) {
                            refreshManager.refresh()
                        }
                    }
                    ReloadingItems.hideLoading()
                    ReloadingItems.loadingComplete()
                }
            }

            true // Indicate that the click was handled
        }

        /** Search Button */
        val searchButton = menu.findItem(R.id.search_button)
        //xml displaying
        searchButton.setOnMenuItemClickListener {
            val popupView = View.inflate(this, R.layout.search_popup, null)
            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            val anim = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            popupView.startAnimation(anim)
            popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.TOP, 0, 50)

            //Query
            val searchInput = popupView.findViewById<AutoCompleteTextView>(R.id.search_input)
            //Team or Match Number
            val numberInput = popupView.findViewById<AutoCompleteTextView>(R.id.number_input)

            //keyboard closing
            numberInput.setOnEditorActionListener { _, _, _ ->
                hideKeyboard(numberInput)
                false
            }
            val searchSubmit = popupView.findViewById<Button>(R.id.search_submit)
            val closeButton = popupView.findViewById<Button>(R.id.close_search_button)

            /** get all datapoint suggestions */
            //filter out datapoints without pages
            val allDisplayedKeys = (
                    Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS +
                            Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED +
                            Constants.FIELDS_TO_BE_DISPLAYED_RANKING +
                            Constants.FIELDS_TO_BE_DISPLAYED_LFM +
                            Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_HEADER_PLAYED +
                            Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_HEADER_NOT_PLAYED
                    ).toSet()

            //All valid Datapoints
            val allSuggestions =
                allDisplayedKeys.mapNotNull { Translations.ACTUAL_TO_HUMAN_READABLE[it] }
            //All Sorted Teams
            val teamSuggestions = teamList.sorted()
            //All Sorted Matches
            val matchSuggestions = matchCache.keys.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }

            // Listener for changes in the number input (team/match number)
            numberInput.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    query: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val input = query.toString()

                    // Get the actual datapoint key based on what's typed in the main search input(Translation -> Constant)
                    val datapointKey = Translations.ACTUAL_TO_HUMAN_READABLE.entries.find {
                        it.value.equals(searchInput.text.toString(), ignoreCase = true)
                    }?.key

                    // Choose which list to filter based on the datapoint type(team or match)
                    val sourceList = when {
                        Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(datapointKey) -> teamSuggestions
                        Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(datapointKey) -> matchSuggestions
                        else -> listOf()
                    }
                    // Filter results that start with the typed number
                    val filtered = sourceList.filter { it.startsWith(input) }

                    // Set the filtered results as suggestions in the dropdown
                    numberInput.setAdapter(
                        android.widget.ArrayAdapter(
                            this@MainViewerActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            filtered
                        )
                    )
                    numberInput.threshold = 0 // Show all suggestions immediately when typing
                    numberInput.showDropDown()
                }
            })

            // Holds the current suggestions for the search input
            val filteredSuggestions = mutableListOf<String>()

            // Adapter that connects filteredSuggestions to the search input box (AutoCompleteTextView)
            val adapter = object : android.widget.ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                filteredSuggestions
            ) {
                override fun getFilter(): android.widget.Filter {
                    return object : android.widget.Filter() {
                        override fun performFiltering(constraint: CharSequence?): FilterResults {
                            // Return the current filteredSuggestions
                            return FilterResults().apply {
                                values = filteredSuggestions
                                count = filteredSuggestions.size
                            }
                        }

                        override fun publishResults(
                            constraint: CharSequence?,
                            results: FilterResults?
                        ) {
                            // Tell the adapter to refresh the dropdown
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            // Hook adapter to the search input box
            searchInput.setAdapter(adapter)

            searchInput.threshold = 1 //starts showing suggestions after 1 character
            //Close keyboard when option selected
            searchInput.setOnItemClickListener { _, _, _, _ ->
                hideKeyboard(searchInput)
                searchInput.dismissDropDown()
            }
            // Run logic every time the user types something
            searchInput.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    query: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    // Convert input to lowercase and split into individual words
                    val queryStr = query.toString().lowercase()
                    val queryWords = queryStr.split(" ")

                    //Substring match based on all query words
                    // Example: typing "auto points" matches "Auto Total Points", "End Auto Points", etc.
                    val substringMatches = allSuggestions.filter { suggestion ->
                        val suggestionLower = suggestion.lowercase()
                        queryWords.all { suggestionLower.contains(it) }
                    }

                    //Fuzzy match (on full string), excluding already matched suggestions
                    // Example: typing "aut totl poits" still finds "Auto Total Points" if it's close enough
                    val fuzzyResults = FuzzySearch.extractSorted(
                        queryStr,
                        allSuggestions - substringMatches.toSet(),
                        70
                    )
                    val fuzzyMatches = fuzzyResults.map { it.string }

                    val startsWithMatches = substringMatches.filter {
                        it.lowercase().startsWith(queryStr)
                    }

                    // These contain the query but don’t start with it
                    val containsMatches = substringMatches - startsWithMatches.toSet()

                    //Combine results
                    val finalSuggestions = startsWithMatches + containsMatches + fuzzyMatches

                    // Update UI
                    filteredSuggestions.clear()
                    filteredSuggestions.addAll(finalSuggestions)
                    adapter.notifyDataSetChanged()
                    searchInput.showDropDown()

                    // Show number box if the datapoint requires it
                    val datapointKey = Translations.ACTUAL_TO_HUMAN_READABLE.entries.find {
                        it.value.equals(query.toString(), ignoreCase = true)
                    }?.key

                    if (datapointKey != null &&
                        (Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(datapointKey) ||
                                Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(
                                    datapointKey
                                ))
                    ) {
                        numberInput.visibility = View.VISIBLE
                        numberInput.hint = when {
                            Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(datapointKey) -> "Enter team number"
                            Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(
                                datapointKey
                            ) -> "Enter match number"

                            else -> "Enter number"
                        }
                    } else {
                        numberInput.visibility = View.GONE
                    }
                }

            })

            // When user hits the submit button on the popup
            searchSubmit.setOnClickListener {
                val query = searchInput.text.toString().trim()
                val number = numberInput.text.toString().trim()
                // Get the actual key from the translation
                val datapointKey = Translations.ACTUAL_TO_HUMAN_READABLE.entries.find {
                    it.value.equals(query, ignoreCase = true)
                }?.key

                val requiresMatchNumber =
                    datapointKey in Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED
                // Input validation
                if (query.isEmpty()) {
                    Toast.makeText(this, "Please enter a datapoint.", Toast.LENGTH_SHORT).show()
                } else if (requiresMatchNumber && number.isEmpty()) {
                    Toast.makeText(this, "Please enter a match number.", Toast.LENGTH_SHORT).show()
                } else {
                    val matchNumber = number.toIntOrNull()
                    // Close everything
                    hideKeyboard(numberInput)

                    this@MainViewerActivity.hideKeyboard()
                    popupWindow.dismiss()

                    // Send to search handler
                    searchDatapoint(
                        context = this@MainViewerActivity,
                        input = query,
                        number = matchNumber
                    )
                }
            }


            closeButton.setOnClickListener {
                hideKeyboard(popupView)

                val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
                popupView.startAnimation(slideUp)

                // Wait until the animation finishes to dismiss
                slideUp.setAnimationListener(object :
                    android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}

                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        popupWindow.dismiss()
                    }
                })
            }


            true
        }


        //Field map button
        val fieldMapItem = menu.findItem(R.id.field_map_button)
        fieldMapItem.setOnMenuItemClickListener {
            val popupView = View.inflate(this, R.layout.field_map_popup, null)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val popupWindow = PopupWindow(popupView, width, height, false)
            popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

            when (mapMode) {
                0 -> {
                    popupView.red_chip.isChecked = true
                    popupView.none_chip.isChecked = false
                    popupView.blue_chip.isChecked = false
                    popupView.field_map.setImageResource(R.drawable.field_red_map_25_min)
                }

                1 -> {
                    popupView.red_chip.isChecked = false
                    popupView.none_chip.isChecked = true
                    popupView.blue_chip.isChecked = false
                    popupView.field_map.setImageResource(R.drawable.field_25_min)
                }

                2 -> {
                    popupView.red_chip.isChecked = false
                    popupView.none_chip.isChecked = false
                    popupView.blue_chip.isChecked = true
                    popupView.field_map.setImageResource(R.drawable.field_blue_map_25_min)
                }
            }

            popupView.red_chip.setOnClickListener { popupView.red_chip.isChecked = true }
            popupView.blue_chip.setOnClickListener { popupView.blue_chip.isChecked = true }
            popupView.none_chip.setOnClickListener { popupView.none_chip.isChecked = true }

            popupView.chip_group.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    popupView.red_chip.id -> {
                        popupView.field_map.setImageResource(R.drawable.field_red_map_25_min)
                        mapMode = 0
                    }

                    popupView.none_chip.id -> {
                        popupView.field_map.setImageResource(R.drawable.field_25_min)
                        mapMode = 1
                    }

                    popupView.blue_chip.id -> {
                        popupView.field_map.setImageResource(R.drawable.field_blue_map_25_min)
                        mapMode = 2
                    }
                }
                return@setOnCheckedChangeListener
            }

            popupView.close_button.setOnClickListener { popupWindow.dismiss() }

            true // Indicate that the click was handled
        }

        return super.onCreateOptionsMenu(menu)
    }


    /** Nav footer that displays when the viewer was last refreshed.  */
    private fun updateNavFooter() {
        val footer = findViewById<TextView>(R.id.nav_footer)
        footer.text = if (Constants.USE_TEST_DATA) getString(R.string.test_data)
        else getString(R.string.last_updated, super.getTimeText())
    }

    /** Object to extract the datapoints of the user preferences file and write to it. */
    object UserDataPoints {
        /** Holds a user's datapoints */
        var contents: JsonObject? = null
        private var gson = Gson()

        /** User preferences file*/
        val file = File(Constants.DOWNLOADS_FOLDER, "viewer_user_data_prefs.json")

        /** Get contents from User Preferences file. This file should always exist unless a mismatch is found, in which the file will be deleted*/
        fun read(context: Context) {
            // Load defaults if no file exists
            if (!fileExists()) copyDefaults(context)
            // Try to pull from file
            try {
                contents = JsonParser.parseReader(FileReader(file)).asJsonObject
            } catch (e: Exception) {
                Log.e("UserDataPoints.read", "Failed to read user datapoints file")
            }
            // Gets user
            val user = contents?.get("selected")?.asString
            // Gets user's datapoints
            val userDataPoints = contents?.get(user)?.asJsonArray
            // If the datapoints exist, check if they match with Constants TEAM / TIM datapoints
            if (userDataPoints != null) {
                for (i in userDataPoints) {
                    if (
                        i.asString !in Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS &&
                        i.asString !in Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED &&
                        i.asString != "See Matches" && i.asString != "TEAM" && i.asString != "TIM"
                    ) {
                        file.delete()
                        // If not, that means someone forgot to update either Constants or User Datapoints defaults
                        Log.e(
                            "UserDataPoints.read",
                            "Datapoint ${i.asString} does not exist in Constants"
                        )
                        // Reset back to defaults
                        copyDefaults(context)
                        break
                    }
                }
            }
        }

        fun write() {
            try {
                val writer = FileWriter(file, false)
                gson.toJson(contents as JsonElement, writer)
                writer.close()
                Log.d(
                    "UserDataPoints",
                    "Successfully wrote contents to file: ${contents.toString()}"
                )
            } catch (e: Exception) {
                Log.e("UserDataPoints", "Failed to write to file: $e")
            }
        }

        /** Check if user preferences file exists*/
        private fun fileExists(): Boolean = file.exists()

        /**Copies the default preferences to the User Preferences file*/
        fun copyDefaults(context: Context) {
            // Read from user preferences file
            val inputStream: InputStream = context.resources.openRawResource(R.raw.default_prefs)
            try {
                // Copies over the contents of the defaults (inputStream) to the file
                val outputStream: OutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len: Int?
                while (inputStream.read(buffer, 0, buffer.size).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len!!)
                }
                inputStream.close()
                outputStream.close()
                try {
                    // Add the other default elements to the file
                    contents = JsonParser.parseReader(FileReader(file)).asJsonObject
                    contents?.remove("key")
                    contents?.addProperty("key", Constants.DEFAULT_KEY)
                    contents?.remove("schedule")
                    contents?.addProperty("schedule", Constants.DEFAULT_SCHEDULE)
                    contents?.remove("default_key")
                    contents?.addProperty("default_key", Constants.DEFAULT_KEY)
                    contents?.remove("default_schedule")
                    contents?.addProperty("default_schedule", Constants.DEFAULT_SCHEDULE)
                    write()
                } catch (e: Exception) {
                    Log.e("UserDataPoints.read", "Failed to read user datapoints file")
                }
            } catch (e: Exception) {
                Log.e("copyDefaults", "Failed to copy default preferences to file, $e")
            }
        }
    }

    /** Object to handle the main app data from saved JSON files */
    object MainAppData {
        /** Holds the app data */
        var contents: JsonObject? = null
        private var gson = Gson()

        /** Main data folder for all data files */
        private val mainDataFolder = File(Constants.DOWNLOADS_FOLDER, "main_viewer_app_data")

        /** Event key */
        val eventKey: String = UserDataPoints.contents?.get("key")!!.asString

        /** Main data folder for a given event key */
        val eventSpecificDataFolder =
            File(mainDataFolder, "${eventKey}_main_viewer_app_data")

        /** Main data file for a given event key */
        val mainDataFile = File(eventSpecificDataFolder, "${eventKey}_viewer_main_app_data.json")

        /** Match schedule file for a given event key */
        val matchScheduleFile = File(eventSpecificDataFolder, "${eventKey}_match_schedule.json")

        /** Team list file for a given event key */
        val teamListFile = File(eventSpecificDataFolder, "${eventKey}_team_list.json")

        /** Stand Strat data folder for a given event key */
        val standStratDataFolder = File(eventSpecificDataFolder, "${eventKey}_stand_strat_data")

        /** Usernames of all stand strategists for a given event key */
        val standStratUsernamesFile =
            File(standStratDataFolder, "${eventKey}_stand_strat_usernames.json")


        val robotImagesFolder = File(eventSpecificDataFolder, "${eventKey}_robot_images")

        /** Get contents from the main data file. */
        fun readViewerDataFromJson(): DataApi.ViewerData {
            try {
                // tries to read from the file
                val jsonString = mainDataFile.readText()
                val newData = Json.decodeFromString<DataApi.ViewerData>(jsonString)
                return newData
            } catch (e: Exception) {
                Log.e("readViewerDataFromJson", "Error reading JSON from file: ${e.message}", e)
            }
            return DataApi.ViewerData(emptyMap(), emptyMap(), emptyMap(), emptyList(), emptyMap())
        }

        /** Creates folders if they do not exist */
        fun createFolder() {
            // Checks to see if each folder exists. If not, it creates it.
            if (!mainDataFolder.exists()) {
                mainDataFolder.mkdir()
            }
            if (!eventSpecificDataFolder.exists()) {
                eventSpecificDataFolder.mkdir()
            }
            if (!standStratDataFolder.exists()) {
                standStratDataFolder.mkdir()
            }
            if (!robotImagesFolder.exists()) {
                robotImagesFolder.mkdir()
            }
            StartupActivity.databaseReference?.let {
                writeStringDataToJson(mainDataFile, Json.encodeToString(it))
            }
        }

        /** Get contents from the match schedule file. */
        fun getMatchScheduleFromJson(): MutableMap<String, MatchScheduleMatch> {
            try {
                // tries to read from the file
                val jsonString = matchScheduleFile.readText()
                val newMatchSchedule =
                    Json.decodeFromString<MutableMap<String, MatchScheduleMatch>>(jsonString)
                // iterates through each match and assigns an alliance color to each team
                for (i in newMatchSchedule) {
                    val match = Match(i.key)
                    for (j in i.value.teams) {
                        when (j.color) {
                            "red" -> match.redTeams.add(j.number)
                            "blue" -> match.blueTeams.add(j.number)
                        }
                    }
                    matchCache[i.key] = match
                }
                matchCache =
                    matchCache.toList().sortedBy { (_, v) -> v.matchNumber.toInt() }
                        .toMap().toMutableMap()
                return newMatchSchedule
            } catch (e: Exception) {
                Log.e("readMatchScheduleFromJson", "Error reading JSON from file: ${e.message}", e)
            }
            return mutableMapOf()
        }

        /** Get contents from the team list. */
        fun readTeamListJson(): List<String> {
            try {
                // tries to read from the file
                val jsonString = teamListFile.readText()
                val newData = Json.decodeFromString<List<String>>(jsonString)
                return newData
            } catch (e: Exception) {
                Log.e("readTeamListFromJson", "Error reading JSON from file: ${e.message}", e)
            }
            return listOf()
        }

        /** Get contents from the stand strat username file. */
        fun getStandStratUsernames(): List<String> {
            try {
                // tries to read from the file
                val jsonString = standStratUsernamesFile.readText().ifEmpty { "" }
                val newData = Json.decodeFromString<List<String>>(jsonString)
                val validNames = mutableListOf<String>()
                for (username in newData) {
                    // changes all spaces in a username into underscores, because file names can't contain spaces
                    validNames.add((username.replace(" ", "_")))
                }
                StartupActivity.standStratUsernames = validNames
                return validNames
            } catch (e: Exception) {
                Log.e("readStandStratDataFromJson", "Error reading JSON from file: ${e.message}", e)
            }
            return listOf()
        }

        /** Writes a string to a JSON file
         * @param file - the filepath of the file being written to
         * @param newData - the string data being written to the file
         * */
        fun writeStringDataToJson(file: File, newData: String) {
            file.writeText(newData)
        }

        /** Creates a folder or empty files if they don't exist */
        fun create() {
            createFolder()
            for (file in listOf(
                mainDataFile,
                matchScheduleFile,
                teamListFile,
                standStratUsernamesFile
            )) {
                if (!file.exists()) file.createNewFile()
            }
        }

        /** Check if a given file for the given event key exists and is not empty
         * @param file - the file being checked */
        fun isValid(file: File): Boolean {
            if (file.exists()) {
                val jsonString = file.readText()
                if (jsonString.isNotEmpty()) {
                    return true
                } else {
                    Log.d("fileValidity", "$file is empty")
                    return false
                }
            } else
                return file.exists()
        }
    }

    /**
     * Writes file to store the starred matches on the viewer
     */
    object StarredMatches {
        var contents = JsonObject()
        private var gson = Gson()

        // Creates a list that stores all the match numbers that team 1678 is in
        val citrusMatches = matchCache.filter {
            return@filter it.value.blueTeams.contains("1678") or it.value.redTeams.contains("1678")
        }.map { return@map it.value.matchNumber }

        private val file = File(
            MainAppData.eventSpecificDataFolder,
            "${MainAppData.eventKey}_starred_matches.json"
        )

        fun read() {
            if (!fileExists()) write()
            try {
                contents = JsonParser.parseReader(FileReader(file)).asJsonObject
            } catch (e: Exception) {
                Log.e("StarredMatches.read", "Failed to read starred matches file")
            }
        }

        private fun write() {
            val writer = FileWriter(file, false)
            gson.toJson(contents as JsonElement, writer)
            writer.close()
        }

        private fun fileExists(): Boolean = file.exists()

        /**
         * Updates the file with the currently starred matches based on the companion object starredMatches
         */
        fun input() {
            val starredJsonArray = JsonArray()
            for (starred in starredMatches) starredJsonArray.add(starred)
            contents.remove("starredMatches")
            contents.add("starredMatches", starredJsonArray)
            write()
        }

    }


    /**
     * An object to read/write the starred teams file with.
     */
    object StarredTeams {
        private val gson = Gson()
        private val teams = mutableSetOf<String>()

        fun add(team: String) {
            teams.add(team)
            write()
        }

        fun remove(team: String) {
            teams.remove(team)
            write()
        }

        fun contains(team: String) = teams.contains(team)

        private val file =
            File(MainAppData.eventSpecificDataFolder, "${MainAppData.eventKey}_starred_teams.json")

        fun read() {
            if (!file.exists()) write()
            try {
                JsonParser.parseReader(FileReader(file)).asJsonArray.forEach { teams.add(it.asString) }
            } catch (e: Exception) {
                Log.e("StarredTeams.read", "Failed to read starred teams file")
            }
        }

        private fun write() {
            val writer = FileWriter(file, false)
            gson.toJson(teams, writer)
            writer.close()
        }
    }

    // Navigates to the correct fragment based on the selected datapoint and optional match/team number
    fun navigateToSearchResult(
        selectedTerm: String,
        matchNumber: Int? = null,
        teamNumber: String? = null
    ) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        when {
            // Rankings
            Constants.FIELDS_TO_BE_DISPLAYED_RANKING.contains(selectedTerm) -> {
                val fragment = RankingFragment().apply {
                    arguments = Bundle().apply {
                        putString(Constants.TEAM_NUMBER, teamNumber)
                    }
                }
                transaction.replace(R.id.nav_host_fragment, fragment, "rankings")
            }


            // Team Details
            Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(selectedTerm) -> {
                val teamNum = teamNumber ?: "1678"
                val teamFragment = TeamDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString(Constants.TEAM_NUMBER, teamNum)
                        putBoolean("LFM", false)
                        putString("targetDatapoint", selectedTerm) //scroll target
                    }
                }
                transaction.replace(R.id.nav_host_fragment, teamFragment, "teamDetails")
            }

            // Match Details
            Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(selectedTerm) -> {
                val matchNum = matchNumber ?: 1
                val matchFragment = MatchDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putInt(Constants.MATCH_NUMBER, matchNum)
                        putString("targetDatapoint", selectedTerm)
                    }
                }

                transaction.replace(R.id.nav_host_fragment, matchFragment, "matchDetails")
            }

            // Fallback
            else -> {
                transaction.replace(R.id.nav_host_fragment, TeamListFragment(), "teamList")
            }
        }

        transaction.addToBackStack(null)
        transaction.commit()
    }

    // Handles search navigation based on the selected datapoint and optional number input
    fun searchDatapoint(context: Context, input: String, number: Int? = null) {
        val humanToActualMap = Translations.ACTUAL_TO_HUMAN_READABLE.entries
            .associateBy({ it.value.lowercase() }, { it.key })

        val lowercaseInput = input.lowercase()

        // Check if the input matches a known datapoint
        if (humanToActualMap.containsKey(lowercaseInput)) {
            val selectedTerm = humanToActualMap[lowercaseInput] ?: return
            // If the datapoint is team-related, expect a team number
            val teamNumber =
                if (Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS.contains(selectedTerm)) number?.toString() else null
            // If the datapoint is match-related, expect a match number
            val matchNumber =
                if (Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED.contains(selectedTerm)) number else null

            // Validate match number if provided
            if (matchNumber != null && !matchCache.containsKey(matchNumber.toString())) {
                Toast.makeText(context, "Match $matchNumber not found.", Toast.LENGTH_SHORT).show()
                return
            }

            // Validate team number if provided
            if (teamNumber != null && !teamList.contains(teamNumber)) {
                Toast.makeText(context, "Team $teamNumber not found.", Toast.LENGTH_SHORT).show()
                return
            }

            val user =
                MainViewerActivity.UserDataPoints.contents?.get("selected")?.asString?.uppercase()
                    ?: "OTHER"
            val userPrefs = MainViewerActivity.UserDataPoints.contents?.get(user)?.asJsonArray

            // If the datapoint the user searched for is not in their preferences list, show a prompt.
            if (userPrefs == null || !userPrefs.any { it.asString == selectedTerm }) {
                AlertDialog.Builder(context)
                    .setTitle("Datapoint Not Enabled")
                    .setMessage("This datapoint is not selected in user preferences. Do you want to enable it?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Add the datapoint and save
                        val updatedList =
                            (userPrefs?.map { it.asString } ?: listOf()) + selectedTerm

                        // Sort the updated list by how it's ordered in the display list
                        val referenceOrder = Constants.FIELDS_TO_BE_DISPLAYED_TEAM_DETAILS +
                                Constants.FIELDS_TO_BE_DISPLAYED_MATCH_DETAILS_PLAYED

                        val sortedList =
                            updatedList.distinct().sortedBy { referenceOrder.indexOf(it) }

                        val sortedJsonArray = JsonArray().apply {
                            sortedList.forEach { add(it) }
                        }

                        // Save the new preferences to disk
                        MainViewerActivity.UserDataPoints.contents?.add(user, sortedJsonArray)
                        MainViewerActivity.UserDataPoints.write()

                        // Navigate to the search result now that the datapoint is enabled
                        (context as? MainViewerActivity)?.navigateToSearchResult(
                            selectedTerm,
                            matchNumber,
                            teamNumber
                        )
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Close out if No
                    }
                    .show()
                return
            }

            // If all checks pass, navigate to the appropriate page
            (context as? MainViewerActivity)?.navigateToSearchResult(
                selectedTerm,
                matchNumber,
                teamNumber
            )
        } else {
            // If no valid datapoint was found
            Toast.makeText(context, "Datapoint not found.", Toast.LENGTH_SHORT).show()
        }
    }


}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

/** Class for creating listeners for all fragments (manages state changes)*/
class NavDrawerListener(
    private val navView: NavigationView,
    private val fragManager: FragmentManager,
    private val activity: Activity
) : DrawerLayout.DrawerListener {
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
    override fun onDrawerOpened(drawerView: View) = activity.hideKeyboard()

    override fun onDrawerClosed(drawerView: View) {}
    override fun onDrawerStateChanged(newState: Int) {
        if (newState == ViewDragHelper.STATE_SETTLING) {
            when (fragManager.fragments.last().tag) {
                "matchSchedule" -> navView.setCheckedItem(R.id.nav_menu_match_schedule)
                "rankings" -> navView.setCheckedItem(R.id.nav_menu_rankings)
//                "picklist" -> navView.setCheckedItem(R.id.nav_menu_picklist)
                "pickability" -> navView.setCheckedItem(R.id.nav_menu_pickability)
                "teamList" -> navView.setCheckedItem(R.id.nav_menu_team_list)
                "teamcomparisonPage" -> navView.setCheckedItem(R.id.nav_menu_team_comparison)
                "allianceDetails" -> navView.setCheckedItem(R.id.nav_menu_alliance_details)
                "groups" -> navView.setCheckedItem(R.id.nav_menu_groups)
                "preferences" -> navView.setCheckedItem(R.id.nav_menu_preferences)
            }
        }
    }
}