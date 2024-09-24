package live.qwiz.json.quiz
import kotlinx.serialization.Serializable

@Serializable
data class CreateQuizParams(val title: String, val description: String)