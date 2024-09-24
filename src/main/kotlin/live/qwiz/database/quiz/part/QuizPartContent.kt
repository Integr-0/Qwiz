package live.qwiz.database.quiz.part

interface QuizPartContent {
    fun validateEdits(): Boolean
    fun getS2CData(): Any
}