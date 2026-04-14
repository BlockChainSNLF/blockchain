package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceRequest(
    val address: String
)

