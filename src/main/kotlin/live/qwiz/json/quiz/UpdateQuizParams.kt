package live.qwiz.json.quiz

import live.qwiz.database.quiz.part.QuizPart
import kotlinx.serialization.Serializable

@Serializable
data class UpdateQuizParams(val title: String, val description: String, val parts: HashSet<QuizPart>, val id: String)