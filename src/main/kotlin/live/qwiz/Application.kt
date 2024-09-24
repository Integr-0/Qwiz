package live.qwiz

import com.codahale.metrics.Slf4jReporter
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.gson.*
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
        gson {  }
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

    /*install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(this@module.log)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(10, TimeUnit.SECONDS)
    }*/

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
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
