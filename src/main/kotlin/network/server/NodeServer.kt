package network.server

import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import network.NodeService

fun startServer(
    host: String,
    port: Int,
    nodeService: NodeService
) {
    embeddedServer(
        CIO,
        host = host,
        port = port
    ) {
        module(nodeService)
    }.start(wait = true)
}

fun Application.module(nodeService: NodeService) {

    install(CallLogging)

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    configureRoutes(nodeService)
}