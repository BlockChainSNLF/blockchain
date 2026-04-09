package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BlockResponse(
    val status: String = "ok",
    val accepted: Boolean = true,
    val action: String = "appended",
    val chainLength: Int
)