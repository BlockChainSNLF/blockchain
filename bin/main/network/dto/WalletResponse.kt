package network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWalletResponse(
    val status: String = "ok",
    val wallet: WalletDto
)

@Serializable
data class WalletDto(
    val privateKey: String,
    val publicKey: String,
    val address: String
)