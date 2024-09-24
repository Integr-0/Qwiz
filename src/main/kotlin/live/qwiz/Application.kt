package live.qwiz

import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import live.qwiz.config.ConfigSystem
import live.qwiz.database.configureDatabases
import live.qwiz.routing.*
import live.qwiz.session.account.AccountSession
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    ConfigSystem.load()
    install(Sessions) {
        val configSecretEncryptKey = ConfigSystem.get().session.secretEncryptKey
        val configSecretSignKey = ConfigSystem.get().session.secretSignKey

        val secretEncryptKey = hex(configSecretEncryptKey)
        val secretSignKey = hex(configSecretSignKey)


        cookie<AccountSession>("session", SessionStorageMemory()) {
            cookie.secure = true
            cookie.httpOnly = true
            cookie.extensions["SameSite"] = "None"
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 60 // 1 hour
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Accept)
        anyHost() // TODO: Don't do this in production if possible. Try to limit it.
        allowHeaders { true }
        allowHeader(HttpHeaders.SetCookie)
        allowHeader(HttpHeaders.Cookie)
        exposeHeader(HttpHeaders.SetCookie)
        exposeHeader(HttpHeaders.Cookie)
        allowCredentials = true
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if (cause is BadRequestException) {
                when (cause.cause) {
                    is JsonConvertException -> {
                        call.respondText(text = "500: Malformed Json", status = HttpStatusCode.InternalServerError)
                    }
                }
            } else call.respondText(text = "500: Internal Server Error: ($cause)", status = HttpStatusCode.InternalServerError)
        }

        status { call, code ->
            call.respondText(text = "${code.value}: ${code.description}", status = code)
        }
    }

    configureDatabases()
    handleRouting()
}

inline fun <reified T> T.jsonTree(): JsonElement {
    return Json.encodeToJsonElement<T>(this)
}

inline fun <reified T> T.json(): String {
    return Json.encodeToString<T>(this)
}

inline fun <reified T> JsonElement.to(): T {
    return Json.decodeFromJsonElement<T>(this)
}

inline fun <reified T> String.to(): T {
    return Json.decodeFromString<T>(this)
}
