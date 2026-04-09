package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterPeerRequest(
    val url: String
)