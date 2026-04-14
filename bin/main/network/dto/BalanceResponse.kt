package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(
    val status: String = "ok",
    val address: String,
    val balance: Int
)

