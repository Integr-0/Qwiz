package live.qwiz.json.quiz
import kotlinx.serialization.Serializable

@Serializable
data class DeleteQuizParams(val id: String)