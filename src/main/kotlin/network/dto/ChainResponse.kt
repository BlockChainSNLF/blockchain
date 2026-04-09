package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChainResponse(
    val status: String = "ok",
    val chain: List<BlockDto>,
    val length: Int
)
