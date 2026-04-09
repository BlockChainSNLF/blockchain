package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BlockResponse(
    val status: String = "ok",
    val accepted: Boolean,
    val action: String,
    val chainLength: Int
)