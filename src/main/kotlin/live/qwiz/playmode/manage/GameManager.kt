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

import kotlinx.coroutines.runBlocking
import live.qwiz.database.quiz.Quiz

class GameManager {
    companion object {
        private val games: HashMap<String, Game> = hashMapOf()
        private val gameProgressionThreads: HashMap<Game, Thread> = hashMapOf()

        fun startNewGame(quiz: Quiz, hostId: String): String {
            val code = generateCode()
            val game = Game(quiz, hostId, hashMapOf())
            games[code] = game
            val thread = Thread {
                while (!game.shouldShutdown()) {
                    if (false) {
                        runBlocking {
                            game.autoProgress()
                        }
                    }

                    Thread.sleep(1000)
                }

                //games.remove(code)
                //gameProgressionThreads.remove(game)
            }

            gameProgressionThreads[game] = thread
            //thread.start()

            return code
        }

        fun resolveGame(code: String): Game? {
            return games[code]
        }

        private fun generateCode(): String {
            var code = (1000..9999).random().toString()

            while (games.containsKey(code)) {
                code = (1000..9999).random().toString()
            }

            return code
        }
    }
}