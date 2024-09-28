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