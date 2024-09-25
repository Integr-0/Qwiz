package live.qwiz.database.quiz.part

import kotlinx.serialization.json.JsonElement
import live.qwiz.database.quiz.part.select.Select
import live.qwiz.to

class Parts {
    companion object {
        const val SELECT = "select"

        fun parseFromType(type: String, obj: JsonElement): QuizPartContent {
            return when (type) {
                SELECT -> obj.to<Select>()
                else -> throw IllegalArgumentException("Invalid type")
            }
        }
    }
}