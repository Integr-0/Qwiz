/*
 * Copyright © 2024 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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