@file:Suppress("DuplicatedCode")

package live.qwiz.routing

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import live.qwiz.database.MainDatabaseHandler
import live.qwiz.database.quiz.Quiz
import live.qwiz.database.user.ServerUser
import live.qwiz.json.quiz.CreateQuizParams
import live.qwiz.json.AuthParams
import live.qwiz.json.quiz.DeleteQuizParams
import live.qwiz.json.quiz.UpdateQuizParams
import live.qwiz.json.response.quiz.ListQuizEntry
import live.qwiz.playmode.json.StartGameParams
import live.qwiz.playmode.manage.ClientIdentifier
import live.qwiz.playmode.manage.Game
import live.qwiz.playmode.manage.GameManager
import live.qwiz.playmode.socket.gamepacket.c2s.JoinGamePacket
import live.qwiz.playmode.socket.gamepacket.s2c.JoinGameResponsePacket
import live.qwiz.playmode.socket.gamepacket.c2s.AnswerPacket
import live.qwiz.playmode.socket.gamepacket.c2s.HostProgressGamePacket
import live.qwiz.playmode.socket.gamepacket.c2s.base.GameC2SPacket
import live.qwiz.session.account.AccountSession
import live.qwiz.session.account.AccountSessionManager
import live.qwiz.util.HashGenerator
import java.io.File
import kotlin.math.min

/*
    Dummy User:
    Username: TestUser
    Password: test
    PasswordHash: ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff
 */

fun Application.handleRouting() {
    routing {
        staticResources("/static", "/pages/static")
        handleAccount()

        handleQuiz()

        handleNonApiQuiz()

        handlePlayMode()

        get("/") {
            val serverUser = call.tryGetUser()
            if (serverUser == null) {
                call.respondFile(File("src/main/resources/pages/main_page.html"))
                return@get
            }

            call.respondRedirect("/dashboard")
        }

        get("/login") {
            val serverUser = call.tryGetUser()
            if (serverUser == null) {
                call.respondFile(File("src/main/resources/pages/login.html"))
                return@get
            }

            call.respondRedirect("/dashboard")
        }

        get("/signup") {
            val serverUser = call.tryGetUser()
            if (serverUser == null) {
                call.respondFile(File("src/main/resources/pages/signup.html"))
                return@get
            }

            call.respondRedirect("/dashboard")
        }

        get("/dashboard") {
            val serverUser = call.tryGetUser()
            if (serverUser == null) {
                call.respondRedirect("/login")
                return@get
            }

            call.respondFile(File("src/main/resources/pages/dashboard.html"))
        }

        get("/status") {
            call.respondFile(File("src/main/resources/pages/status.html"))
        }
    }
}

fun Routing.handlePlayMode() {
    post("/api/game/start") {
        val serverUser = call.tryGetUserForApiCall() ?: return@post

        val startParams = call.receive<StartGameParams>()

        val quiz = call.tryGetQuizForEditsForApiCall(startParams.id, serverUser.id) ?: return@post
        if (quiz.questions.isEmpty()) {
            call.respondLog("Quiz has no questions!", HttpStatusCode.BadRequest)
            return@post
        }

        val code = GameManager.startNewGame(quiz, serverUser.id)

        call.respondLog(code)
    }

    webSocket("/api/game/host_socket/{code}") {
        val gameCode = call.parameters["code"] ?: return@webSocket
        val game = GameManager.resolveGame(gameCode) ?: return@webSocket
        val user = call.tryGetUserForApiCall() ?: return@webSocket

        if (game.hostId != user.id) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "You are not the host!"))
            return@webSocket
        }

        game.hostSession = this

        while (true) {
            try {
                val controlPacket = receiveDeserialized<GameC2SPacket>()
                when (controlPacket.action) {
                    "host_progress" -> {
                        game.progressGameStage()
                    }
                }
            } catch (e: Exception) {
                game.hostSession = null
                break
            }
        }
    }

    webSocket("/api/game/client_socket/{code}") {
        val gameCode = call.parameters["code"] ?: return@webSocket
        val game = GameManager.resolveGame(gameCode) ?: return@webSocket

        while (true) {
            try {
                if (game.clientSessions.values.contains(this)) {
                    // handle game
                    val identifier = game.clientSessions.filter { it.value == this }.keys.first()

                    if (game.gameState == Game.Companion.GameState.QUESTION) {
                        if (!game.hasAnswered(identifier)) {
                            val controlPacket = receiveDeserialized<GameC2SPacket>()

                            if (controlPacket.action == "client_answer") {
                                val answerPacket = controlPacket.to<AnswerPacket>()

                                if (answerPacket.num > 0 && answerPacket.num < game.quiz.questions.size) {
                                    game.regAnswer(identifier, answerPacket.num)
                                }
                            }
                        }
                    }
                } else {
                    if (game.gameState == Game.Companion.GameState.WAITING) {
                        sendSerialized(JoinGameResponsePacket("Game has already started!"))
                        break
                    }

                    // handle login
                    val joinItem = receiveDeserialized<JoinGamePacket>()

                    if (joinItem.username.isBlank()) {
                        sendSerialized(JoinGameResponsePacket("Invalid username!"))
                        break
                    }

                    if (game.clientSessions.any { it.key.username == joinItem.username }) {
                        sendSerialized(JoinGameResponsePacket("Username already exists!"))
                        break
                    }

                    val identifier = ClientIdentifier(game.getNewId(), joinItem.username)
                    game.clientSessions[identifier] = this
                }

            } catch (e: Exception) {
                game.clientSessions.values.removeIf { it == this }
                break
            }
        }
    }
}

inline fun <reified T> GameC2SPacket.to(): T {
    return Gson().fromJson(this.content, T::class.java)
}

fun Routing.handleNonApiQuiz() {
    get("/edit/{id}") {
        val serverUser = call.tryGetUser()
        if (serverUser == null) {
            call.respondRedirect("/login")
            return@get
        }

        val id = call.parameters["id"] ?: return@get

        val quiz = MainDatabaseHandler.getQuizDatabaseService().get(id)

        if (quiz == null) {
            call.respondStatusPage("Quiz does not exist!", HttpStatusCode.NotFound)
            return@get
        }

        if (serverUser.id != quiz.authorId) {
            call.respondStatusPage("You do not own this quiz!", HttpStatusCode.Forbidden)
            return@get
        }

        call.respondFile(File("src/main/resources/pages/edit.html"))
    }
}

fun Routing.handleQuiz() {
    post("/api/quiz/create") {
        val serverUser = call.tryGetUserForApiCall() ?: return@post

        val createParams = call.receive<CreateQuizParams>()
        if (createParams.title.isBlank() || createParams.description.isBlank()) {
            call.respondLog("Invalid parameters!", HttpStatusCode.BadRequest)
            return@post
        }

        val quiz = Quiz(createParams.title, createParams.description, serverUser.id, hashSetOf(), MainDatabaseHandler.getQuizDatabaseService().getNewId())

        serverUser.quizIdSet.add(quiz.id)

        MainDatabaseHandler.getUserDatabaseService().update(serverUser.id, ServerUser(serverUser.name, serverUser.passwordHash512, serverUser.salt, serverUser.id, serverUser.quizIdSet))
        MainDatabaseHandler.getQuizDatabaseService().insert(quiz)

        call.respondLog(quiz.id)
    }

    delete("/api/quiz/delete") {
        val serverUser = call.tryGetUserForApiCall() ?: return@delete

        val deleteParams = call.receive<DeleteQuizParams>()

        val quiz = call.tryGetQuizForEditsForApiCall(deleteParams.id, serverUser.id) ?: return@delete

        serverUser.quizIdSet.removeIf { it == quiz.id }

        MainDatabaseHandler.getUserDatabaseService().update(serverUser.id, ServerUser(serverUser.name, serverUser.passwordHash512, serverUser.salt, serverUser.id, serverUser.quizIdSet))
        MainDatabaseHandler.getQuizDatabaseService().delete(quiz.id)
        call.respondLog("Quiz deleted!")
    }

    get("/api/quiz/list") {
        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
        val amount = 10

        val serverUser = call.tryGetUserForApiCall() ?: return@get

        val quizzes: HashSet<ListQuizEntry> = hashSetOf()

        for (i in ((0+offset)..<(min(amount, serverUser.quizIdSet.size)))) {
            val curr = MainDatabaseHandler.getQuizDatabaseService().get(serverUser.quizIdSet.elementAt(i))

            if (curr != null) {
                quizzes.add(ListQuizEntry(curr.title, curr.description, curr.id))
            }
        }

        call.respond(quizzes)
    }

    get("/api/quiz/get") {
        val serverUser = call.tryGetUserForApiCall() ?: return@get

        val id = call.request.queryParameters["id"]

        if (id == null) {
            call.respondLog("Missing parameters!", HttpStatusCode.BadRequest)
            return@get
        }

        val quiz = MainDatabaseHandler.getQuizDatabaseService().get(id)

        if (quiz == null) {
            call.respondLog("Quiz does not exist!", HttpStatusCode.NotFound)
            return@get
        }

        if (serverUser.id != quiz.authorId) {
            call.respondLog("You do not own this quiz!", HttpStatusCode.Forbidden)
            return@get
        }

        call.respond(quiz)
    }

    patch("/api/quiz/edit") {
        val updateParams = call.receive<UpdateQuizParams>()

        val serverUser = call.tryGetUserForApiCall() ?: return@patch
        val quiz = call.tryGetQuizForEditsForApiCall(updateParams.id, serverUser.id) ?: return@patch

        // Checking integrity
        if (updateParams.title.isBlank() || updateParams.description.isBlank()) {
            call.respondLog("Invalid parameters!", HttpStatusCode.BadRequest)
            return@patch
        }

        updateParams.questions.forEachIndexed { _, question ->
            if (question.title.isBlank() || question.description.isBlank()) {
                call.respondLog("Invalid parameters!", HttpStatusCode.BadRequest)
                return@patch
            }

            for (option in question.options) {
                if (option.title.isBlank() || option.description.isBlank()) {
                    call.respondLog("Invalid parameters!", HttpStatusCode.BadRequest)
                    return@patch
                }
            }
        }


        MainDatabaseHandler.getQuizDatabaseService().update(quiz.id, Quiz(updateParams.title, updateParams.description, quiz.authorId, updateParams.questions, quiz.id))
        call.respondLog("Questions updated!")
    }
}

fun Routing.handleAccount() {
    post("/api/account/signup") {
        if (call.sessions.get<AccountSession>() != null) {
            call.respondLog("Already logged in!", HttpStatusCode.BadRequest)
            return@post
        }

        val signUpParams = call.receive<AuthParams>()
        if (signUpParams.username.isBlank()
            || signUpParams.username.length < 3
            || signUpParams.passwordHash512.isBlank()
            || signUpParams.username.contains(' ')
            || !HashGenerator.validate512(signUpParams.passwordHash512)
        ) {
            call.respondLog("Invalid parameters!", HttpStatusCode.BadRequest)
            return@post
        }

        if (MainDatabaseHandler.getUserDatabaseService().get(signUpParams.username) != null) {
            call.respondLog("Username already exists!", HttpStatusCode.BadRequest)
            return@post
        }

        val salt = HashGenerator.generateHash()
        val user = ServerUser(signUpParams.username, HashGenerator.digest512(signUpParams.passwordHash512 + salt), salt, MainDatabaseHandler.getUserDatabaseService().getNewId(), hashSetOf())

        MainDatabaseHandler.getUserDatabaseService().insert(user)
        val userSession = AccountSessionManager.createSession(user)

        call.sessions.set(userSession)

        call.respondLog("User created!")
    }

    post("/api/account/logout") {
        val userSession = call.sessions.get<AccountSession>()
        if (userSession == null) {
            call.respondLog("Not logged in!", HttpStatusCode.BadRequest)
            return@post
        }

        AccountSessionManager.removeSession(userSession)
        call.sessions.clear<AccountSession>()
        call.respondLog("Logged out!")
    }

    post("/api/account/login") {
        if (call.sessions.get<AccountSession>() != null) {
            call.respondLog("Already logged in!", HttpStatusCode.BadRequest)
            return@post
        }

        val loginParams = call.receive<AuthParams>()
        if (loginParams.username.isBlank()
            || loginParams.passwordHash512.isBlank()
            || loginParams.username.contains(' ')
            || !HashGenerator.validate512(loginParams.passwordHash512)
        ) {
            call.respondLog("Invalid parameters!", HttpStatusCode.BadRequest)
            return@post
        }

        val requestedUser = MainDatabaseHandler.getUserDatabaseService().get(loginParams.username)

        if (requestedUser == null
            || requestedUser.passwordHash512 != HashGenerator.digest512(loginParams.passwordHash512 + requestedUser.salt)
        ) {
            call.respondLog("Invalid credentials!", HttpStatusCode.BadRequest)
            return@post
        }

        val session = AccountSessionManager.createSession(requestedUser)

        call.sessions.set(session)

        call.respondLog("Logged in!")
    }

    delete("/api/account/delete") {
        val userSession = call.sessions.get<AccountSession>()
        if (userSession == null) {
            call.respondLog("Not logged in!", HttpStatusCode.BadRequest)
            return@delete
        }

        val user = AccountSessionManager.resolveUser(userSession)
        if (user == null) {
            call.respondLog("Unknown user!", HttpStatusCode.BadRequest)
            return@delete
        }


        AccountSessionManager.removeSession(userSession)
        call.sessions.clear<AccountSession>()

        MainDatabaseHandler.getUserDatabaseService().delete(user.id)

        call.respondLog("Deleted and logged out!")
    }
}

fun ApplicationCall.tryGetUser(): ServerUser? {
    val userSession = this.sessions.get<AccountSession>() ?: return null

    val serverUser = AccountSessionManager.resolveUser(userSession) ?: return null

    return serverUser
}

suspend fun ApplicationCall.respondLog(text: String, code: HttpStatusCode= HttpStatusCode.OK) {
    this.respondText(text, status = code)
}

suspend fun ApplicationCall.respondStatusPage(text: String, code: HttpStatusCode= HttpStatusCode.OK) {
    this.respondRedirect("/status?message=$text&code=$code")
}

suspend fun ApplicationCall.tryGetUserForApiCall(): ServerUser? {
    val userSession = this.sessions.get<AccountSession>()
    if (userSession == null) {
        this.respondLog("Not logged in!", HttpStatusCode.BadRequest)
        return null
    }

    val serverUser = AccountSessionManager.resolveUser(userSession)
    if (serverUser == null) {
        this.respondLog("Unknown user!", HttpStatusCode.NotFound)
        return null
    }

    return serverUser
}

suspend fun ApplicationCall.tryGetQuizForEditsForApiCall(id: String, userId: String): Quiz? {
    val quiz = MainDatabaseHandler.getQuizDatabaseService().get(id)

    if (quiz == null) {
        this.respondLog("Quiz does not exist!", HttpStatusCode.NotFound)
        return null
    }

    if (userId != quiz.authorId) {
        this.respondLog("You do not own this quiz!", HttpStatusCode.Forbidden)
        return null
    }

    return quiz
}