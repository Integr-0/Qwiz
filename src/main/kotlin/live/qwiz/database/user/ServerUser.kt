package live.qwiz.database.user

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

@Serializable
data class ServerUser(val name: String, val passwordHash512: String, val salt: String, val id: String, val quizIdSet: HashSet<String>) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): ServerUser = json.decodeFromString(document.toJson())
    }
}