package org.citruscircuits.viewer.data

//import org.citruscircuits.viewer.fragments.offline_picklist.PicklistData
import android.annotation.SuppressLint
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toByteArray
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.citruscircuits.viewer.MainViewerActivity.MainAppData.robotImagesFolder
import org.citruscircuits.viewer.constants.KESTREL_KEY
import org.citruscircuits.viewer.fragments.auto_paths.AutoPath
import org.citruscircuits.viewer.fragments.match_schedule.MatchScheduleMatch
import java.io.File
import java.net.Inet4Address

// API docs are at https://kestrel.1678doozer.net/docs#
const val apiUrl = "https://kestrel.1678doozer.net"
val teamCollections = arrayOf(
    "obj_team",
    "tba_team",
    "subj_team",
    "predicted_team",
    "pickability",
    "raw_obj_pit",
    "picklist",
    "ss_team"
)
val timCollections = arrayOf(
    "obj_tim",
    "subj_tim",
    "tba_tim",
    "ss_tim"
)

class Ipv4OnlyDns : Dns {
    override fun lookup(hostname: String) =
        Dns.SYSTEM.lookup(hostname).sortedBy { it !is Inet4Address }
}

// Creates a client for the http request
val client = HttpClient(OkHttp) {

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    engine {
        preconfigured = OkHttpClient.Builder().dns(Ipv4OnlyDns()).build()
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.d("Ktor", message)
            }
        }
        level = LogLevel.ALL
    }
    // Sets the timeout to be 60 seconds
    install(HttpTimeout) {
        requestTimeoutMillis = 60 * 1000//900 * 1000 for champs
        connectTimeoutMillis = 60 * 1000//900 * 1000 for champs
        socketTimeoutMillis = 60 * 1000//900 * 1000 for champs
    }
    defaultRequest {
        // there should be a file in the constants directory called kestrelKey.kt with the api key
        header("Kestrel-API-Key", KESTREL_KEY)
    }
}

// Gets the live picklist data from Kestrel and updates live picklist
//object PicklistApi {
//    suspend fun getPicklist(eventKey: String? = null): PicklistData = client.get("$apiUrl/picklist/rest/list") {
//        if (eventKey != null) parameter("event_key", eventKey)
//    }.body()
//
//    // Sets the data in Kestrel to the new live picklist data
//    suspend fun setPicklist(picklist: PicklistData, password: String, eventKey: String? = null): PicklistSetResponse =
//        client.put("$apiUrl/picklist/rest/list") {
//            parameter("password", password)
//            if (eventKey != null) parameter("event_key", eventKey)
//            contentType(ContentType.Application.Json)
//            setBody(picklist)
//        }.body()

@Serializable(with = PicklistSetSerializer::class)
sealed class PicklistSetResponse {
    @Serializable
    data class Success(val deleted: Int) : PicklistSetResponse()

    @Serializable
    data class Error(val error: String) : PicklistSetResponse()
}

object PicklistSetSerializer :
    JsonContentPolymorphicSerializer<PicklistSetResponse>(PicklistSetResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out PicklistSetResponse> =
        when {
            element.jsonObject.containsKey("error") -> PicklistSetResponse.Error.serializer()
            element.jsonObject.containsKey("deleted") -> PicklistSetResponse.Success.serializer()
            else -> throw IllegalArgumentException("Unknown response type")
        }
}

object DataApi {

    /**
     * @return The team list from Kestrel.
     */
    suspend fun getTeamList(eventKey: String): List<String> =
        client.get("$apiUrl/tba/team_list/$eventKey").body()

    /**
     * @return The match schedule from Kestrel.
     */
    suspend fun getMatchSchedule(eventKey: String): MutableMap<String, MatchScheduleMatch> =
        client.get("$apiUrl/tba/match_schedule/$eventKey").body()

    suspend fun getViewerData(eventKey: String?): ViewerData {
        // fetch data for every team collection
        // teamData will have team numbers as keys and the value is a JsonObject containing ALL of the team data for that team
        // team data collections are defined at the top of the file
        val teamData: MutableMap<String, JsonObject> = mutableMapOf()
        for (collection in teamCollections) {
            val data: MutableMap<String, JsonObject> =
                client.get("$apiUrl/database/team/$eventKey/$collection").body()
            // update the teamData with the team data from this collection
            for (team in data.keys) {
                if (!teamData.containsKey(team)) {
                    // If the overall teamData doesn't already have data for this team, initialize the data
                    teamData[team] = data[team] ?: JsonObject(mapOf())
                } else {
                    // update the existing teamData with the data from this collection
                    teamData[team] = JsonObject(teamData[team]!!.toMutableMap().apply {
                        for (datapoint in data[team]!!.keys) {
                            data[team]!![datapoint]?.let { put(datapoint, it) }
                        }
                    })
                }
            }
        }
        // fetch data for every tim collection
        // timData will have match numbers as keys and the value is a map of team numbers to the tim corresponding to that team in that match
        // tim data collections are also defined at the top of the file
        val timData: MutableMap<String, MutableMap<String, JsonObject>> = mutableMapOf()
        for (collection in timCollections) {
            val data: MutableMap<String, MutableMap<String, JsonObject>> =
                client.get("$apiUrl/database/tim/$eventKey/$collection").body()
            // update the timData with the tim data from this collection
            for (match in data.keys) {
                if (!timData.containsKey(match)) {
                    // If the tim data doesn't already have this match in it, initialize timData[match]
                    timData[match] = data[match] ?: mutableMapOf()
                } else {
                    // need to manually go through and update every team and datapoint
                    for (team in data[match]!!.keys) {
                        if (timData[match]?.containsKey(team) == false) {
                            // if there isn't already a tim with that match and team, initialize it
                            timData[match]?.set(team, data[match]?.get(team) ?: JsonObject(mapOf()))
                        } else {
                            // otherwise, update the existing tim data
                            timData[match]!![team] =
                                JsonObject(timData[match]!![team]!!.toMutableMap().apply {
                                    for (datapoint in data[match]!![team]!!.keys) {
                                        data[match]!![team]!![datapoint]?.let { put(datapoint, it) }
                                    }
                                })
                        }
                    }
                }
            }
        }
        // fetch predicted_aim data
        val aimData: Map<String, AimData> =
            client.get("$apiUrl/database/predicted_aim/$eventKey").body()
        val autoPathsData: Map<String, Map<String, AutoPath>> =
            client.get("$apiUrl/database/auto_paths/$eventKey").body()
        val elimsAllianceData: List<ElimAlliance> =
            client.get("$apiUrl/database/raw/$eventKey/predicted_alliances").body()
        return ViewerData(
            team = teamData,
            tim = timData,
            aim = aimData,
            alliances = elimsAllianceData,
            auto_paths = autoPathsData
        )
    }

    @Suppress("PropertyName")
    @Serializable
    data class ViewerData(
        val team: Map<String, JsonObject>,
        val tim: Map<String, Map<String, JsonObject>>,
        val aim: Map<String, AimData>,
        val alliances: List<ElimAlliance>,
        val auto_paths: Map<String, Map<String, AutoPath>>
    )

    @Serializable
    data class AimData(val red: JsonObject? = null, val blue: JsonObject? = null)

    @Serializable
    data class ElimAlliance(
        @SerialName("alliance_num") val allianceNum: Int,
        val picks: List<String>
    )
}

object NotesApi {
    suspend fun getAll(eventKey: String?): Map<String, String> =
        client.get("$apiUrl/database/notes/$eventKey") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun get(eventKey: String?, team: String): NoteData =
        client.get("$apiUrl/database/notes/$eventKey/$team") {
            contentType(ContentType.Application.Json)
        }.body<NoteData>()

    suspend fun set(eventKey: String?, team: String, notes: String) =
        client.put("$apiUrl/database/notes/$eventKey/$team") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("note" to notes))
        }

    @Serializable
    data class NoteData(@SerialName("team_number") val teamNumber: String, val notes: String)
}

object PitImagesApi {
    /** Fetch images from the cloud db */
    suspend fun fetchImages(eventKey: String) {
        val imageNameList: List<String> = client.get(
            "$apiUrl/database/pit_collection/image_list/$eventKey"
        ).body()
        for (imageName in imageNameList) {
            val imageFile = File(robotImagesFolder, imageName)
            if (imageFile.exists()) {
                continue
            }
            val image = client.get(
                "$apiUrl/database/pit_collection/images/$eventKey/$imageName",
            ) {
                accept(ContentType.Image.JPEG)
            }.body<ByteReadChannel>()
            imageFile.writeBytes(image.toByteArray())
        }
    }
}