package live.qwiz.database.quiz.question.option

import kotlinx.serialization.Serializable

@Serializable
data class Option(val title: String, val description: String, val isCorrect: Boolean)