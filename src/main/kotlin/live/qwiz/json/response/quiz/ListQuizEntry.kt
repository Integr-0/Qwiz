package live.qwiz.json.response.quiz
import kotlinx.serialization.Serializable

@Serializable
data class ListQuizEntry(val title: String, val description: String, val id: String)