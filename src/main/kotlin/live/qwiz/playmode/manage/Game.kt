package live.qwiz.playmode.manage

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.server.websocket.*
import live.qwiz.database.quiz.Quiz
import live.qwiz.database.quiz.question.Question
import live.qwiz.playmode.socket.gamepacket.s2c.AskQuestionPacket
import live.qwiz.playmode.socket.gamepacket.s2c.base.GameS2CPacket
import live.qwiz.playmode.socket.gamepacket.s2c.part.OptionPart
import live.qwiz.util.HashGenerator

class Game(val quiz: Quiz, val hostId: String, val clientSessions: HashMap<ClientIdentifier, WebSocketServerSession>, var hostSession: WebSocketServerSession? = null) {
    companion object {
        enum class GameState {
            WAITING,
            QUESTION,
            ANSWER,
            RESULT
        }
    }

    private var answers = HashMap<ClientIdentifier, Int>()
    private var currentQuestionIndex = 0
    var gameState = GameState.WAITING

    suspend fun progressGameStage() {
        when (gameState) {
            GameState.WAITING -> {
                gameState = GameState.QUESTION
                askQuestion(quiz.questions.elementAt(currentQuestionIndex))
                currentQuestionIndex++
            }

            GameState.QUESTION -> {
                gameState = GameState.ANSWER
                //TODO: showAnswer
            }

            GameState.ANSWER -> {
                if (currentQuestionIndex < quiz.questions.size) {
                    gameState = GameState.QUESTION
                    askQuestion(quiz.questions.elementAt(currentQuestionIndex))
                    currentQuestionIndex++
                } else {
                    GameState.RESULT
                    //TODO: showResult
                }
            }

            GameState.RESULT -> {}
        }
    }

    fun getNewId(): String {
        var id = HashGenerator.generateHash()

        while(clientSessions.any { it.key.id == id }) {
            id = HashGenerator.generateHash()
        }

        return id
    }

    fun shouldContinue(): Boolean {
        if (hostSession == null && gameState != GameState.WAITING) return false
        return gameState != GameState.RESULT && clientSessions.isNotEmpty()
    }

    private fun allHaveAnswered(): Boolean {
        return clientSessions.size <= answers.size
    }

    fun hasAnswered(clientId: ClientIdentifier): Boolean {
        return answers.containsKey(clientId)
    }

    fun regAnswer(clientId: ClientIdentifier, answer: Int) {
        answers[clientId] = answer
    }

    private suspend fun askQuestion(question: Question) {
        answers.clear()
        val optionParts = question.options.map { OptionPart(it.title, it.description) }
        sendToEach("server_ask_question", AskQuestionPacket(question.title, optionParts))
    }

    private fun Any.ps(): JsonElement {
        return Gson().toJsonTree(this)
    }

    private suspend fun sendToEach(action: String, message: Any) {
        clientSessions.forEach {
            it.value.sendSerialized<GameS2CPacket>(GameS2CPacket(action, message.ps()))
        }
    }

    suspend fun autoProgress() {
        if (gameState == GameState.QUESTION && allHaveAnswered()) {
            progressGameStage()
        }
    }
}