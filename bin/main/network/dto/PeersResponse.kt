package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PeersResponse(
    val status: String = "ok",
    val peers: List<String>,
    val count: Int
) {
}