package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterPeerResponse(
    val status: String = "ok",
    val registered: String,
    val peers: List<String>,
) {
}