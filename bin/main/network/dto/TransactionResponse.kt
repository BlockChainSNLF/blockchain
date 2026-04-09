package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    val status: String = "ok",
    val accepted: Boolean,
    val txId: String
) {
}