package network.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import network.NodeService
import network.dto.ErrorDto
import network.dto.ErrorResponse
import network.dto.RegisterPeerRequest
import network.dto.SuccessResponse
import network.dto.TransactionDto
import network.dto.TransactionResponse
import network.results.Accepted
import network.results.RejectedSubmission

fun Application.configureRoutes(nodeService: NodeService) {
    routing {
        get("/health") {
            call.respond(SuccessResponse())
        }

        get("/status") {
            call.respond(nodeService.getStatus())
        }

        get("/chain") {
            call.respond(nodeService.getChain())
        }

        get("/peers") {
            call.respond(nodeService.getPeers())
        }

        post("/peers") {
            val request = call.receive<RegisterPeerRequest>()
            call.respond(nodeService.registerPeer(request.url))
        }

        post("/transactions") {
            val tx = call.receive<TransactionDto>()

            when (val result = nodeService.submitTransaction(tx)) {
                is Accepted -> {
                    call.respond(
                        HttpStatusCode.Accepted,
                        TransactionResponse(
                            status = "ok",
                            accepted = true,
                            txId = result.txId
                        )
                    )
                }

                is RejectedSubmission -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            status = "error",
                            error = ErrorDto(
                                code = result.code,
                                message = result.message
                            )
                        )
                    )
                }
            }
        }

        post("/wallet") {call.respond(nodeService.createWallet())}
    }
}