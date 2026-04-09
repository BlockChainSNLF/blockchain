package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String,
    val type: String,
    val from: String,
    val to: String,
    val amount: Int,
    val timestamp: Long,
    val publicKey: String,
    val signature: String
)