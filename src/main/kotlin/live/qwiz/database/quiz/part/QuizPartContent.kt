package live.qwiz.database.quiz.part

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
open class QuizPartContent {
    open fun validateEdits(): Boolean {
        throw NotImplementedError("This method must be overridden")
    }

    open fun getS2CData(): JsonElement {
        throw NotImplementedError("This method must be overridden")
    }
}