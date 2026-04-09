package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String = "ok",
    val node: NodeDto,
    val chain: ChainDataDto,
    val peers: PeersCount
)

@Serializable
data class NodeDto(
    val url: String,
    val address: String,
    val publicKey: String
)

@Serializable
data class ChainDataDto(
    val length: Int,
    val latestHash: String
)

@Serializable
data class PeersCount(
    val count: Int
)

