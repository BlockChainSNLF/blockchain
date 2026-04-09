package network.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import network.dto.BlockDto
import network.dto.BlockResponse
import network.dto.ChainResponse
import network.dto.PeersResponse
import network.dto.RegisterPeerRequest
import network.dto.RegisterPeerResponse
import network.dto.StatusResponse
import network.dto.TransactionDto
import network.dto.TransactionResponse


class PeerClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }

        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun getStatus(baseUrl: String): StatusResponse {
        return client.get("$baseUrl/status").body()
    }

    suspend fun getChain(baseUrl: String): ChainResponse {
        return client.get("$baseUrl/chain").body()
    }

    suspend fun getPeers(baseUrl: String): PeersResponse {
        return client.get("$baseUrl/peers").body()
    }

    suspend fun registerAtPeer(baseUrl: String, myUrl: String): RegisterPeerResponse {
        return client.post("$baseUrl/peers") {
            contentType(ContentType.Application.Json)
            setBody(RegisterPeerRequest(url = myUrl))
        }.body()
    }

    suspend fun sendBlock(baseUrl: String, block: BlockDto): BlockResponse {
        return client.post("$baseUrl/blocks") {
            contentType(ContentType.Application.Json)
            setBody(block)
        }.body()
    }

    suspend fun sendTransaction(baseUrl: String, tx: TransactionDto): TransactionResponse {
        return client.post("$baseUrl/transactions") {
            contentType(ContentType.Application.Json)
            setBody(tx)
        }.body()
    }
}