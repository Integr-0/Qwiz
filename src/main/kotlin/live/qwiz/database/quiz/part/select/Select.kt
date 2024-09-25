package live.qwiz.database.quiz.part.select

import kotlinx.serialization.Serializable
import live.qwiz.database.quiz.part.QuizPartContent
import live.qwiz.database.quiz.part.select.option.Option

@Serializable
data class Select(val title: String, val description: String, val options: HashSet<Option>) : QuizPartContent {
    override fun validateEdits(): Boolean {
        for (option in options) {
            if (option.title.isEmpty()) {
                return false
            }
        }

        return title.isNotEmpty() && options.isNotEmpty()
    }

    private data class S2CData(val title: String, val description: String, val options: HashSet<S2COption>)
    private data class S2COption(val title: String, val description: String)

    override fun getS2CData(): Any {
        return S2CData(title, description, options.map { S2COption(it.title, it.hint) }.toHashSet())
    }
}