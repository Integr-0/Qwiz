package live.qwiz.database.quiz.part.select.option

import kotlinx.serialization.Serializable

@Serializable
data class Option(val title: String, val hint: String, val isCorrect: Boolean)