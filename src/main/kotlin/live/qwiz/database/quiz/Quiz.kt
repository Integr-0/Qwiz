package live.qwiz.database.quiz

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import live.qwiz.database.quiz.question.Question
import live.qwiz.util.HashGenerator
import org.bson.Document

@Serializable
data class Quiz(val title: String, val description: String, val authorId: String, val questions: HashSet<Question>, val id: String) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Quiz = json.decodeFromString(document.toJson())
    }
}