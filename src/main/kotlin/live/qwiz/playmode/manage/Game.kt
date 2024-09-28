package live.qwiz.playmode.manage

import io.ktor.server.websocket.*
import live.qwiz.database.quiz.Quiz
import live.qwiz.database.quiz.part.QuizPart
import live.qwiz.playmode.socket.gamepacket.s2c.base.GameS2CPacket
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
                askQuestion(quiz.parts.elementAt(currentQuestionIndex))
                currentQuestionIndex++
            }

            GameState.QUESTION -> {
                gameState = GameState.ANSWER
                //TODO: showAnswer
            }

            GameState.ANSWER -> {
                if (currentQuestionIndex < quiz.parts.size) {
                    gameState = GameState.QUESTION
                    askQuestion(quiz.parts.elementAt(currentQuestionIndex))
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
        return gameState != GameState.RESULT
    }

    fun shouldShutdown(): Boolean {
        if (hostSession == null && gameState != GameState.WAITING) return true
        if (clientSessions.isEmpty() && gameState != GameState.WAITING) return true
        return false
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

    private suspend fun askQuestion(part: QuizPart) {
        answers.clear()

        sendToEach(part.getS2CQuestionPacket())
    }

    private suspend fun sendToEach(message: GameS2CPacket) {
        clientSessions.forEach {
            it.value.sendSerialized<GameS2CPacket>(message)
        }
    }

    suspend fun autoProgress() {
        if (gameState == GameState.QUESTION && allHaveAnswered()) {
            progressGameStage()
        }
    }
}