package live.qwiz.json.quiz

import live.qwiz.database.quiz.question.Question

data class UpdateQuizParams(val title: String, val description: String, val questions: HashSet<Question>, val id: String)