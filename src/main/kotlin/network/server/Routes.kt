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
import network.results.AcceptedBlock
import network.results.RejectedBlockSubmission
import network.results.RejectedSubmission
import miner.MinedBlock
import miner.NotMined
import block.BlockMapper

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
            val tx = try {
                call.receive<TransactionDto>()
            } catch (_: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        status = "error",
                        error = ErrorDto(
                            code = "INVALID_TRANSACTION",
                            message = "Malformed or incomplete transaction JSON"
                        )
                    )
                )
                return@post
            }

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

        post("/blocks") {
            val block = try {
                call.receive<BlockDto>()
            } catch (_: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        status = "error",
                        error = ErrorDto(
                            code = "INVALID_BLOCK",
                            message = "Malformed or incomplete block JSON"
                        )
                    )
                )
                return@post
            }

            when (val result = nodeService.submitBlock(block)) {
                is AcceptedBlock -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BlockResponse(
                            status = "ok",
                            accepted = true,
                            action = "appended",
                            chainLength = result.chainLength
                        )
                    )
                }

                is RejectedBlockSubmission -> {
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

        post("/mine") {
            when (val result = nodeService.mine()) {
                is MinedBlock -> {
                    call.respond(
                        HttpStatusCode.OK,
                        MinerResponse(
                            mined = true,
                            trigger = result.trigger,
                            block = BlockMapper.toDto(result.block)
                        )
                    )
                }

                is NotMined -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(
                            status = "error",
                            error = ErrorDto(
                                code = "MINING_FAILED",
                                message = result.reason
                            )
                        )
                    )
                }
            }
        }

    }
}