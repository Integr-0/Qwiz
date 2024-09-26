package live.qwiz.session.account

import kotlinx.coroutines.runBlocking
import live.qwiz.database.MainDatabaseHandler
import live.qwiz.database.user.ServerUser
import live.qwiz.util.HashGenerator

class AccountSessionManager {
    companion object {
        private val sessions: HashMap<AccountSession, String> = hashMapOf()

        private fun generateSessionId(): String {
            var hash = HashGenerator.generateHash()

            while (containsSessionId(hash)) {
                hash =  generateSessionId()
            }

            return hash
        }

        fun createSession(user: ServerUser): AccountSession {
            val sessionId = generateSessionId()
            val session = AccountSession(sessionId)
            sessions[session] = user.id
            return session
        }

        fun resolveUser(session: AccountSession): ServerUser? {
            return runBlocking {
                MainDatabaseHandler.getUserDatabaseService().getById(sessions[session]!!)
            }
        }

        fun resolveUser(sessionId: String): ServerUser? {
            return runBlocking {
                val session = sessions.filter { it.key.sessionId == sessionId }.keys.firstOrNull()
                MainDatabaseHandler.getUserDatabaseService().getById(sessions[session]!!)
            }
        }

        private fun containsSessionId(sessionId: String): Boolean {
            return sessions.keys.any { it.sessionId == sessionId }
        }

        fun removeSession(session: AccountSession) {
            sessions.remove(session)
        }
    }
}