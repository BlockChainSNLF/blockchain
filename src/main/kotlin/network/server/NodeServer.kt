package network.server

import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import network.NodeService
import io.ktor.server.routing.Routing

fun startServer(
    host: String,
    port: Int,
    nodeService: NodeService,
    wait: Boolean = true,
    additionalRoutes: Routing.() -> Unit = {}
){
    val server = embeddedServer(
        CIO,
        host = host,
        port = port
    ) {
        module(nodeService, additionalRoutes)
    }

    server.start(wait = wait)
}

fun Application.module(
    nodeService: NodeService,
    additionalRoutes: Routing.() -> Unit = {}
) {

    install(CallLogging)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    configureRoutes(nodeService, additionalRoutes)
}