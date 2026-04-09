package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BlockDto(
    val index: Int,
    val timestamp: Long,
    val transactions: List<TransactionDto>,
    val previousHash: String,
    val hash: String,
    val nonce: Long
) {
}