package live.qwiz.database.quiz.part.select

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import live.qwiz.database.quiz.part.QuizPartContent
import live.qwiz.database.quiz.part.select.option.Option
import live.qwiz.jsonTree

@Serializable
data class Select(val title: String, val description: String, val options: HashSet<Option>) : QuizPartContent() {
    override fun validateEdits(): Boolean {
        for (option in options) {
            if (option.title.isEmpty()) {
                return false
            }
        }

        return title.isNotEmpty() && options.isNotEmpty()
    }

    @Serializable
    private data class S2CData(val title: String, val description: String, val options: HashSet<S2COption>)

    @Serializable
    private data class S2COption(val title: String, val hint: String)

    override fun getS2CData(): JsonElement {
        return S2CData(title, description, options.map { S2COption(it.title, it.hint) }.toHashSet()).jsonTree()
    }
}