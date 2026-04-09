package network.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import network.NodeService
import network.dto.BlockDto
import network.dto.BlockResponse
import network.dto.ErrorDto
import network.dto.ErrorResponse
import network.dto.MinerResponse
import network.dto.RegisterPeerRequest
import network.dto.SuccessResponse
import network.dto.TransactionDto
import network.dto.TransactionResponse
import network.results.Accepted
import network.results.BlockAccepted
import network.results.BlockRejected
import network.results.MineFailed
import network.results.MinedSuccessfully
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
                is Accepted -> call.respond(
                    HttpStatusCode.Accepted,
                    TransactionResponse(status = "ok", accepted = true, txId = result.txId)
                )
                is RejectedSubmission -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(status = "error", error = ErrorDto(result.code, result.message))
                )
            }
        }

        post("/blocks") {
            val block = call.receive<BlockDto>()
            when (val result = nodeService.receiveBlock(block)) {
                is BlockAccepted -> call.respond(
                    HttpStatusCode.OK,
                    BlockResponse(
                        status = "ok",
                        accepted = true,
                        action = "appended",
                        chainLength = result.chainLength
                    )
                )
                is BlockRejected -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        status = "error",
                        error = ErrorDto(
                            code = "INVALID_BLOCK",
                            message = result.reasons.joinToString("; ")
                        )
                    )
                )
            }
        }

        post("/mine") {
            when (val result = nodeService.mine(trigger = "manual")) {
                is MinedSuccessfully -> call.respond(
                    HttpStatusCode.OK,
                    MinerResponse(
                        status = "ok",
                        mined = true,
                        trigger = result.trigger,
                        block = result.block
                    )
                )
                is MineFailed -> call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        status = "error",
                        error = ErrorDto(code = "MINE_FAILED", message = result.reason)
                    )
                )
            }
        }

        post("/wallet") {
            call.respond(nodeService.createWallet())
        }
    }
}
