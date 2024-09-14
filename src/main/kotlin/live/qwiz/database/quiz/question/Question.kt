package live.qwiz.database.quiz.question

import kotlinx.serialization.Serializable
import live.qwiz.database.quiz.question.option.Option
import live.qwiz.util.HashGenerator

@Serializable
data class Question(val title: String, val description: String, val options: HashSet<Option>)